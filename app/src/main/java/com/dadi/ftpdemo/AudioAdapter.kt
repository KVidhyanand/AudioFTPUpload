package com.dadi.ftpdemo
import android.content.Context
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AudioAdapter(
    private val context: Context,
    private val audioFiles: List<String>
) : RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {
    private val ftpManager = FtpManager()

    class AudioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val fileName: TextView = view.findViewById(R.id.fileName)
        val playButton: Button = view.findViewById(R.id.playButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.audio_item, parent, false)
        return AudioViewHolder(view)
    }

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        val audioFileName = audioFiles[position]
        holder.fileName.text = audioFileName

        holder.playButton.setOnClickListener {
            val localFilePath = "${context.getExternalFilesDir(null)}/$audioFileName"
            val ftpServer = "Server Address"
            val ftpPort = 21  // Default FTP port is 21
            val ftpUser = "anonymous"  // If no username is provided, you can use "anonymous"
            val ftpPassword = ""
            val remoteFolder = "Test"
            // Download and play the audio file
            CoroutineScope(Dispatchers.IO).launch {
                val connected = ftpManager.connectToFtpServer(ftpServer, ftpPort, ftpUser, ftpPassword)

                if (connected) {
                    val remoteFilePath = "Test/$audioFileName"
                    val isDownloaded = ftpManager.downloadFile(remoteFilePath, localFilePath)
                    ftpManager.disconnect()

                    withContext(Dispatchers.Main) {
                        if (isDownloaded) {
                            playAudio(localFilePath)
                        } else {
                            Toast.makeText(context, "Failed to download audio file", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Failed to connect to FTP server", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = audioFiles.size

    // Play the audio file
    private fun playAudio(filePath: String) {
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(filePath)
        mediaPlayer.prepare()
        mediaPlayer.start()
    }
}
