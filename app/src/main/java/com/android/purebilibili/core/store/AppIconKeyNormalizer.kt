package com.android.purebilibili.core.store

const val DEFAULT_APP_ICON_KEY = "icon_blue_snow_maid"

private val CANONICAL_APP_ICON_KEYS = setOf(
    "icon_blue_snow_maid",
    "icon_blue_snow_maid_announcement",
    "icon_blue_snow_maid_front",
    "icon_3d",
    "icon_bilipai",
    "icon_bilipai_pink",
    "icon_bilipai_white",
    "icon_bilipai_monet"
)

fun normalizeAppIconKey(rawKey: String?): String {
    val key = rawKey?.trim().orEmpty()
    if (key.isEmpty()) return DEFAULT_APP_ICON_KEY

    return when (key) {
        "default", "Blue Snow Maid", "蓝雪女仆" -> "icon_blue_snow_maid"
        "Blue Snow Maid Announcement", "蓝雪女仆·喇叭", "蓝雪女仆喇叭" -> "icon_blue_snow_maid_announcement"
        "Blue Snow Maid Front", "蓝雪女仆·正面", "蓝雪女仆正面" -> "icon_blue_snow_maid_front"
        "3D" -> "icon_3d"
        "BiliPai", "bilipai", "Icon BiliPai" -> "icon_bilipai"
        "BiliPai Pink", "BiliPai 粉", "bilipai_pink" -> "icon_bilipai_pink"
        "BiliPai White", "BiliPai 白", "bilipai_white" -> "icon_bilipai_white"
        "BiliPai Monet", "BiliPai 莫奈", "bilipai_monet" -> "icon_bilipai_monet"
        else -> if (CANONICAL_APP_ICON_KEYS.contains(key)) key else DEFAULT_APP_ICON_KEY
    }
}
