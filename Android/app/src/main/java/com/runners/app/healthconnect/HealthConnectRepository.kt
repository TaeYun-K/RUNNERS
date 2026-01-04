package com.runners.app.healthconnect

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.metadata.DataOrigin
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant

object HealthConnectRepository {
    val requiredPermissions: Set<String> = setOf(
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
    )

    val requiredPermissionLabels: Map<String, String> = mapOf(
        HealthPermission.getReadPermission(ExerciseSessionRecord::class) to "운동 세션(러닝) 읽기",
        HealthPermission.getReadPermission(DistanceRecord::class) to "거리(km) 읽기",
    )

    fun getSdkStatus(context: Context): Int = HealthConnectClient.getSdkStatus(context)

    fun getClient(context: Context): HealthConnectClient = HealthConnectClient.getOrCreate(context)

    suspend fun hasAllPermissions(client: HealthConnectClient): Boolean {
        val granted = client.permissionController.getGrantedPermissions()
        return granted.containsAll(requiredPermissions)
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
}
