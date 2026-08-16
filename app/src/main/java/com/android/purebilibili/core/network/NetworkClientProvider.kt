package com.android.purebilibili.core.network

import okhttp3.OkHttpClient

/**
 * 网络客户端接缝（阶段 2 Core 接缝）。
 *
 * 新代码应通过构造函数注入本接口（默认参数指向 [NetworkModule]），而不是
 * 直接引用 object。业务层只依赖该接口时，网络客户端实现可独立替换/测试。
 * 只暴露新代码最常使用的访问面，避免一开始就包装全部 13 个 Retrofit 客户端。
 */
interface NetworkClientProvider {

    /** 主 API 客户端（B 站常规 API）。 */
    val api: BilibiliApi

    val okHttpClient: OkHttpClient

    /** 播放流专用 OkHttpClient（独立超时/代理策略）。 */
    val playbackOkHttpClient: OkHttpClient

    /** 当前播放账号作用域的 Bilibili API 客户端（未选播放账号时即 [api]）。 */
    fun playbackApi(): BilibiliApi

    /** 当前播放账号作用域的番剧 API 客户端。 */
    fun playbackBangumiApi(): BangumiApi
}
