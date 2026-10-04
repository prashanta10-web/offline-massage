package com.offlinemessage.app

import android.annotation.SuppressLint
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var deviceListText: TextView
    private lateinit var pttButton: Button
    private lateinit var btnScan: Button
    private lateinit var btnMakeDiscoverable: Button

    private lateinit var bluetoothManager: LocalBluetoothManager
    private val discoveredDevicesList = mutableListOf()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            statusText.text = "Permissions granted!\nReady to scan for nearby devices."
        } else {
            statusText.text = "Permissions denied.\nCannot discover nearby devices without permissions."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        deviceListText = findViewById(R.id.deviceListText)
        pttButton = findViewById(R.id.pttButton)
        btnScan = findViewById(R.id.btnScan)
        btnMakeDiscoverable = findViewById(R.id.btnMakeDiscoverable)

        bluetoothManager = LocalBluetoothManager(this)

        btnScan.setOnClickListener {
            if (hasAllPermissions()) {
                startBluetoothDiscovery()
            } else {
                checkAndRequestPermissions()
            }
        }

        btnMakeDiscoverable.setOnClickListener {
            if (hasAllPermissions()) {
                bluetoothManager.makeDiscoverable(300)
                Toast.makeText(this, "Phone is visible to nearby devices for 5 minutes", Toast.LENGTH_SHORT).show()
            } else {
                checkAndRequestPermissions()
            }
        }

        pttButton.setOnClickListener {
            if (hasAllPermissions()) {
                statusText.text = "Voice button pressed"
            } else {
                checkAndRequestPermissions()
            }
        }

        checkAndRequestPermissions()
    }

    @SuppressLint("MissingPermission")
    private fun startBluetoothDiscovery() {
        if (!bluetoothManager.isBluetoothSupported) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show()
            return
        }

        statusText.text = "Scanning for nearby Bluetooth devices..."
        discoveredDevicesList.clear()
        deviceListText.text = "Scanning..."

        bluetoothManager.startDiscovery { device ->
            val deviceName = device.name ?: "Unknown Device"
            val deviceAddress = device.address
            val entry = "\(deviceName (\)deviceAddress)"

            if (!discoveredDevicesList.contains(entry)) {
                discoveredDevicesList.add(entry)
                runOnUiThread {
                    deviceListText.text = discoveredDevicesList.joinToString("\n")
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothManager.stopDiscovery()
    }

    private fun getRequiredPermissions(): Array {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.RECORD_AUDIO
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
        }

        return permissions.toTypedArray()
    }

    private fun hasAllPermissions(): Boolean {
        return getRequiredPermissions().all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun checkAndRequestPermissions() {
        if (!hasAllPermissions()) {
            statusText.text = "Requesting required permissions..."
            requestPermissionLauncher.launch(getRequiredPermissions())
        } else {
            statusText.text = "All required permissions granted!\nReady to scan for nearby devices."
        }
    }
}
