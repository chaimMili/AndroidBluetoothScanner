package com.chaimmili.bluetoothscanner

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class DevicesViewModel @Inject constructor(
    @Named("BroadcastResult") val broadcastResult: MutableStateFlow<Intent?>
) : ViewModel() {

    val discoveryState = MutableLiveData<BluetoothDiscoveryMonitor>()

    val currentDisplayingDevices = MutableLiveData<List<DeviceDetails>>()

    private val currentDiscoveryDevices = mutableListOf<DeviceDetails>()

    private var isFirstDiscovery = true

    init {
        broadcastResult.onEach { intent ->
            Log.i("Receiver Result", "${intent?.action}")
            Log.i("Receiver Data", "${intent?.data}")
            when (intent?.action) {
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    discoveryFinished()
                    discoveryState.value = BluetoothDiscoveryMonitor.FINISHED
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    discoveryState.value = BluetoothDiscoveryMonitor.STARTED
                }
                BluetoothDevice.ACTION_FOUND -> {
                    intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        ?.apply {
                            addNewDevice(DeviceDetails(name ?: "", address ?: ""))
                        }
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun addNewDevice(deviceDetails: DeviceDetails) {
        currentDiscoveryDevices.addToFirstPosition(deviceDetails)

        if (isFirstDiscovery)
            addDevicesToDisplayingList(currentDiscoveryDevices)
    }

    private fun MutableList<DeviceDetails>.addToFirstPosition(deviceDetails: DeviceDetails): Unit? =
        if (!this.any { it == deviceDetails })
            add(0, deviceDetails)
        else
            null

    private fun addDevicesToDisplayingList(devices: List<DeviceDetails>) {
        currentDisplayingDevices.value = devices
    }

    private fun discoveryFinished() {
        addDevicesToDisplayingList(currentDiscoveryDevices)
        currentDiscoveryDevices.clear()
        isFirstDiscovery = false
    }

}

enum class BluetoothDiscoveryMonitor {
    STARTED, FINISHED
}