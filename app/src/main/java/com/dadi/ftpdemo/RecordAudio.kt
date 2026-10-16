package com.dadi.ftpdemo

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.net.PrintCommandListener
import org.apache.commons.net.ftp.FTPFile
import java.io.File
import java.io.IOException
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RecordAudio : AppCompatActivity() {
    var formattedDate : String = ""
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var audioFilePath: String = ""
    lateinit var context : Context

    private val ftpManager = FtpManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.record_audio)
        context = this
        val recordButton: Button = findViewById(R.id.record_button)

        // Request permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions()
        }

        recordButton.setOnClickListener {
            if (isRecording) {
                stopRecording()
                recordButton.text = "Start Recording"
                Toast.makeText(this, "Recording stopped, file saved at $audioFilePath", Toast.LENGTH_LONG).show()
            } else {
                startRecording()
                recordButton.text = "Stop Recording"
                Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show()
            }
        }

        val recordings : Button = findViewById(R.id.recordings)
        recordings.setOnClickListener {
            startActivity(Intent(this,AllRecording::class.java))
        }
    }

    // Start recording audio
    private fun startRecording() {
        // Prepare the file path where audio will be saved
       /* val audioDirectory = File(Environment.getExternalStorageDirectory().absolutePath + "/MyAudioFiles")
        if (!audioDirectory.exists()) {
            audioDirectory.mkdirs() // Create the folder if it doesn't exist
        }
        audioFilePath = "${audioDirectory.absolutePath}/audio_${System.currentTimeMillis()}.3gp"*/

        val audioDirectory = getExternalFilesDir(null) // App-specific directory for external storage
        if (audioDirectory != null && !audioDirectory.exists()) {
            audioDirectory.mkdirs() // Create the directory if it doesn't exist
        }
        val dateFormat = SimpleDateFormat("ddMMyyHHmmss", Locale.getDefault())
        val currentTime = Date()
         formattedDate = dateFormat.format(currentTime)
        // Create the file path
        audioFilePath = "${audioDirectory?.absolutePath}/audio_${formattedDate}.wav"

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setOutputFile(audioFilePath)

            try {
                prepare()
                start()
                isRecording = true
            } catch (e: IOException) {
                System.out.println("Record audio exception "+e.message.toString())
                e.printStackTrace()
            }
        }
    }

    // Stop recording audio
    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
            isRecording = false
            pushtolive(audioFilePath.toString())
            System.out.println("Record audio file path "+audioFilePath.toString())
        }
        mediaRecorder = null
    }

    // Handle permission request
    private fun requestPermissions() {
        if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), REQUEST_RECORD_AUDIO_PERMISSION)
        }
    }

    // Cleanup when activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        if (mediaRecorder != null) {
            mediaRecorder?.release()
            mediaRecorder = null
        }
    }

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }

    public fun pushtolive( url: String){
        System.out.println("local url.. "+url.toString())
        val ftpServer = "Server Address"
        val ftpPort = 21  // Default FTP port is 21
        val ftpUser = "anonymous"  // If no username is provided, you can use "anonymous"
        val ftpPassword = ""
        val remoteFolder = "Test"
        //val remoteFolder = "TestFTPFolder"
        val localFilePath =  url
        val serverFileName = formattedDate+".wav"
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
                        Toast.makeText(this@RecordAudio, "File uploaded and downloaded successfully!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@RecordAudio, "Failed to upload/download files", Toast.LENGTH_LONG).show()
                    }
                }
                // Disconnect when done
                //ftpManager.disconnect()

              /*  val files: Array<FTPFile>? = ftpManager.listFilesInDirectory(remoteFolder)

                withContext(Dispatchers.Main) {
                    if (files != null && files.isNotEmpty()) {
                        val fileList = files.joinToString(separator = "\n") { it.name }
                        System.out.println("All Data files "+fileList.toString())
                        Toast.makeText(this@RecordAudio, "Files in root folder:\n$fileList", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@RecordAudio, "No files found in root folder", Toast.LENGTH_LONG).show()
                    }
                }*/

                // Perform other actions like upload/download if needed

                // Disconnect when done
                ftpManager.disconnect()
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@RecordAudio, "Failed to connect to FTP server", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}