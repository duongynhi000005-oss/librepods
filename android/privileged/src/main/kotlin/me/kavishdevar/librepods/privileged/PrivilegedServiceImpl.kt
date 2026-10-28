package me.kavishdevar.librepods.privileged

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Process
import android.util.Log
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class PrivilegedServiceImpl(
    private val context: Context,
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

    override fun setConnectionPolicy(address: String, profile: Int, policy: Int): Boolean {
        return try {
            val device = adapter.getRemoteDevice(address)
            var result = false
            val latch = CountDownLatch(1)
            adapter.getProfileProxy(
                context,
                object : BluetoothProfile.ServiceListener {
                    override fun onServiceConnected(p: Int, proxy: BluetoothProfile) {
                        try {
                            val method = proxy.javaClass.getMethod(
                                "setConnectionPolicy",
                                BluetoothDevice::class.java,
                                Int::class.javaPrimitiveType
                            )
                            result = method.invoke(proxy, device, policy) as? Boolean ?: false
                            Log.d(TAG, "setConnectionPolicy profile=$p policy=$policy result=$result")
                        } catch (e: Exception) {
                            Log.e(TAG, "setConnectionPolicy failed for profile $p", e)
                        } finally {
                            adapter.closeProfileProxy(p, proxy)
                            latch.countDown()
                        }
                    }

                    override fun onServiceDisconnected(p: Int) {
                        latch.countDown()
                    }
                },
                profile
            )
            latch.await(2, TimeUnit.SECONDS)
            result
        } catch (e: Exception) {
            Log.e(TAG, "Error setting connection policy", e)
            false
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
