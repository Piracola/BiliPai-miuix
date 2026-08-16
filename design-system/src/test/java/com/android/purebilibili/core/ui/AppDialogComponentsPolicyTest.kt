package com.android.purebilibili.core.ui

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppDialogComponentsPolicyTest {

    @Test
    fun `dialog actions stay content sized inside material alert dialogs`() {
        val policy = resolveDialogActionLayoutPolicy()

        assertFalse(policy.expandToContainer)
    }

    @Test
    fun contentDialogLayout_disablesPlatformDefaultWidthAndCapsMaxWidth() {
        val compact = resolveAppCompactContentDialogLayoutPolicy()
        val standard = resolveAppContentDialogLayoutPolicy()
        val expanded = resolveAppExpandedContentDialogLayoutPolicy()

        assertFalse(compact.usePlatformDefaultWidth)
        assertFalse(standard.usePlatformDefaultWidth)
        assertFalse(expanded.usePlatformDefaultWidth)
        assertEquals(360, compact.maxWidthDp)
        assertEquals(420, standard.maxWidthDp)
        assertEquals(560, expanded.maxWidthDp)
        assertTrue(compact.maxWidthDp <= standard.maxWidthDp)
        assertTrue(standard.maxWidthDp <= expanded.maxWidthDp)
    }

    @Test
    fun contentDialogProperties_forceTabletSafeWidthFlag() {
        val properties = resolveAppContentDialogProperties(
            usePlatformDefaultWidth = false,
        )
        assertFalse(properties.usePlatformDefaultWidth)
    }
}
