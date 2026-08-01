// 文件路径: data/repository/SponsorBlockRepository.kt
package com.android.purebilibili.data.repository

import com.android.purebilibili.core.network.NetworkModule
import com.android.purebilibili.data.model.response.SponsorCategory
import com.android.purebilibili.data.model.response.SponsorSegment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit

internal fun buildSponsorBlockHttpClient(baseClient: OkHttpClient): OkHttpClient {
    return baseClient.newBuilder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()
}

internal fun buildSponsorBlockSegmentsUrl(
    baseUrl: String,
    bvid: String,
    cid: Long = 0L,
    categories: List<String> = SponsorCategory.ALL_SKIP_CATEGORIES
): String {
    val params = buildList {
        add("videoID=$bvid")
        if (cid > 0L) {
            add("cid=$cid")
        }
        categories.forEach { category ->
            add("category=$category")
        }
    }
    return "$baseUrl/skipSegments?${params.joinToString("&")}"
}

/**
 * 空降助手 (BilibiliSponsorBlock) 数据仓库
 * API 文档: https://github.com/hanydd/BilibiliSponsorBlock/wiki/API
 */
object SponsorBlockRepository {
    const val DEFAULT_BASE_URL = "https://bsbsb.top/api"
    private const val TAG = "SponsorBlock"
    
