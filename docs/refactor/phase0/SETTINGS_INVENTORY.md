# 阶段 0：设置项清单与 schema 冻结

日期：2026-08-16。本清单是阶段 2"设置 schema 冻结"与"设置持久化兼容测试"的依据。

## 1. 持久化架构

- **真值（truth）**：单文件 DataStore `settings_prefs`（`core/store/SettingsManager.kt:68`，`internal val Context.settingsDataStore`），所有 Flow 读取均来自它。
- **影子缓存**：`SettingsPrefsCache.kt` 对"需要同步读取"的分组双写 SharedPreferences（约 25 个 SP 文件，如 `auto_play_cache`、`theme_cache`、`mini_player`、`quality_settings`、`feed_api`、`privacy_mode`、`playback_speed_cache` 等），经 `SerializedSettingsCacheWriter` + `commit()` 保证一致性。
- **独立 DataStore**：`webdav_backup_prefs`（`feature/settings/webdav/WebDavBackupStore.kt`）。
- **独立 SP（非设置类）**：`multi_account_sessions`、`token_backup_sp`、`wbi_keys_sp`。
- **子模块拆分**：播放速度/音质 → `core/store/player/PlayerSettingsStore.kt`；导航 → `core/store/navigation/NavigationSettingsStore.kt`；首页合并流 → `core/store/home/HomeSettingsStore.kt`。
- 真值 DataStore 直接字符串 key **约 232 个** + scoped 派生弹幕 key **约 47 个**（`danmaku_portrait_*`/`danmaku_landscape_*`，读时优先 scoped 回退全局）+ 拆分 store key 4 个 + WebDAV 5 个。

## 2. 设置 key 总清单（按分组，key 名冻结）

### 播放/解码（`SettingsManager.kt:912-1115` 一带）
auto_play、playback_completion_behavior、hw_decode、bg_play（**废弃**，被 mini_player_mode 取代）、gesture_sensitivity、slide_volume_brightness_enabled、set_system_brightness、pip_no_danmaku、danmaku_cloud_sync_enabled、show_player_cast_button、show_video_follow_button、player_progress_placement、double_tap_seek_enabled、seek_forward_seconds、seek_backward_seconds、long_press_speed、long_press_speed_hint_close_enabled、long_press_speed_hint_hidden、long_press_speed_lock_enabled、long_press_speed_lock_hint_shown、long_press_speed_hint_scale、long_press_speed_hint_alpha、two_finger_vertical_speed_enabled、two_finger_horizontal_speed_enabled、hi_res_long_press_compat_hint_shown、subtitle_vertical_offset_fraction、subtitle_portrait_vertical_offset_fraction、default_playback_speed、remember_last_playback_speed、last_playback_speed、comment_default_sort_mode、comment_fraud_detection_enabled、comment_member_decorations_enabled、image_preview_long_press_save_enabled、stop_playback_on_exit、background_playback_enabled、audio_focus_enabled、video_ai_summary_entry_enabled、video_note_enabled、video_note_default_collapsed、video_info_default_expanded、click_to_play、resume_playback_prompt_enabled、space_played_video_locate_prompt_enabled、external_playlist_auto_continue

### 外观/主题/字体/启动
theme_mode_v2、dark_theme_style_v1、app_language_v1、single_choice_presentation、app_font_size_preset、app_font_file_name、app_font_display_name、app_ui_scale_preset、app_dpi_override_percent、app_list_item_style、splash_wallpaper_uri、splash_wallpaper_history、splash_random_pool_uris、splash_enabled、splash_random_enabled、splash_icon_animation_enabled、splash_alignment_mobile、splash_alignment_tablet

### 首页/列表/动态（`core/store/home/HomeSettingsStore.kt` 合并流）
header_blur_enabled（legacy→home_header_blur_mode）、home_header_blur_mode、header_collapse_enabled（legacy→home_header_collapse_mode）、home_header_collapse_mode、common_list_header_collapse_mode、home_top_layout_order、display_mode、grid_column_count、home_feed_card_width_preset、home_feed_card_style、home_hero_carousel_enabled、home_hero_carousel_autoplay_enabled、card_animation_enabled、card_transition_enabled、live_surface_card_transition_enabled、video_transition_realtime_blur_enabled、video_shared_transition_speed、video_shared_transition_custom_duration_millis、smart_visual_guard_enabled（**已下线**，getter 恒 false）、runtime_visual_guard_enabled、compact_video_stats_on_cover、low_quality_home_cover_in_data_saver、home_cover_glass_badges_visible（**已下线**）、home_info_glass_badges_visible（**已下线**）、home_card_badge_effect_mode（**已下线**）、home_wallpaper_uri、home_wallpaper_effect_mode、home_wallpaper_effect_scope、home_up_badges_visible、home_up_avatars_visible、home_video_duration_badges_visible（legacy→home_duration_style）、home_duration_style、top_tab_order、top_tab_visible_tabs、top_tab_label_mode、home_top_right_action、dynamic_tab_visible_tabs、dynamic_image_preview_text_visible、dynamic_all_tab_horizontal_user_list_visible、dynamic_top_bar_collapse_on_scroll、live_favorite_tags

