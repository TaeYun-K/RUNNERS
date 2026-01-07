package com.runners.app.community.data

import com.runners.app.network.BackendCommunityApi
import com.runners.app.network.CommunityPostCursorListResult
import com.runners.app.network.CreateCommunityPostResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommunityRepository {
    suspend fun listPosts(cursor: String?, size: Int = 20): CommunityPostCursorListResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.listPosts(cursor = cursor, size = size)
        }

    suspend fun createPost(title: String, content: String): CreateCommunityPostResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.createPost(title = title, content = content)
        }
}