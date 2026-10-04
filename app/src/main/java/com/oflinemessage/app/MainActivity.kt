package com.offlinemessage.app

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
    private lateinit var pttButton: Button

    // Launcher to request multiple runtime permissions together
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            statusText.text = "All required permissions granted!\nReady to discover devices."
            Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show()
        } else {
            statusText.text = "Permissions denied.\nApp cannot function offline without permissions."
            Toast.makeText(this, "Permissions Required", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        pttButton = findViewById(R.id.pttButton)

        pttButton.setOnClickListener {
            if (hasAllPermissions()) {
                statusText.text = "Voice button pressed (Permissions OK)"
            } else {
                statusText.text = "Please grant permissions first."
                checkAndRequestPermissions()
            }
        }

        // Prompt for permissions immediately upon launching
        checkAndRequestPermissions()
    }

    private fun getRequiredPermissions(): Array {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.RECORD_AUDIO
        )

        // Android 12 (API 31) and higher require dedicated Bluetooth & Nearby permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        }

        // Android 13 (API 33) and higher require Nearby Wi-Fi permission for Wi-Fi Direct
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
            statusText.text = "Requesting permissions..."
            requestPermissionLauncher.launch(getRequiredPermissions())
        } else {
            statusText.text = "All required permissions granted!\nReady to discover devices."
        }
    }
}