### 导航/底栏/侧栏/返回
bottom_bar_floating、bottom_bar_label_mode、bottom_bar_blur_enabled、bottom_bar_search_enabled、bottom_bar_search_auto_expand_mode、bottom_bar_search_layout_mode、blur_intensity、bottom_bar_order、bottom_bar_visible_tabs、bottom_bar_item_colors、bottom_bar_visibility_mode、tablet_use_sidebar、sidebar_account_switcher_enabled、predictive_back_enabled、haptic_feedback_enabled、full_screen_swipe_back_enabled（NavigationSettingsStore）

### 隐私/追踪/更新
privacy_mode_enabled、privacy_content_authentication_enabled、crash_tracking_consent_shown、crash_tracking_enabled、analytics_enabled、auto_check_app_update、app_update_channel、hide_triple_button、hide_cache_button

### Feed/实验
feed_api_type、incremental_timeline_refresh、home_refresh_count、exp_auto_1080p、exp_auto_skip_op_ed、exp_prefetch_video、exp_double_tap_like

### 播放器交互/全屏/画质
portrait_fullscreen_enabled、vertical_video_ratio、triple_jump_enabled、auto_rotate_enabled、wifi_default_quality、mobile_default_quality、video_codec_preference、video_second_codec_preference、audio_quality_preference、subscribed_collection_ids、collection_sort_preferences、auto_highest_quality、bili_directed_traffic、sponsor_block_enabled、sponsor_block_auto_skip、mini_player_mode、swipe_hide_player（legacy→portrait_player_collapse_mode）、portrait_player_collapse_mode、pause_on_player_collapse、portrait_swipe_to_fullscreen、center_swipe_to_fullscreen、inline_swipe_seek_seconds、fullscreen_swipe_seek_enabled、fullscreen_swipe_seek_seconds、fullscreen_gesture_reverse、hide_video_page_status_bar、portrait_letterbox_ambient_haze、tablet_comment_panel_width_preset、auto_enter_fullscreen、auto_exit_fullscreen（legacy→auto_exit_fullscreen_mode）、auto_exit_fullscreen_mode、show_fullscreen_lock_button、show_fullscreen_screenshot_button、app_gesture_screenshot_enabled、app_screenshot_gesture_mode、app_screenshot_capture_mode、show_fullscreen_battery_level、show_fullscreen_time、show_fullscreen_action_items、show_online_count、comment_collapsed_reply_preview_limit、player_diagnostic_logging_enabled、dash_segment_requests_enabled、quality_switch_failure_dialog_enabled、quality_switch_failure_dialog_once_enabled、quality_switch_failure_dialog_shown、subtitle_auto_preference、bottom_progress_behavior、progress_peak_danmaku_enabled、horizontal_adaptation_enabled、fullscreen_mode、fullscreen_aspect_ratio

### 下载/存储/个人中心
download_path、download_export_tree_uri、image_save_tree_uri、data_saver_mode、profile_bg_uri、profile_bg_scale_mobile、profile_bg_scale_tablet、profile_bg_offset_x_mobile、profile_bg_offset_x_tablet、profile_bg_alignment_mobile、profile_bg_alignment_tablet

### 弹幕全局（`SettingsManager.kt:3009-3046`）
danmaku_enabled、danmaku_opacity、danmaku_font_scale、danmaku_speed、danmaku_area、danmaku_font_weight、danmaku_stroke_width、danmaku_line_height、danmaku_scroll_duration_seconds、danmaku_static_duration_seconds、danmaku_scroll_fixed_velocity、danmaku_static_to_scroll、danmaku_massive_mode、danmaku_allow_scroll、danmaku_allow_top、danmaku_allow_bottom、danmaku_allow_colorful、danmaku_allow_special、danmaku_block_attention_commands、danmaku_smart_occlusion、danmaku_fullscreen_panel_width_mode、danmaku_block_rules、danmaku_merge_duplicates、danmaku_duplicate_merge_window_ms、danmaku_duplicate_merge_count_threshold、danmaku_send_color、danmaku_send_mode、danmaku_send_font_size、danmaku_defaults_version（当前期望 5）、home_visual_defaults_version（当前期望 3）
scoped 派生（23 后缀 × 竖/横屏，读时回退全局）：enabled、opacity、font_scale、speed、area、font_weight、stroke_width、line_height、scroll_duration_seconds、static_duration_seconds、scroll_fixed_velocity、static_to_scroll、massive_mode、allow_scroll、allow_top、allow_bottom、allow_colorful、allow_special、smart_occlusion、block_rules、merge_duplicates、duplicate_merge_window_ms、duplicate_merge_count_threshold + `danmaku_portrait_display_area_mode`

