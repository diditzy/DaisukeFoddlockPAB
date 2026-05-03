package com.example.daisukefoddlock10.util

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SyncManager(
    private val onSync: suspend () -> Unit
) : LifecycleEventObserver {
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_RESUME) {
            scope.launch {
                onSync()
            }
        }
    }
}
