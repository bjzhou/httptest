package com.hinnka.httptest

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.hinnka.httptest.dns.DnsClient
import kotlinx.coroutines.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.coroutines.resume

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.button).setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val ping = tcpTest("connect.rom.miui.com")
                findViewById<TextView>(R.id.textView2).text = ping.toString()
            }
        }

        findViewById<Button>(R.id.button2).setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val ping = udpTest("114.114.114.114", 53)
                findViewById<TextView>(R.id.textView2).text = ping.toString()
            }
        }

        findViewById<Button>(R.id.button3).setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val ping = tcpTest("captive.apple.com")
                findViewById<TextView>(R.id.textView2).text = ping.toString()
            }
        }

        findViewById<Button>(R.id.button4).setOnClickListener {
            GlobalScope.launch(Dispatchers.Main) {
                val ping = udpTest("208.67.222.222", 5353)
                findViewById<TextView>(R.id.textView2).text = ping.toString()
            }
        }
    }

    suspend fun tcpTest(hostname: String): Long {
        return withContext(Dispatchers.IO) {
            try {
                val socket = Socket()
                socket.use {
                    val port = 80
                    val downloadRequest = """GET /generate_204 HTTP/1.1
Host: $hostname
Connection: keep-alive
Pragma: no-cache
Cache-Control: no-cache


    """
                    socket.soTimeout = 5000
                    val start = System.currentTimeMillis()
                    socket.connect(InetSocketAddress(hostname, port))
                    socket.getOutputStream().write(downloadRequest.toByteArray())
                    socket.getOutputStream().flush()
                    val ins = socket.getInputStream()
                    ins.read()
                    return@withContext System.currentTimeMillis() - start
                }
            } catch (e: Exception) {
                println("tcp error: $e")
            }
            return@withContext -1
        }
    }

    suspend fun udpTest(hostname: String, port: Int): Long {
        return withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { continuation ->
                val client = DnsClient(hostname, port, "www.microsoft.com") {
                    continuation.resume(it)
                }
                client.makeRequest()
            }
        }
    }
}