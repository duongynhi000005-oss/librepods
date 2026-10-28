package me.kavishdevar.librepods.privileged

import android.annotation.SuppressLint
import android.app.ActivityThread
import android.bluetooth.BluetoothManager
import android.content.Context
import android.net.LocalServerSocket
import android.os.Looper
import android.util.Log

object Main {
    @SuppressLint("MissingPermission")
    @JvmStatic
    fun main(args: Array<String>) {
        Log.i("LibrePodsPrivileged", "Starting LibrePods Privileged Service daemon")

        @Suppress("DEPRECATION")
        Looper.prepareMainLooper()

        ActivityThread.initializeMainlineModules()

        val context = ActivityThread.systemMain().systemContext as Context
        val adapter = context.getSystemService(BluetoothManager::class.java).adapter

        check(adapter != null) { "Bluetooth adapter unavailable" }
        check(adapter.isEnabled) { "Bluetooth adapter not enabled" }

        val service = PrivilegedServiceImpl(context, adapter)
        Log.i("LibrePodsPrivileged", "Privileged service initialized with UID ${service.serviceUid}")

        try {
            val serverSocket = LocalServerSocket("me.kavishdevar.librepods.privileged")
            Log.i("LibrePodsPrivileged", "Listening on LocalServerSocket 'me.kavishdevar.librepods.privileged'")

            Thread {
                while (true) {
                    try {
                        val socket = serverSocket.accept()
                        Log.i("LibrePodsPrivileged", "Accepted client connection")
                    } catch (e: Exception) {
                        Log.e("LibrePodsPrivileged", "Socket accept error", e)
                        break
                    }
                }
            }.start()
        } catch (e: Exception) {
            Log.w("LibrePodsPrivileged", "Could not bind local socket: " + str(e))
        }

        Log.i("LibrePodsPrivileged", "Privileged service daemon running in event loop")
        Looper.loop()
    }
}
