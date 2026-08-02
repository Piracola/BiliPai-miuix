package com.android.purebilibili.feature.dynamic.components

import com.android.purebilibili.data.model.response.DynamicDisputeModule
import com.android.purebilibili.data.model.response.DynamicFoldModule
import com.android.purebilibili.data.model.response.DynamicTagModule

/**
 * 卡片附加模块的渲染策略：相关动态折叠条、置顶标、风险提示条。
 * 对齐 PiliPlus 的 moduleFold / moduleTag / moduleDispute 处理。
 */

// 置顶标：module_tag 存在且 text 非空（B 站固定返回 "置顶"）时显示。
internal fun shouldShowDynamicPinnedTag(tag: DynamicTagModule?): Boolean =
    !tag?.text.isNullOrBlank()

// 相关动态折叠条：statement 非空（如 "展开3条相关动态"）才渲染。
internal fun resolveDynamicFoldStatement(fold: DynamicFoldModule?): String? {
    if (fold == null) return null
    val statement = fold.statement.trim()
    return statement.ifEmpty { null }
}

// 风险提示条：title 或 desc 任一非空即渲染。
internal fun shouldShowDynamicDispute(dispute: DynamicDisputeModule?): Boolean {
    if (dispute == null) return false
    return dispute.title.isNotBlank() || dispute.desc.isNotBlank()
}
