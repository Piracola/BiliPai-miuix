package com.android.purebilibili.feature.onboarding

import androidx.annotation.AnyRes
import com.android.purebilibili.R

internal data class OnboardingHeroIconSpec(
    @param:AnyRes val iconRes: Int,
    val imageScale: Float
)

private const val DEFAULT_ICON_IMAGE_SCALE = 1f

/**
 * 极简版：启动器图标固定为默认「蓝雪女仆」，不再按设置的应用图标 key 切换。
 */
internal fun resolveOnboardingHeroIconSpec(
    appIconKey: String
): OnboardingHeroIconSpec {
    return OnboardingHeroIconSpec(
        iconRes = R.mipmap.ic_launcher_blue_snow_maid_round,
        imageScale = DEFAULT_ICON_IMAGE_SCALE
    )
}
