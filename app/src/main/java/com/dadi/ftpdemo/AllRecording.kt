package com.dadi.ftpdemo

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTPFile

class AllRecording : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var audioAdapter: AudioAdapter
    private val remoteFolder = "Test" // Change to your FTP folder
    private val ftpManager = FtpManager()
    lateinit var context: Context
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.all_recording)
        context = this
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        pushtolive()
    }

    override fun onDestroy() {
        super.onDestroy()
        ftpManager.disconnect()
    }


    public fun pushtolive(){
        val ftpServer = "Server Address"
        val ftpPort = 21  // Default FTP port is 21
        val ftpUser = "anonymous"  // If no username is provided, you can use "anonymous"
        val ftpPassword = ""
        val remoteFolder = "Test"
        //val remoteFolder = "TestFTPFolder"
         val serverFileName = "uploaded_samplefile1.txt"
        val remoteFileToDownload = "remote_samplefile1.txt"

        // Establish FTP connection and perform actions using coroutines

        CoroutineScope(Dispatchers.IO).launch {
            val connected = ftpManager.connectToFtpServer(ftpServer, ftpPort, ftpUser, ftpPassword)
            if (connected) {
                val files: Array<FTPFile>? = ftpManager.listFilesInDirectory(remoteFolder)

                withContext(Dispatchers.Main) {
                    if (files != null && files.isNotEmpty()) {
                        val audioFiles = files.joinToString(separator = "\n") { it.name }
                        System.out.println("All Data files "+audioFiles.toString())
                        Toast.makeText(this@AllRecording, "Files in root folder:\n$audioFiles", Toast.LENGTH_LONG).show()
                        if (audioFiles.isNotEmpty()) {
                            audioAdapter = AudioAdapter(this@AllRecording, listOf(audioFiles))
                            recyclerView.adapter = audioAdapter
                        } else {
                            Toast.makeText(this@AllRecording, "No .wav files found", Toast.LENGTH_LONG).show()
                        }

                    } else {
                        Toast.makeText(this@AllRecording, "No files found in root folder", Toast.LENGTH_LONG).show()
                    }
                }

                // Perform other actions like upload/download if needed

                // Disconnect when done
                ftpManager.disconnect()
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AllRecording, "Failed to connect to FTP server", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}