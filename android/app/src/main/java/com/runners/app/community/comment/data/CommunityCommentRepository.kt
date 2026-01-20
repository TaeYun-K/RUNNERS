package com.runners.app.community.comment.data

import com.runners.app.network.BackendCommunityApi
import com.runners.app.network.CommunityCommentCursorListResult
import com.runners.app.network.CommunityCommentMutationResult
import com.runners.app.network.CommunityCommentRecommendResult
import com.runners.app.network.DeleteCommunityCommentResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommunityCommentRepository {
    suspend fun createComment(postId: Long, content: String, parentId: Long? = null): CommunityCommentMutationResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.createComment(postId = postId, content = content, parentId = parentId)
        }

    suspend fun updateComment(postId: Long, commentId: Long, content: String): CommunityCommentMutationResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.updateComment(postId = postId, commentId = commentId, content = content)
        }

    suspend fun deleteComment(postId: Long, commentId: Long): DeleteCommunityCommentResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.deleteComment(postId = postId, commentId = commentId)
        }

    suspend fun listComments(postId: Long, cursor: String?, size: Int = 20): CommunityCommentCursorListResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.listComments(postId = postId, cursor = cursor, size = size)
        }

    suspend fun recommendComment(postId: Long, commentId: Long): CommunityCommentRecommendResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.recommendComment(postId = postId, commentId = commentId)
        }

    suspend fun getCommentRecommendStatus(postId: Long, commentId: Long): CommunityCommentRecommendResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.getCommentRecommendStatus(postId = postId, commentId = commentId)
        }

    suspend fun unrecommendComment(postId: Long, commentId: Long): CommunityCommentRecommendResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.unrecommendComment(postId = postId, commentId = commentId)
        }
}
