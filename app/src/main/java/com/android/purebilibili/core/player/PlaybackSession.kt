package com.android.purebilibili.core.player

/**
 * 播放会话只读接缝（阶段 2 Core 接缝）。
 *
 * 当前播放会话的宿主是 feature 层的 MiniPlayerManager 全局单例。本接口把
 * 播放会话的**读取面**收敛到 Core 层：UI/其他 feature 需要了解当前播放状态时
 * 只依赖该接口，不感知宿主实现。宿主在阶段 4/5 通过只读视图暴露自身。
 *
 * 注意：本接口只读，不含播放控制副作用；控制面（play/pause/seek 等）仍在
 * 既有播放器路径上，避免在接缝阶段引入第二套播放控制入口。
 */
interface PlaybackSession {

    val isActive: Boolean

    val currentBvid: String?

    val currentPosition: Long

    val duration: Long

    val isPlaying: Boolean
}
