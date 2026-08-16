// 文件路径: core/store/collection/CollectionSortMode.kt
package com.android.purebilibili.core.store.collection

/**
 * 阶段 6 前置：从 feature/video/ui/components/CollectionEpisodePolicy.kt
 * 抽出的合集排序枚举（纯值对象，零 feature 依赖），供设置存储层使用。
 */
enum class CollectionSortMode(val label: String) {
    ASCENDING("正序"),
    DESCENDING("倒序"),
    RECENT("最近观看")
}
