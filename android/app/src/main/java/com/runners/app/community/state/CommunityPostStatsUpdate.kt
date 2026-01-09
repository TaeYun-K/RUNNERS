package com.runners.app.community.state

import java.io.Serializable

data class CommunityPostStatsUpdate(
    val postId: Long,
    val viewCount: Int,
    val recommendCount: Int,
    val commentCount: Int,
) : Serializable

