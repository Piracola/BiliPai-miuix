package com.android.purebilibili.feature.space

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import kotlin.test.Test
import kotlin.test.assertNotNull

class SpaceViewModelConstructorTest {
    @Test
    fun supportsSavedStateDefaultViewModelFactory() {
        assertNotNull(SpaceViewModel::class.java.getConstructor(Application::class.java, SavedStateHandle::class.java))
    }
}
