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
import androidx.navigation.NavType
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.health.connect.client.records.ExerciseSessionRecord
import com.runners.app.community.post.ui.create.CommunityCreatePostScreen
import com.runners.app.community.post.ui.edit.CommunityPostEditScreen
import com.runners.app.community.post.ui.detail.CommunityPostDetailScreen
import com.runners.app.community.post.ui.list.CommunityBoardScreen
import com.runners.app.community.post.ui.list.CommunityScreen
import com.runners.app.community.post.state.CommunityPostStatsUpdate
import com.runners.app.community.post.viewmodel.CommunityViewModel
import com.runners.app.community.post.viewmodel.CommunityPostDetailViewModel
import com.runners.app.community.userprofile.CommunityUserProfileScreen
import com.runners.app.healthconnect.HealthConnectRepository
import com.runners.app.home.HomeScreen
import com.runners.app.home.HomeUiState
import com.runners.app.home.PopularPostUiModel
import com.runners.app.home.RecentRunUiModel
import com.runners.app.mypage.MyPageScreen
import com.runners.app.network.CommunityPostBoardType
import com.runners.app.network.GoogleLoginResult
import com.runners.app.network.BackendUserApi
import com.runners.app.records.RecordsDashboardScreen
import com.runners.app.records.RunRecordUiModel
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
    val communityViewModel: CommunityViewModel = viewModel()

    var totalDistanceKm by remember { mutableStateOf<Double?>(null) }
    var weekDistanceKm by remember { mutableStateOf<Double?>(null) }
    var monthDistanceKm by remember { mutableStateOf<Double?>(null) }
    var firstRunDate by remember { mutableStateOf<LocalDate?>(null) }
    var recentRuns by remember { mutableStateOf<List<RecentRunUiModel>>(emptyList()) }
    var allRuns by remember { mutableStateOf<List<RunRecordUiModel>>(emptyList()) }
    var isHomeRefreshing by remember { mutableStateOf(false) }
    var homeRefreshNonce by remember { mutableStateOf(0) }

    data class RunningStatsSnapshot(
        val totalDistanceKm: Double,
        val totalDurationMinutes: Long,
        val runCount: Int,
    )
    var lastSyncedRunningStats by remember { mutableStateOf<RunningStatsSnapshot?>(null) }

    LaunchedEffect(providerPackage) {
        totalDistanceKm = null
        weekDistanceKm = null
        monthDistanceKm = null
        firstRunDate = null
        recentRuns = emptyList()
        allRuns = emptyList()
        isHomeRefreshing = false
        lastSyncedRunningStats = null
    }

    LaunchedEffect(providerPackage, homeRefreshNonce) {
        isHomeRefreshing = true
        try {
            runCatching {
                val client = HealthConnectRepository.getClient(context, providerPackage)
                val hasCorePermissions = HealthConnectRepository.hasAllPermissions(
                    client = client,
                    permissions = HealthConnectRepository.corePermissions,
                )
                if (!hasCorePermissions) return@runCatching

                val hasHistoryPermission = HealthConnectRepository.hasAllPermissions(
                    client = client,
                    permissions = HealthConnectRepository.historyPermission,
                )

            data class Stats(
                val totalKm: Double?,
                val weekKm: Double?,
                val monthKm: Double?,
                val firstDate: LocalDate?,
                val recentRuns: List<RecentRunUiModel>,
                val runs: List<RunRecordUiModel>,
            )

            val stats = withContext(Dispatchers.IO) {
                val nowInstant = Instant.now()
                val zoneId = ZoneId.systemDefault()
                val nowDate = LocalDate.now(zoneId)
                val weekStart = nowDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val monthStart = nowDate.withDayOfMonth(1)

                val weekStartInstant = weekStart.atStartOfDay(zoneId).toInstant()
                val monthStartInstant = monthStart.atStartOfDay(zoneId).toInstant()

                val sinceInstant =
                    if (hasHistoryPermission) {
                        Instant.EPOCH
                    } else {
                        nowInstant.minus(Duration.ofDays(30))
                    }

                val allSessions = HealthConnectRepository.readRunningSessions(
                    client = client,
                    since = sinceInstant,
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
                val runs = ArrayList<RunRecordUiModel>(sessions.size)
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

                    runs.add(
                        RunRecordUiModel(
                            date = sessionDate,
                            startTime = session.startTime,
                            endTime = session.endTime,
                            dataOriginPackageName = session.metadata.dataOrigin.packageName,
                            distanceKm = distanceKm,
                            durationMinutes = durationMinutes,
                        )
                    )

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
                    runs = runs,
                )
            }

            totalDistanceKm = stats.totalKm
            weekDistanceKm = stats.weekKm
            monthDistanceKm = stats.monthKm
            firstRunDate = stats.firstDate
            recentRuns = stats.recentRuns
                allRuns = stats.runs
            }
        } finally {
            isHomeRefreshing = false
        }
    }

    LaunchedEffect(totalDistanceKm, allRuns) {
        val km = totalDistanceKm ?: return@LaunchedEffect
        if (km.isNaN() || km.isInfinite() || km < 0.0) return@LaunchedEffect

        val totalDurationMinutes = allRuns.sumOf { run ->
            run.durationMinutes ?: Duration.between(run.startTime, run.endTime).toMinutes().coerceAtLeast(0L)
        }.coerceAtLeast(0L)
        val runCount = allRuns.size.coerceAtLeast(0)

        val snapshot = RunningStatsSnapshot(
            totalDistanceKm = km,
            totalDurationMinutes = totalDurationMinutes,
            runCount = runCount,
        )

        val last = lastSyncedRunningStats
        val shouldSync =
            last == null ||
                abs(snapshot.totalDistanceKm - last.totalDistanceKm) >= 0.1 ||
                abs(snapshot.totalDurationMinutes - last.totalDurationMinutes) >= 1L ||
                snapshot.runCount != last.runCount
        if (!shouldSync) return@LaunchedEffect

        runCatching {
            withContext(Dispatchers.IO) {
                // Avoid firing immediately during startup recompositions.
                delay(300)
                BackendUserApi.updateRunningStats(
                    totalDistanceKm = snapshot.totalDistanceKm,
                    totalDurationMinutes = snapshot.totalDurationMinutes,
                    runCount = snapshot.runCount,
                )
            }
            lastSyncedRunningStats = snapshot
        }.onFailure {
            runCatching {
                withContext(Dispatchers.IO) {
                    delay(300)
                    BackendUserApi.updateTotalDistanceKm(snapshot.totalDistanceKm)
                }
                lastSyncedRunningStats = snapshot
            }
        }
    }

    val communityPostStatsUpdateKey = "community:post:statsUpdate"
    val communityPostDeletedKey = "community:post:deleted"

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
                isRefreshing = isHomeRefreshing,
                onRefresh = {
                    if (!isHomeRefreshing) homeRefreshNonce += 1
                },
                onOpenCommunity = {
                    navController.navigate(
                        route = AppRoute.Community.route,
                        navOptions = androidx.navigation.navOptions {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        },
                    )
                },
                onPopularPostClick = {
                    navController.navigate(
                        route = AppRoute.Community.route,
                        navOptions = androidx.navigation.navOptions {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        },
                    )
                },
                onRecentRunClick = { date ->
                    navController.navigate(
                        route = AppRoute.Records.createRoute(date),
                        navOptions = androidx.navigation.navOptions {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Do NOT restore previous Records state here; the user expects the newly clicked date to apply.
                            launchSingleTop = false
                            restoreState = false
                        },
                    )
                },
            )
        }
        composable(
            route = AppRoute.Records.routeWithDate,
            arguments = listOf(
                navArgument(AppRoute.Records.dateArg) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) { entry ->
            val dateArg = entry.arguments?.getString(AppRoute.Records.dateArg)
            val initialDate = dateArg?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            RecordsDashboardScreen(
                runs = allRuns,
                providerPackage = providerPackage,
                initialSelectedDate = initialDate,
            )
        }
        composable(AppRoute.Community.route) { entry ->
            val statsUpdate =
                entry.savedStateHandle
                    .getStateFlow<CommunityPostStatsUpdate?>(communityPostStatsUpdateKey, null)
                    .collectAsStateWithLifecycle()
                    .value
            val deletedPostId =
                entry.savedStateHandle
                    .getStateFlow<Long?>(communityPostDeletedKey, null)
                    .collectAsStateWithLifecycle()
                    .value

            LaunchedEffect(statsUpdate) {
                val update = statsUpdate ?: return@LaunchedEffect
                communityViewModel.applyPostStatsUpdate(update)
                entry.savedStateHandle[communityPostStatsUpdateKey] = null
            }
            LaunchedEffect(deletedPostId) {
                val deleted = deletedPostId ?: return@LaunchedEffect
                communityViewModel.deletePost(deleted)
                entry.savedStateHandle[communityPostDeletedKey] = null
            }

            CommunityScreen(
                authorNickname = session.nickname ?: session.email ?: "RUNNERS",
                totalDistanceKm = totalDistanceKm,
                onCreateClick = { navController.navigate(AppRoute.CommunityCreate.route) },
                onPostClick = { postId -> navController.navigate(AppRoute.CommunityPostDetail.createRoute(postId)) },
                onBoardClick = { type ->
                    val arg = type?.name ?: "ALL"
                    navController.navigate(AppRoute.CommunityBoard.createRoute(arg))
                },
                viewModel = communityViewModel,
            )
        }
        composable(
            route = AppRoute.CommunityBoard.route,
            arguments = listOf(navArgument("boardType") { type = NavType.StringType }),
        ) { entry ->
            val raw = entry.arguments?.getString("boardType") ?: "ALL"
            val boardType =
                if (raw.equals("ALL", ignoreCase = true)) {
                    null
                } else {
                    runCatching { CommunityPostBoardType.valueOf(raw) }.getOrNull()
                }

            CommunityBoardScreen(
                boardType = boardType,
                onBack = { navController.popBackStack() },
                onCreateClick = { navController.navigate(AppRoute.CommunityCreate.route) },
                onPostClick = { postId -> navController.navigate(AppRoute.CommunityPostDetail.createRoute(postId)) },
                viewModel = communityViewModel,
            )
        }
        composable(
            route = AppRoute.CommunityUserProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.LongType }),
        ) { entry ->
            val userId = entry.arguments?.getLong("userId") ?: return@composable
            CommunityUserProfileScreen(
                userId = userId,
                onBack = { navController.popBackStack() },
            )
        }
        composable(AppRoute.CommunityCreate.route) {
            CommunityCreatePostScreen(
                authorNickname = session.nickname ?: session.email ?: "RUNNERS",
                authorPictureUrl = session.picture,
                totalDistanceKm = totalDistanceKm,
                onBack = { navController.popBackStack() },
                onCreated = { postId ->
                    navController.navigate(AppRoute.CommunityPostDetail.createRoute(postId)) {
                        popUpTo(AppRoute.CommunityCreate.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                viewModel = communityViewModel,
            )
        }
        composable(
            route = AppRoute.CommunityPostDetail.route,
            arguments = listOf(navArgument("postId") { type = NavType.LongType }),
        ) { entry ->
            val postId = entry.arguments?.getLong("postId") ?: return@composable
            CommunityPostDetailScreen(
                postId = postId,
                onBack = { detail ->
                    if (detail != null) {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(
                                communityPostStatsUpdateKey,
                                CommunityPostStatsUpdate(
                                    postId = detail.postId,
                                    viewCount = detail.viewCount,
                                    recommendCount = detail.recommendCount,
                                    commentCount = detail.commentCount,
                                ),
                            )
                    }
                    navController.popBackStack()
                },
                onEdit = { navController.navigate(AppRoute.CommunityPostEdit.createRoute(postId)) },
                onDeleted = { deleted ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(communityPostDeletedKey, deleted)
                    navController.popBackStack()
                },
                currentUserId = session.userId,
                onAuthorClick = { userId -> navController.navigate(AppRoute.CommunityUserProfile.createRoute(userId)) },
            )
        }
        composable(
            route = AppRoute.CommunityPostEdit.route,
            arguments = listOf(navArgument("postId") { type = NavType.LongType }),
        ) { entry ->
            val postId = entry.arguments?.getLong("postId") ?: return@composable
            val detailEntry = remember(entry) { navController.getBackStackEntry(AppRoute.CommunityPostDetail.route) }
            val viewModel: CommunityPostDetailViewModel =
                viewModel(
                    viewModelStoreOwner = detailEntry,
                    key = "CommunityPostDetailViewModel:$postId",
                    factory = CommunityPostDetailViewModel.Factory(postId = postId),
                )

            CommunityPostEditScreen(
                postId = postId,
                onBack = { navController.popBackStack() },
                currentUserId = session.userId,
                viewModel = viewModel,
            )
        }
        composable(AppRoute.MyPage.route) {
            MyPageScreen(
                session = session,
                onLogout = onLogout,
                onHealthConnectUpdated = { homeRefreshNonce += 1 },
            )
        }
    }
}
