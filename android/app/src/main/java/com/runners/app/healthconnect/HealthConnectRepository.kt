package com.runners.app.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.aggregate.AggregationResult
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant

object HealthConnectRepository {
    private val providerPackages = listOf(
        "com.android.healthconnect.controller",
        "com.google.android.apps.healthdata",
    )

    val corePermissions: Set<String> = setOf(
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
    )

    val historyPermission: Set<String> = setOf(
        HealthPermission.PERMISSION_READ_HEALTH_DATA_HISTORY,
    )

    val optionalDetailPermissions: Set<String> = setOf(
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
    )

    val requestedPermissions: Set<String> = corePermissions + historyPermission + optionalDetailPermissions

    val requiredPermissionLabels: Map<String, String> = mapOf(
        HealthPermission.getReadPermission(ExerciseSessionRecord::class) to "운동 세션(러닝) 읽기",
        HealthPermission.getReadPermission(DistanceRecord::class) to "거리(km) 읽기",
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class) to "총 소모 칼로리 읽기",
        HealthPermission.getReadPermission(HeartRateRecord::class) to "심박 읽기",
        HealthPermission.getReadPermission(StepsRecord::class) to "걸음수 읽기(케이던스 계산)",
        HealthPermission.PERMISSION_READ_HEALTH_DATA_HISTORY to "과거 건강 데이터(히스토리) 읽기",
    )

    fun resolveProviderPackage(context: Context): String? {
        val pm = context.packageManager
        return providerPackages.firstOrNull { pkg ->
            runCatching { pm.getPackageInfo(pkg, 0) }.isSuccess
        }
    }

    fun getSdkStatus(context: Context, providerPackage: String? = null): Int =
        if (providerPackage == null) {
            HealthConnectClient.getSdkStatus(context)
        } else {
            HealthConnectClient.getSdkStatus(context, providerPackage)
        }

    fun getClient(context: Context, providerPackage: String? = null): HealthConnectClient =
        if (providerPackage == null) {
            HealthConnectClient.getOrCreate(context)
        } else {
            HealthConnectClient.getOrCreate(context, providerPackage)
        }

    suspend fun hasAllPermissions(
        client: HealthConnectClient,
        permissions: Set<String> = corePermissions,
    ): Boolean {
        val granted = client.permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    suspend fun readRunningSessionCount(
        client: HealthConnectClient,
        since: Instant = Instant.EPOCH,
        until: Instant = Instant.now(),
        dataOrigin: DataOrigin? = null,
    ): Int {
        val request = ReadRecordsRequest(
            recordType = ExerciseSessionRecord::class,
            timeRangeFilter = TimeRangeFilter.between(since, until),
        )
        val response = client.readRecords(request)

        return response.records.count { record ->
            record.exerciseType == ExerciseSessionRecord.EXERCISE_TYPE_RUNNING &&
                (dataOrigin == null || record.metadata.dataOrigin == dataOrigin)
        }
    }

    suspend fun readRunningSessions(
        client: HealthConnectClient,
        since: Instant,
        until: Instant,
        maxRecords: Int = Int.MAX_VALUE,
        dataOrigin: DataOrigin? = null,
    ): List<ExerciseSessionRecord> {
        val results = ArrayList<ExerciseSessionRecord>()
        var pageToken: String? = null

        while (results.size < maxRecords) {
            val request = ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(since, until),
                dataOriginFilter = if (dataOrigin == null) emptySet() else setOf(dataOrigin),
                ascendingOrder = false,
                pageSize = 50,
                pageToken = pageToken,
            )
            val response = client.readRecords(request)
            for (record in response.records) {
                if (record.exerciseType == ExerciseSessionRecord.EXERCISE_TYPE_RUNNING) {
                    results.add(record)
                    if (results.size >= maxRecords) break
                }
            }
            pageToken = response.pageToken
            if (pageToken.isNullOrEmpty()) break
        }

        return results
    }

    suspend fun readMostRecentRunningSession(
        client: HealthConnectClient,
        dataOrigin: DataOrigin? = null,
    ): ExerciseSessionRecord? {
        var pageToken: String? = null

        repeat(10) {
            val request = ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(Instant.EPOCH, Instant.now()),
                dataOriginFilter = if (dataOrigin == null) emptySet() else setOf(dataOrigin),
                ascendingOrder = false,
                pageSize = 50,
                pageToken = pageToken,
            )
            val response = client.readRecords(request)
            val found = response.records.firstOrNull { it.exerciseType == ExerciseSessionRecord.EXERCISE_TYPE_RUNNING }
            if (found != null) return found
            pageToken = response.pageToken
            if (pageToken.isNullOrEmpty()) return null
        }

        return null
    }

    suspend fun readEarliestRunningSession(
        client: HealthConnectClient,
        dataOrigin: DataOrigin? = null,
    ): ExerciseSessionRecord? {
        var pageToken: String? = null

        repeat(20) {
            val request = ReadRecordsRequest(
                recordType = ExerciseSessionRecord::class,
                timeRangeFilter = TimeRangeFilter.between(Instant.EPOCH, Instant.now()),
                dataOriginFilter = if (dataOrigin == null) emptySet() else setOf(dataOrigin),
                ascendingOrder = true,
                pageSize = 100,
                pageToken = pageToken,
            )
            val response = client.readRecords(request)
            val found = response.records.firstOrNull { it.exerciseType == ExerciseSessionRecord.EXERCISE_TYPE_RUNNING }
            if (found != null) return found
            pageToken = response.pageToken
            if (pageToken.isNullOrEmpty()) return null
        }

        return null
    }

    suspend fun aggregateDistance(
        client: HealthConnectClient,
        since: Instant,
        until: Instant,
        dataOrigins: Set<DataOrigin> = emptySet(),
    ): Double {
        val request = AggregateRequest(
            metrics = setOf(DistanceRecord.DISTANCE_TOTAL),
            timeRangeFilter = TimeRangeFilter.between(since, until),
            dataOriginFilter = dataOrigins,
        )
        val result: AggregationResult = client.aggregate(request)
        val metricKey = DistanceRecord.DISTANCE_TOTAL.metricKey
        return result.doubleValues[metricKey] ?: 0.0
    }

    suspend fun distanceKmForSession(
        client: HealthConnectClient,
        session: ExerciseSessionRecord,
    ): Double {
        val meters = aggregateDistance(
            client = client,
            since = session.startTime,
            until = session.endTime,
            dataOrigins = setOf(session.metadata.dataOrigin),
        )
        return meters / 1000.0
    }

    suspend fun caloriesKcalForSession(
        client: HealthConnectClient,
        since: Instant,
        until: Instant,
        dataOrigin: DataOrigin? = null,
    ): Double {
        var total = 0.0
        var pageToken: String? = null

        while (true) {
            val request = ReadRecordsRequest(
                recordType = TotalCaloriesBurnedRecord::class,
                timeRangeFilter = TimeRangeFilter.between(since, until),
                dataOriginFilter = if (dataOrigin == null) emptySet() else setOf(dataOrigin),
                ascendingOrder = false,
                pageSize = 100,
                pageToken = pageToken,
            )
            val response = client.readRecords(request)
            for (record in response.records) {
                total += record.energy.inKilocalories
            }
            pageToken = response.pageToken
            if (pageToken.isNullOrEmpty()) break
        }

        return total
    }

    data class HeartRateSummary(
        val avgBpm: Int?,
        val maxBpm: Int?,
    )

    suspend fun heartRateSummaryForSession(
        client: HealthConnectClient,
        since: Instant,
        until: Instant,
        dataOrigin: DataOrigin? = null,
    ): HeartRateSummary {
        var sum = 0.0
        var count = 0
        var max = 0
        var pageToken: String? = null

        while (true) {
            val request = ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(since, until),
                dataOriginFilter = if (dataOrigin == null) emptySet() else setOf(dataOrigin),
                ascendingOrder = true,
                pageSize = 100,
                pageToken = pageToken,
            )
            val response = client.readRecords(request)
            for (record in response.records) {
                for (sample in record.samples) {
                    val bpm = sample.beatsPerMinute.toInt()
                    if (bpm <= 0) continue
                    sum += bpm.toDouble()
                    count += 1
                    if (bpm > max) max = bpm
                }
            }
            pageToken = response.pageToken
            if (pageToken.isNullOrEmpty()) break
        }

        val avg = if (count > 0) (sum / count).toInt() else null
        val maxBpm = if (max > 0) max else null
        return HeartRateSummary(avgBpm = avg, maxBpm = maxBpm)
    }

    suspend fun stepsForSession(
        client: HealthConnectClient,
        since: Instant,
        until: Instant,
        dataOrigin: DataOrigin? = null,
    ): Long {
        var total = 0L
        var pageToken: String? = null

        while (true) {
            val request = ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(since, until),
                dataOriginFilter = if (dataOrigin == null) emptySet() else setOf(dataOrigin),
                ascendingOrder = false,
                pageSize = 200,
                pageToken = pageToken,
            )
            val response = client.readRecords(request)
            for (record in response.records) {
                total += record.count
            }
            pageToken = response.pageToken
            if (pageToken.isNullOrEmpty()) break
        }

        return total
    }
}
