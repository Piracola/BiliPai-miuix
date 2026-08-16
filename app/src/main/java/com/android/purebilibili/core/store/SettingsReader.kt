package com.android.purebilibili.core.store

import android.content.Context
import com.android.purebilibili.feature.settings.AppLanguage
import com.android.purebilibili.feature.settings.AppThemeMode
import com.android.purebilibili.feature.settings.DarkThemeStyle
import kotlinx.coroutines.flow.Flow

/**
 * 设置读取接缝（阶段 2 Core 接缝）。
 *
 * 新代码读取设置应通过本接口注入（默认参数指向 [SettingsManager]），而不是
 * 直接引用 6000+ 行的 object。本接口是**窄接口**：只声明新代码实际需要的
 * getter，不包装整个 SettingsManager，避免把庞大的设置门面固化进依赖边界。
 * 需要新增设置读取面时按需扩展，而不是一开始就做全量映射。
 */
interface SettingsReader {

    fun getAutoPlay(context: Context): Flow<Boolean>

    fun getHwDecode(context: Context): Flow<Boolean>

    fun getAppThemeSettings(context: Context): Flow<AppThemeSettings>

    fun getThemeMode(context: Context): Flow<AppThemeMode>

    fun getAppLanguage(context: Context): Flow<AppLanguage>

    fun getDarkThemeStyle(context: Context): Flow<DarkThemeStyle>

    fun getSponsorBlockAutoSkip(context: Context): Flow<Boolean>
}