    private val client = buildSponsorBlockHttpClient(NetworkModule.okHttpClient)
    
    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
    }

    @Serializable
    data class SegmentSubmission(
        val segment: List<Float>,
        val category: String,
        val actionType: String = "skip"
    )

    @Serializable
    private data class SegmentSubmissionRequest(
        val videoID: String,
        val cid: String,
        val userID: String,
        val videoDuration: Float,
        val userAgent: String,
        val segments: List<SegmentSubmission>
    )

    data class ServerStatus(
        val reachable: Boolean,
        val message: String
    )

    data class CommunityUserInfo(
        val userName: String = "",
        val viewCount: Long = 0L,
        val minutesSaved: Double = 0.0,
        val segmentCount: Int = 0
    )
    
    /**
     * 获取视频的空降片段
     * @param bvid 视频 BV 号
     * @param categories 要获取的片段类别，默认获取所有跳过类别
     * @return 片段列表，失败返回空列表
     */
    suspend fun getSegments(
        bvid: String,
        cid: Long = 0L,
        categories: List<String> = SponsorCategory.ALL_SKIP_CATEGORIES,
        baseUrl: String = DEFAULT_BASE_URL
    ): List<SponsorSegment> = withContext(Dispatchers.IO) {
        try {
            // 构建 URL，添加类别参数
            val url = buildSponsorBlockSegmentsUrl(
                baseUrl = baseUrl.trimEnd('/'),
                bvid = bvid,
                cid = cid,
                categories = categories
            )
            
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "BiliPai/2.4.1")
                .get()
                .build()
            
            val response = client.newCall(request).execute()
            
            when (response.code) {
                200 -> {
                    val body = response.body.string()
                    val segments = json.decodeFromString<List<SponsorSegment>>(body)
                    android.util.Log.d(TAG, "获取到 ${segments.size} 个空降片段 for $bvid")
                    segments.filter { it.isSkipType } // 只返回跳过类型的片段
                }
                404 -> {
                    // 没有空降数据，这是正常情况
                    android.util.Log.d(TAG, "视频 $bvid 没有空降数据")
                    emptyList()
                }
                else -> {
                    android.util.Log.w(TAG, "API 返回错误: ${response.code}")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "获取空降片段失败: ${e.message}")
            emptyList()
        }
    }

    suspend fun checkServerStatus(baseUrl: String): ServerStatus = withContext(Dispatchers.IO) {
        runCatching {
            val response = client.newCall(
                Request.Builder().url("${baseUrl.trimEnd('/')}/status/uptime").get().build()
            ).execute()
            response.use {
                if (it.isSuccessful) ServerStatus(true, "服务正常")
                else ServerStatus(false, "HTTP ${it.code}")
            }
        }.getOrElse { error -> ServerStatus(false, error.message ?: "无法连接服务器") }
    }

    suspend fun getCommunityUserInfo(
        baseUrl: String,
        userId: String
    ): Result<CommunityUserInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val url = okhttp3.HttpUrl.Builder()
                .scheme(baseUrl.substringBefore("://"))
                .host(java.net.URI(baseUrl).host)
                .apply {
                    val port = java.net.URI(baseUrl).port
                    if (port != -1) port(port)
                    java.net.URI(baseUrl).path.trim('/').split('/').filter(String::isNotBlank).forEach(::addPathSegment)
                    addPathSegment("userInfo")
                    addQueryParameter("userID", userId)
                    addQueryParameter("values", "[\"userName\",\"viewCount\",\"minutesSaved\",\"segmentCount\"]")
                }
                .build()
            val response = client.newCall(Request.Builder().url(url).get().build()).execute()
            response.use {
                require(it.isSuccessful) { "HTTP ${it.code}" }
                val payload = json.parseToJsonElement(it.body.string()).jsonObject
                CommunityUserInfo(
                    userName = payload["userName"]?.jsonPrimitive?.contentOrNull.orEmpty(),
                    viewCount = payload["viewCount"]?.jsonPrimitive?.longOrNull ?: 0L,
                    minutesSaved = payload["minutesSaved"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                    segmentCount = payload["segmentCount"]?.jsonPrimitive?.intOrNull ?: 0
                )
            }
        }
    }

    suspend fun uploadViewedSegment(baseUrl: String, segmentId: String): Result<Unit> = postJson(
        url = "${baseUrl.trimEnd('/')}/viewedVideoSponsorTime",
        body = "{\"UUID\":${json.encodeToString(segmentId)}}"
    )

    suspend fun submitSegments(
        baseUrl: String,
        bvid: String,
        cid: Long,
        userId: String,
        videoDurationSeconds: Float,
        segments: List<SegmentSubmission>
    ): Result<Unit> = postJson(
        url = "${baseUrl.trimEnd('/')}/skipSegments",
        body = json.encodeToString(
            SegmentSubmissionRequest(
                videoID = bvid,
                cid = cid.toString(),
                userID = userId,
                videoDuration = videoDurationSeconds,
                userAgent = "BiliPai",
                segments = segments
            )
        )
    )

    private suspend fun postJson(url: String, body: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(url)
                .post(body.toRequestBody("application/json".toMediaType()))
                .build()
            client.newCall(request).execute().use { response ->
                require(response.isSuccessful) { "HTTP ${response.code}: ${response.body.string().take(160)}" }
            }
        }
    }
    
    /**
     * 检查当前播放位置是否在某个空降片段内
     * @param segments 片段列表
     * @param currentPositionMs 当前播放位置（毫秒）
     * @return 匹配的片段，没有则返回 null
     */
    fun findSegmentAtPosition(
        segments: List<SponsorSegment>,
        currentPositionMs: Long
    ): SponsorSegment? {
        val currentSeconds = currentPositionMs / 1000f
        return segments.find { segment ->
            currentSeconds >= segment.startTime && currentSeconds < segment.endTime - 0.5f
        }
    }
    
    /**
     * 获取下一个即将到来的空降片段
     * @param segments 片段列表
     * @param currentPositionMs 当前播放位置（毫秒）
     * @param lookAheadMs 提前多少毫秒提示
     * @return 即将到来的片段，没有则返回 null
     */
    fun findUpcomingSegment(
        segments: List<SponsorSegment>,
        currentPositionMs: Long,
        lookAheadMs: Long = 2000
    ): SponsorSegment? {
        val currentSeconds = currentPositionMs / 1000f
        val lookAheadSeconds = lookAheadMs / 1000f
        
        return segments.find { segment ->
            val timeToStart = segment.startTime - currentSeconds
            timeToStart > 0 && timeToStart <= lookAheadSeconds
        }
    }
}
