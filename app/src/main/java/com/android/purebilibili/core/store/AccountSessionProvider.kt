package com.android.purebilibili.core.store

import android.content.Context
import com.android.purebilibili.data.model.response.NavData

/**
 * 账号会话接缝（阶段 2 Core 接缝）。
 *
 * 新代码应通过构造函数注入本接口（默认参数指向 [AccountSessionStore]），
 * 而不是直接引用 object 单例。业务层只依赖该接口时，账号存储实现可独立
 * 替换/测试，UI 层也不必感知具体持久化机制。
 */
interface AccountSessionProvider {

    fun getAccounts(context: Context): List<StoredAccountSession>

    fun getActiveAccountMid(context: Context): Long?

    fun getPlaybackAccountMid(context: Context): Long?

    fun getPlaybackAccount(context: Context): StoredAccountSession?

    fun setPlaybackAccountMid(context: Context, mid: Long?): Boolean

    fun clearActiveAccount(context: Context)

    fun removeAccount(context: Context, mid: Long): Boolean

    suspend fun upsertCurrentAccount(context: Context, navData: NavData? = null): StoredAccountSession?

    suspend fun activateAccount(context: Context, mid: Long): Boolean
}
