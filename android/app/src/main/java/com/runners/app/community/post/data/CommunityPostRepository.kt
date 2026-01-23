package com.runners.app.community.post.data

import com.runners.app.network.BackendCommunityApi
import com.runners.app.network.CommunityPostBoardType
import com.runners.app.network.CommunityPostCursorListResult
import com.runners.app.network.CommunityPostDetailResult
import com.runners.app.network.CommunityPostRecommendResult
import com.runners.app.network.CreateCommunityPostResult
import com.runners.app.network.PresignCommunityImageUploadFileRequest
import com.runners.app.network.PresignCommunityImageUploadResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CommunityPostRepository {
    suspend fun listPosts(
        boardType: CommunityPostBoardType? = null,
        cursor: String?,
        size: Int = 20
    ): CommunityPostCursorListResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.listPosts(boardType = boardType, cursor = cursor, size = size)
        }

    suspend fun searchPosts(
        query: String,
        boardType: CommunityPostBoardType? = null,
        cursor: String?,
        size: Int = 20
    ): CommunityPostCursorListResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.searchPosts(query = query, boardType = boardType, cursor = cursor, size = size)
        }

    suspend fun getPost(postId: Long): CommunityPostDetailResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.getPost(postId)
        }

    suspend fun createPost(
        title: String,
        content: String,
        boardType: CommunityPostBoardType? = null,
        imageKeys: List<String>? = null
    ): CreateCommunityPostResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.createPost(title = title, content = content, boardType = boardType, imageKeys = imageKeys)
        }

    suspend fun updatePost(postId: Long, title: String, content: String, imageKeys: List<String>? = null): CreateCommunityPostResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.updatePost(
                postId = postId,
                title = title,
                content = content,
                imageKeys = imageKeys,
            )
        }

    suspend fun presignCommunityPostImageUploads(files: List<PresignCommunityImageUploadFileRequest>): PresignCommunityImageUploadResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.presignCommunityPostImageUploads(files)
        }

    suspend fun deletePost(postId: Long) =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.deletePost(postId)
        }

    suspend fun recommendPost(postId: Long): CommunityPostRecommendResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.recommendPost(postId)
        }

    suspend fun getPostRecommendStatus(postId: Long): CommunityPostRecommendResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.getPostRecommendStatus(postId)
        }

    suspend fun unrecommendPost(postId: Long): CommunityPostRecommendResult =
        withContext(Dispatchers.IO) {
            BackendCommunityApi.unrecommendPost(postId)
        }
}