### 拆分 store / 独立 DataStore
- PlayerSettingsStore：default_audio_quality、preferred_player_volume、player_insight_mode（含 legacy `show_stats` 兼容，旧 SP `app_prefs`）
- NavigationSettingsStore：full_screen_swipe_back_enabled、tablet_use_sidebar、sidebar_account_switcher_enabled、predictive_back_enabled（与 SettingsManager 双处定义同 key）
- WebDavBackupStore（`webdav_backup_prefs`）：webdav_base_url、webdav_username、webdav_password、webdav_remote_dir、webdav_enabled

### 注释掉/勿复用
`unlock_high_quality`（`SettingsManager.kt:1011` 注释行，已回滚）

## 3. 设置页 UI 结构（8 大类）

`SettingsRootCategoryPolicy.kt`：外观与主题 / 播放与画质 / 首页与推荐 / 导航与交互 / 隐私与权限 / 存储与备份 / 插件与扩展 / 系统与关于。屏幕映射 `SettingsNavHierarchyPolicy.kt` + `SettingsSections.kt`；另有 3 个 deprecated 分类仅用于恢复旧导航状态。主屏幕：AppearanceSettingsScreen(1284 行)、PlaybackSettingsScreen(2323 行)、BottomBarSettingsScreen(1042 行)、AnimationSettingsScreen(378 行)、SettingsScreen、SettingsSearchScreen、PluginsScreen、SettingsShareScreen、WebDavBackupScreen。分组→key 映射见审计报告（略）。

## 4. Schema 冻结规则（阶段 2 起生效）

1. **key 只增不改**：重构期间不重命名/删除任何运行时 key；新 key 只增。已存在的 legacy 回退逻辑（theme_mode_v2、home_header_blur_mode、portrait_player_collapse_mode、auto_exit_fullscreen_mode、home_duration_style、danmaku scoped、mini_player_mode、player_insight_mode 等）原样保留。
2. **锁定字符串编码格式**：逗号分隔（top_tab_order/visible_tabs、bottom_bar_order/visible_tabs、subscribed_collection_ids）、`ID:idx` 对（bottom_bar_item_colors，含旧别名 normalizeBottomBarColorItemId）、JSON（live_favorite_tags、splash_wallpaper_history、splash_random_pool_uris、collection_sort_preferences）。阶段 2 兼容测试覆盖这些解析。
3. **双写影子缓存必须保持同步**：DataStore key ↔ SP 影子 key 同名不同值（auto_play↔auto_play_cache、click_to_play↔click_to_play_enabled、theme_mode_v2↔theme_cache、default_playback_speed↔playback_speed_cache 等）是既有事实，任何一处双写遗忘都会造成 Flow 读与同步读分叉。新增/修改设置时必须在 `SettingsPrefsCache` 注册对应影子 key。
4. **枚举编码锁定**：同一设置要么存 enum `value`(Int) 要么存 `name`(String)，混合现状（blur_intensity/app_list_item_style/player_insight_mode 存 name，其余存 Int）冻结，新 key 沿用该域既有习惯。
5. **版本化默认值迁移沿用现有模式**：`danmaku_defaults_version`(→5)、`home_visual_defaults_version`(→3) 是"手动 default 迁移"（非 DataStore PRODUCE migration）；新增此类迁移必须同时升版本号。
6. **已下线 key 只保留字段不读旧值**：smart_visual_guard_enabled、home_card_badge_effect_mode、home_cover_glass_badges_visible、home_info_glass_badges_visible。
7. `SettingsManagerSizeRatchetTest`（行数棘轮，只减不增）约束拆分节奏：阶段 2 拆分 store 时同步更新。
8. `shareableSettingDefinitions`（`SettingsManager.kt:5813-6026`，约 140 个可分享 key）为设置导入导出白名单；增删 key 需同步维护。

## 5. 关键同步读取（SP 影子缓存）消费方密度

isPrivacyModeEnabledSync(8)、getStopPlaybackOnExitSync(6)、getClickToPlaySync(6)、getVideoSecondCodecSync(5)、getMiniPlayerModeSync(5)、getVideoCodecSync(4)、getSponsorBlockAutoSkip(4)、getVideoNoteEnabledSync(3)、getTripleJumpEnabled(3)、getFeedApiTypeSync(3)、getHideVideoPageStatusBarSync(2)、getPreferredPlaybackSpeedSync(3)、getAudioFocusEnabledSync(3)、getBiliDirectedTrafficEnabledSync(2)、getBackgroundPlaybackEnabledSync(2)、getDataSaverModeSync(2)、getCommentDefaultSortModeSync(3)、getSplash*Sync(3+) 等——这些是阶段 2 接缝测试（旧 schema 可读、默认值正确）的优先覆盖对象。
