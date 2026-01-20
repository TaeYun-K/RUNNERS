package com.runners.app.community.post.data

import com.runners.app.network.BackendCommunityApi
import com.runners.app.network.CommunityPostCursorListResult
import com.runners.app.network.CommunityPostDetailResult
import com.runners.app.network.CreateCommunityPostResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommunityPostRepository {
    suspend fun listPosts(cursor: String?, size: Int = 20): CommunityPostCursorListResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.listPosts(cursor = cursor, size = size)
        }

    suspend fun getPost(postId: Long): CommunityPostDetailResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.getPost(postId)
        }

    suspend fun createPost(title: String, content: String): CreateCommunityPostResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.createPost(title = title, content = content)
        }

    suspend fun updatePost(postId: Long, title: String, content: String): CreateCommunityPostResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.updatePost(
                postId = postId,
                title = title,
                content = content,
            )
        }

    suspend fun deletePost(postId: Long) =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.deletePost(postId)
        }
}

