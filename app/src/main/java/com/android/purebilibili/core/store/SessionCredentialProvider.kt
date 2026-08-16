package com.android.purebilibili.core.store

/**
 * 会话凭证接缝（阶段 2 Core 接缝）。
 *
 * 新代码读取账号凭证（Cookie/登录态）应通过本接口注入，而不是直接引用
 * [TokenManager] object。该接口只暴露内存缓存读取面，凭证写入仍留在
 * TokenManager 内部（登录/登出流程不经过本接口，避免扩大写入面）。
 */
interface SessionCredentialProvider {

    /** B 站登录态 Cookie（SESSDATA）。 */
    val sessData: String?

    /** CSRF Token（bili_jct）。 */
    val csrf: String?

    /** 当前用户 mid（null 表示未登录）。 */
    val mid: Long?

    /** 设备标识 Cookie。 */
    val buvid3: String?

    /** 是否大会员。 */
    val isVip: Boolean
}
