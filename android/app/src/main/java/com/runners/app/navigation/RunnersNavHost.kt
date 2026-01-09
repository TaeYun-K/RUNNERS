package com.runners.app.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.health.connect.client.records.ExerciseSessionRecord
import com.runners.app.community.ui.CommunityScreen
import com.runners.app.healthconnect.HealthConnectRepository
import com.runners.app.home.HomeScreen
import com.runners.app.home.HomeUiState
import com.runners.app.home.PopularPostUiModel
import com.runners.app.home.RecentRunUiModel
import com.runners.app.mypage.MyPageScreen
import com.runners.app.network.GoogleLoginResult
import com.runners.app.network.BackendUserApi
import com.runners.app.records.RecordsDashboardScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.abs
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

@Composable
fun RunnersNavHost(
    navController: NavHostController,
    session: GoogleLoginResult,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val providerPackage = remember(context) { HealthConnectRepository.resolveProviderPackage(context) }

    var totalDistanceKm by remember { mutableStateOf<Double?>(null) }
    var weekDistanceKm by remember { mutableStateOf<Double?>(null) }
    var monthDistanceKm by remember { mutableStateOf<Double?>(null) }
    var firstRunDate by remember { mutableStateOf<LocalDate?>(null) }
    var recentRuns by remember { mutableStateOf<List<RecentRunUiModel>>(emptyList()) }
    var lastSyncedTotalDistanceKm by remember { mutableStateOf<Double?>(null) }

    LaunchedEffect(providerPackage) {
        totalDistanceKm = null
        weekDistanceKm = null
        monthDistanceKm = null
        firstRunDate = null
        recentRuns = emptyList()

        runCatching {
            val client = HealthConnectRepository.getClient(context, providerPackage)
            val hasAllPermissions = HealthConnectRepository.hasAllPermissions(client)
            if (!hasAllPermissions) return@runCatching

            data class Stats(
                val totalKm: Double?,
                val weekKm: Double?,
                val monthKm: Double?,
                val firstDate: LocalDate?,
                val recentRuns: List<RecentRunUiModel>,
            )

            val stats = withContext(Dispatchers.IO) {
                val nowInstant = Instant.now()
                val zoneId = ZoneId.systemDefault()
                val nowDate = LocalDate.now(zoneId)
                val weekStart = nowDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val monthStart = nowDate.withDayOfMonth(1)

                val weekStartInstant = weekStart.atStartOfDay(zoneId).toInstant()
                val monthStartInstant = monthStart.atStartOfDay(zoneId).toInstant()

                val allSessions = HealthConnectRepository.readRunningSessions(
                    client = client,
                    since = Instant.EPOCH,
                    until = nowInstant,
                    maxRecords = 1000,
                )

                fun isPreferredManualOrigin(session: ExerciseSessionRecord): Boolean {
                    val packageName = session.metadata.dataOrigin.packageName.lowercase()
                    return packageName.contains("zepp") || packageName.contains("huami")
                }

                fun overlaps(a: ExerciseSessionRecord, b: ExerciseSessionRecord): Boolean {
                    val startDiffMillis = abs(Duration.between(a.startTime, b.startTime).toMillis())
                    if (startDiffMillis <= Duration.ofMinutes(10).toMillis()) return true

                    val latestStart = maxOf(a.startTime, b.startTime)
                    val earliestEnd = minOf(a.endTime, b.endTime)
                    return earliestEnd.isAfter(latestStart)
                }

                fun pickPreferred(a: ExerciseSessionRecord, b: ExerciseSessionRecord): ExerciseSessionRecord {
                    val aPreferred = isPreferredManualOrigin(a)
                    val bPreferred = isPreferredManualOrigin(b)
                    if (aPreferred != bPreferred) return if (aPreferred) a else b

                    val aMinutes = Duration.between(a.startTime, a.endTime).toMinutes()
                    val bMinutes = Duration.between(b.startTime, b.endTime).toMinutes()
                    if (aMinutes != bMinutes) return if (aMinutes > bMinutes) a else b

                    return if (a.startTime.isAfter(b.startTime)) a else b
                }

                fun dedupeSessions(sessions: List<ExerciseSessionRecord>): List<ExerciseSessionRecord> {
                    val chosen = ArrayList<ExerciseSessionRecord>(sessions.size)

                    for (session in sessions) {
                        var merged = false
                        for (i in chosen.indices) {
                            if (overlaps(session, chosen[i])) {
                                chosen[i] = pickPreferred(session, chosen[i])
                                merged = true
                                break
                            }
                        }
                        if (!merged) chosen.add(session)
                    }

                    chosen.sortByDescending { it.startTime }
                    return chosen
                }

                val sessions = dedupeSessions(allSessions)

                var totalKm = 0.0
                var weekKm = 0.0
                var monthKm = 0.0

                val recent = ArrayList<RecentRunUiModel>(5)
                val firstDate = sessions.minByOrNull { it.startTime }?.startTime?.atZone(zoneId)?.toLocalDate()

                for ((index, session) in sessions.withIndex()) {
                    val distanceKm = HealthConnectRepository.distanceKmForSession(client, session)
                    val sessionDate = session.startTime.atZone(zoneId).toLocalDate()
                    val durationMinutes = Duration.between(session.startTime, session.endTime)
                        .toMinutes()
                        .takeIf { it > 0 }

                    totalKm += distanceKm
                    if (session.startTime >= weekStartInstant) weekKm += distanceKm
                    if (session.startTime >= monthStartInstant) monthKm += distanceKm

                    if (index < 5) {
                        recent.add(
                            RecentRunUiModel(
                                date = sessionDate,
                                distanceKm = distanceKm,
                                durationMinutes = durationMinutes,
                            )
                        )
                    }
                }

                Stats(
                    totalKm = totalKm,
                    weekKm = weekKm,
                    monthKm = monthKm,
                    firstDate = firstDate,
                    recentRuns = recent,
                )
            }

            totalDistanceKm = stats.totalKm
            weekDistanceKm = stats.weekKm
            monthDistanceKm = stats.monthKm
            firstRunDate = stats.firstDate
            recentRuns = stats.recentRuns
        }
    }

    LaunchedEffect(totalDistanceKm) {
        val km = totalDistanceKm ?: return@LaunchedEffect
        if (km.isNaN() || km.isInfinite() || km < 0.0) return@LaunchedEffect

        val last = lastSyncedTotalDistanceKm
        val shouldSync = last == null || kotlin.math.abs(km - last) >= 0.1
        if (!shouldSync) return@LaunchedEffect

        runCatching {
            withContext(Dispatchers.IO) {
                // Avoid firing immediately during startup recompositions.
                delay(300)
                BackendUserApi.updateTotalDistanceKm(km)
            }
            lastSyncedTotalDistanceKm = km
        }
    }

    NavHost(
        navController = navController,
        startDestination = AppRoute.Home.route,
        modifier = modifier,
    ) {
        composable(AppRoute.Home.route) {
            val popularPosts = remember { emptyList<PopularPostUiModel>() }

            HomeScreen(
                uiState = HomeUiState(
                    nickname = session.nickname ?: session.email ?: "RUNNERS",
                    totalDistanceKm = totalDistanceKm,
                    weekDistanceKm = weekDistanceKm,
                    monthDistanceKm = monthDistanceKm,
                    firstRunDate = firstRunDate,
                    recentRuns = recentRuns,
                    popularPosts = popularPosts,
                ),
                onOpenCommunity = { navController.navigate(AppRoute.Community.route) },
                onPopularPostClick = { navController.navigate(AppRoute.Community.route) },
            )
        }
        composable(AppRoute.Records.route) { RecordsDashboardScreen() }
        composable(AppRoute.Community.route) {
            CommunityScreen(
                authorNickname = session.nickname ?: session.email ?: "RUNNERS",
                totalDistanceKm = totalDistanceKm,
            )
        }
        composable(AppRoute.MyPage.route) { MyPageScreen(session = session, onLogout = onLogout) }
    }
}
