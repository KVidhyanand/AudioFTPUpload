package com.dadi.ftpdemo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.PrintCommandListener
import java.io.PrintWriter

class MainActivity : AppCompatActivity() {
    private val ftpManager = FtpManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // FTP connection details
        // FTP connection details
        val ftpServer = "Server Address"
        val ftpPort = 21  // Default FTP port is 21
        val ftpUser = "anonymous"  // If no username is provided, you can use "anonymous"
        val ftpPassword = ""
        val remoteFolder = "Test"
        //val remoteFolder = "TestFTPFolder"
        val localFilePath = "/storage/emulated/0/Download/samplefile.txt"
        val serverFileName = "uploaded_samplefile1.txt"
        val remoteFileToDownload = "remote_samplefile1.txt"

        // Establish FTP connection and perform actions using coroutines

        CoroutineScope(Dispatchers.IO).launch {
            val connected = ftpManager.connectToFtpServer(ftpServer, ftpPort, ftpUser, ftpPassword)
            if (connected) {
                System.out.println("FTP connection "+connected);
                // Upload a file to the remote FTP folder

                ftpManager.ftpClient.addProtocolCommandListener(PrintCommandListener(PrintWriter(System.out), true))

                val uploadSuccess = ftpManager.uploadFile(localFilePath, serverFileName, remoteFolder)

                // Download a file from the remote FTP folder
                // val downloadSuccess = ftpManager.downloadFile(remoteFileToDownload, localFilePath, remoteFolder)

                withContext(Dispatchers.Main) {
                    if (uploadSuccess ) {
                        Toast.makeText(this@MainActivity, "File uploaded and downloaded successfully!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Failed to upload/download files", Toast.LENGTH_LONG).show()
                    }
                }
                // Disconnect when done
                //ftpManager.disconnect()

                val files: Array<FTPFile>? = ftpManager.listFilesInDirectory(remoteFolder)

                withContext(Dispatchers.Main) {
                    if (files != null && files.isNotEmpty()) {
                        val fileList = files.joinToString(separator = "\n") { it.name }
                        System.out.println("All Data files "+fileList.toString())
                        Toast.makeText(this@MainActivity, "Files in root folder:\n$fileList", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@MainActivity, "No files found in root folder", Toast.LENGTH_LONG).show()
                    }
                }

                // Perform other actions like upload/download if needed

                // Disconnect when done
                ftpManager.disconnect()
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Failed to connect to FTP server", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}