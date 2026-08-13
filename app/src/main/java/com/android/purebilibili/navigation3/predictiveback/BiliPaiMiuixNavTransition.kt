// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 BiliPai contributors
package com.android.purebilibili.navigation3.predictiveback

import top.yukonga.miuix.kmp.nav.transition.NavTransition

/**
 * 极简版：预测返回收敛为单一固定转场，不再提供 AOSP / SCALE / CLASSIC / 实时模糊等冗余风格。
 *
 * 返回与预测返回统一走 [NoPredictiveBackTransition]（横向滑动 + 基础景深），
 * 保持系统手势与提交动画同向，避免「预览右滑、松手提交左滑」的换向撕裂。
 */
internal fun biliPaiMiuixNavTransition(
    isLightBackground: Boolean,
): NavTransition = NoPredictiveBackTransition
