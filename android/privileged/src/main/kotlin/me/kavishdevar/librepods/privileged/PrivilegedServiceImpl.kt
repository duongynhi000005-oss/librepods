package me.kavishdevar.librepods.privileged

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.os.Process
import android.util.Log

class PrivilegedServiceImpl(
    private val adapter: BluetoothAdapter
) : ILibrePodsPrivilegedService.Stub() {

    override fun setBluetoothMetadata(address: String, key: Int, value: ByteArray): Boolean {
        return try {
            val device = adapter.getRemoteDevice(address)
            val method = BluetoothDevice::class.java.getDeclaredMethod(
                "setMetadata",
                Int::class.javaPrimitiveType,
                ByteArray::class.java
            )
            method.isAccessible = true
            val res = method.invoke(device, key, value) as? Boolean ?: false
            Log.d(TAG, "setMetadata key=$key for $address result=$res")
            res
        } catch (e: Exception) {
            Log.e(TAG, "Error setting metadata", e)
            false
        }
    }

    override fun getBluetoothMetadata(address: String, key: Int): ByteArray? {
        return try {
            val device = adapter.getRemoteDevice(address)
            val method = BluetoothDevice::class.java.getDeclaredMethod(
                "getMetadata",
                Int::class.javaPrimitiveType
            )
            method.isAccessible = true
            method.invoke(device, key) as? ByteArray
        } catch (e: Exception) {
            Log.e(TAG, "Error getting metadata", e)
            null
        }
    }

    override fun getServiceUid(): Int = Process.myUid()

    override fun destroy() {
        Log.i(TAG, "Privileged service destroying")
    }

    companion object {
        private const val TAG = "LibrePodsPrivileged"
    }
}
