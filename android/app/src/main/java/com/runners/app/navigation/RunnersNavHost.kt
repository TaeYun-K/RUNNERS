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
import com.runners.app.community.CommunityScreen
import com.runners.app.healthconnect.HealthConnectRepository
import com.runners.app.home.HomeScreen
import com.runners.app.home.HomeUiState
import com.runners.app.mypage.MyPageScreen
import com.runners.app.network.GoogleLoginResult
import com.runners.app.records.RecordsDashboardScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
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
    NavHost(
        navController = navController,
        startDestination = AppRoute.Home.route,
        modifier = modifier,
    ) {
        composable(AppRoute.Home.route) {
            val context = LocalContext.current
            val providerPackage = remember { HealthConnectRepository.resolveProviderPackage(context) }

            var totalDistanceKm by remember { mutableStateOf<Double?>(null) }
            var weekDistanceKm by remember { mutableStateOf<Double?>(null) }
            var monthDistanceKm by remember { mutableStateOf<Double?>(null) }
            var recentRunDistanceKm by remember { mutableStateOf<Double?>(null) }
            var recentRunDate by remember { mutableStateOf<LocalDate?>(null) }
            var firstRunDate by remember { mutableStateOf<LocalDate?>(null) }

            LaunchedEffect(providerPackage) {
                totalDistanceKm = null
                weekDistanceKm = null
                monthDistanceKm = null
                recentRunDistanceKm = null
                recentRunDate = null
                firstRunDate = null

                runCatching {
                    val client = HealthConnectRepository.getClient(context, providerPackage)
                    val hasAllPermissions = HealthConnectRepository.hasAllPermissions(client)
                    if (!hasAllPermissions) return@runCatching

                    data class Stats(
                        val totalKm: Double?,
                        val weekKm: Double?,
                        val monthKm: Double?,
                        val recentKm: Double?,
                        val recentDate: LocalDate?,
                        val firstDate: LocalDate?,
                    )

                    val stats = withContext(Dispatchers.IO) {
                        val nowInstant = Instant.now()
                        val zoneId = ZoneId.systemDefault()
                        val nowDate = LocalDate.now(zoneId)
                        val weekStart = nowDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                        val monthStart = nowDate.withDayOfMonth(1)

                        val weekStartInstant = weekStart.atStartOfDay(zoneId).toInstant()
                        val monthStartInstant = monthStart.atStartOfDay(zoneId).toInstant()

                        var recentKm: Double? = null
                        var recentDate: LocalDate? = null
                        val recentSession = HealthConnectRepository.readMostRecentRunningSession(client)
                        if (recentSession != null) {
                            recentKm = HealthConnectRepository.distanceKmForSession(client, recentSession)
                            recentDate = recentSession.startTime.atZone(zoneId).toLocalDate()
                        }

                        var firstDate: LocalDate? = null
                        val firstSession = HealthConnectRepository.readEarliestRunningSession(client)
                        if (firstSession != null) {
                            firstDate = firstSession.startTime.atZone(zoneId).toLocalDate()
                        }

                        val weekSessions = HealthConnectRepository.readRunningSessions(
                            client = client,
                            since = weekStartInstant,
                            until = nowInstant,
                            maxRecords = 200,
                        )
                        var weekKm = 0.0
                        for (session in weekSessions) {
                            weekKm += HealthConnectRepository.distanceKmForSession(client, session)
                        }

                        val monthSessions = HealthConnectRepository.readRunningSessions(
                            client = client,
                            since = monthStartInstant,
                            until = nowInstant,
                            maxRecords = 400,
                        )
                        var monthKm = 0.0
                        for (session in monthSessions) {
                            monthKm += HealthConnectRepository.distanceKmForSession(client, session)
                        }

                        val allSessions = HealthConnectRepository.readRunningSessions(
                            client = client,
                            since = Instant.EPOCH,
                            until = nowInstant,
                            maxRecords = 1000,
                        )
                        var totalKm = 0.0
                        for (session in allSessions) {
                            totalKm += HealthConnectRepository.distanceKmForSession(client, session)
                        }

                        Stats(
                            totalKm = totalKm,
                            weekKm = weekKm,
                            monthKm = monthKm,
                            recentKm = recentKm,
                            recentDate = recentDate,
                            firstDate = firstDate,
                        )
                    }

                    totalDistanceKm = stats.totalKm
                    weekDistanceKm = stats.weekKm
                    monthDistanceKm = stats.monthKm
                    recentRunDistanceKm = stats.recentKm
                    recentRunDate = stats.recentDate
                    firstRunDate = stats.firstDate
                }
            }

            HomeScreen(
                uiState = HomeUiState(
                    nickname = session.name ?: session.email ?: "RUNNERS",
                    totalDistanceKm = totalDistanceKm,
                    weekDistanceKm = weekDistanceKm,
                    monthDistanceKm = monthDistanceKm,
                    recentRunDistanceKm = recentRunDistanceKm,
                    recentRunDate = recentRunDate,
                    firstRunDate = firstRunDate,
                )
            )
        }
        composable(AppRoute.Records.route) { RecordsDashboardScreen() }
        composable(AppRoute.Community.route) { CommunityScreen() }
        composable(AppRoute.MyPage.route) { MyPageScreen(onLogout = onLogout) }
    }
}
