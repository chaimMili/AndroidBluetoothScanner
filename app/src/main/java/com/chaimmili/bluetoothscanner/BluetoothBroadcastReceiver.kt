package com.chaimmili.bluetoothscanner

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@AndroidEntryPoint
class BluetoothBroadcastReceiver @Inject constructor(
    @Named("BroadcastResult") val broadcastResult: MutableStateFlow<Intent?>
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        broadcastResult.value = intent
    }
}


@InstallIn(SingletonComponent::class)
@Module
object BroadcastResultModule {

    @Provides
    @Singleton
    @Named("BroadcastResult")
    fun provideBroadcastResult(): MutableStateFlow<Intent?> = MutableStateFlow(null)
}