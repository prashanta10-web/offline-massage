package com.offlinemessage.app

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

class LocalBluetoothManager(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        manager.adapter
    }

    val isBluetoothSupported: Boolean
        get() = bluetoothAdapter != null

    val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    private val discoveredDevices = mutableListOf()

    // BroadcastReceiver to capture devices found during Bluetooth scanning
    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        if (!discoveredDevices.contains(it)) {
                            discoveredDevices.add(it)
                            onDeviceFoundCallback?.invoke(it)
                        }
                    }
                }
            }
        }
    }

    private var onDeviceFoundCallback: ((BluetoothDevice) -> Unit)? = null

    @SuppressLint("MissingPermission")
    fun startDiscovery(onDeviceFound: (BluetoothDevice) -> Unit) {
        onDeviceFoundCallback = onDeviceFound
        discoveredDevices.clear()

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        context.registerReceiver(receiver, filter)

        bluetoothAdapter?.startDiscovery()
    }

    @SuppressLint("MissingPermission")
    fun stopDiscovery() {
        try {
            bluetoothAdapter?.cancelDiscovery()
            context.unregisterReceiver(receiver)
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered or already unregistered
        }
    }

    @SuppressLint("MissingPermission")
    fun makeDiscoverable(durationSeconds: Int = 300) {
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, durationSeconds)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(discoverableIntent)
    }
}
