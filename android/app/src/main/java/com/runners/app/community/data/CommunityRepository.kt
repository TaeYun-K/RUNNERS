package com.runners.app.community.data

import com.runners.app.network.BackendCommunityApi
import com.runners.app.network.CommunityPostCursorListResult
import com.runners.app.network.CommunityPostDetailResult
import com.runners.app.network.CreateCommunityPostResult
import com.runners.app.network.CreateCommunityCommentResult
import com.runners.app.network.DeleteCommunityCommentResult
import com.runners.app.network.CommunityCommentCursorListResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommunityRepository {
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

    suspend fun createComment(postId: Long, content: String, parentId: Long? = null): CreateCommunityCommentResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.createComment(postId = postId, content = content, parentId = parentId)
        }

    suspend fun deleteComment(postId: Long, commentId: Long): DeleteCommunityCommentResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.deleteComment(postId = postId, commentId = commentId)
        }

    suspend fun listComments(postId: Long, cursor: String?, size: Int = 20): CommunityCommentCursorListResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.listComments(postId = postId, cursor = cursor, size = size)
        }
}
