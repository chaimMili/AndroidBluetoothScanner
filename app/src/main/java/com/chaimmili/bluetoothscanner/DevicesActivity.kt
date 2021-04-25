package com.chaimmili.bluetoothscanner

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.chaimmili.bluetoothscanner.databinding.ActivityDevicesBinding
import com.chaimmili.bluetoothscanner.extention.hasPermission
import com.chaimmili.bluetoothscanner.extention.toast
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.content.Intent
import android.location.LocationManager
import android.provider.Settings
import androidx.activity.result.ActivityResult
import com.chaimmili.bluetoothscanner.extention.displayAlertDialog

@AndroidEntryPoint
class DevicesActivity : AppCompatActivity() {

    private val devicesViewModel: DevicesViewModel by viewModels()

    @Inject
    lateinit var bluetoothBroadcastReceiver: BluetoothBroadcastReceiver

    private lateinit var binding: ActivityDevicesBinding
    private val availableDevicesAdapter = AvailableDevicesAdapter()
    private val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private lateinit var registerBluetoothEnableResult: ActivityResultLauncher<Intent>
    private lateinit var registerNavigationEnableResult: ActivityResultLauncher<Intent>

    companion object {
        const val REQUEST_LOCATION_PERMISSION_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDevicesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initRegistersForActivityResult()
        initView()
        initListeners()
    }

    private fun initRegistersForActivityResult() {
        registerBluetoothEnableResult = registerForActivityResult {
            if (it.resultCode == RESULT_OK) {
                startDiscovery()
            } else {
                toast("Enable bluetooth is required")
            }
        }
        registerNavigationEnableResult = registerForActivityResult {
            if (navigationEnable())
                startDiscovery()
            else
                toast("Enable navigation is required")
        }
    }

    private fun registerForActivityResult(function: (ActivityResult) -> Unit) =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            function(it)
        }

    private fun initView() {
        binding.devicesRecycler.apply {
            adapter = availableDevicesAdapter
            layoutManager = LinearLayoutManager(this@DevicesActivity)
            addItemDecoration(
                DividerItemDecoration(
                    this@DevicesActivity,
                    DividerItemDecoration.VERTICAL
                )
            )
        }
        binding.scannerBtn.setOnClickListener { handleBluetooth() }
    }

    private fun initListeners() {
        devicesViewModel.discoveryState.observe(this, { discoveryMode ->
            when (discoveryMode!!) {
                BluetoothDiscoveryMonitor.STARTED -> {
                    binding.scannerBtn.isInvisible = true
                    binding.progressBar.isVisible = true
                }
                BluetoothDiscoveryMonitor.FINISHED -> mBluetoothAdapter.startDiscovery()
            }
        })

        devicesViewModel.currentDisplayingDevices.observe(this, { devices ->
            availableDevicesAdapter.setDevices(devices)
        })
    }

    private fun handleBluetooth() {
        if (mBluetoothAdapter.isDiscovering) {
            mBluetoothAdapter.cancelDiscovery()
            return
        }

        if (mBluetoothAdapter.isEnabled)
            startDiscovery()
        else {
            registerBluetoothEnableResult.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        }
    }

    private fun startDiscovery() {
        if (hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION) &&
            hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !navigationEnable())
                buildAlertMessageNoGps()
            else
                if (mBluetoothAdapter.isEnabled && !mBluetoothAdapter.isDiscovering) {
                    beginDiscovery()
                }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                REQUEST_LOCATION_PERMISSION_CODE
            )
        }
    }

    private fun beginDiscovery() {
        registerReceiver(bluetoothBroadcastReceiver, IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        })
        mBluetoothAdapter.startDiscovery()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[grantResults.size - 1] == PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !navigationEnable())
                    buildAlertMessageNoGps()
                else
                    startDiscovery()
            } else
                toast("Location permission is necessary")
        }
    }

    private fun navigationEnable(): Boolean {
        val manager = getSystemService(LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun buildAlertMessageNoGps() {
        displayAlertDialog(
            "Enable Location",
            "For this android version you need to enable location for scan a bluetooth devices"
        ) {
            registerNavigationEnableResult.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothBroadcastReceiver)
    }
}