package com.dadi.ftpdemo
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class FtpManager {

    val ftpClient = FTPClient()

    // Connect to the FTP Server
    fun connectToFtpServer(server: String, port: Int, user: String, password: String): Boolean {
        return try {
            ftpClient.connect(server, port)
            val success = ftpClient.login(user, password)
            ftpClient.enterLocalPassiveMode() // Required for some servers
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
            success
        } catch (ex: IOException) {
            ex.printStackTrace()
            false
        }
    }

    // Upload a file to FTP Server (Upload to TestFTPFolder)
    fun uploadFile(localFilePath: String, serverFileName: String, remoteFolder: String = ""): Boolean {
        return try {
            val inputStream = FileInputStream(localFilePath)
            val remotePath = if (remoteFolder.isNotEmpty()) "$remoteFolder/$serverFileName" else serverFileName
            val success = ftpClient.storeFile(remotePath, inputStream)
            inputStream.close()
            success
        } catch (ex: IOException) {
            ex.printStackTrace()
            false
        }
    }

    fun listFiles(remoteFolder: String): List<String> {
        return try {
            ftpClient.changeWorkingDirectory(remoteFolder)
            ftpClient.listFiles().filter { it.name.endsWith(".wav") }.map { it.name }
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun downloadFile(remoteFilePath: String, localFilePath: String): Boolean {
        return try {
            val outputStream = FileOutputStream(localFilePath)
            ftpClient.retrieveFile(remoteFilePath, outputStream)
            outputStream.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    // Download a file from FTP Server (Download from TestFTPFolder)
    fun downloadFile(remoteFilePath: String, localFilePath: String, remoteFolder: String = ""): Boolean {
        return try {
            val outputStream = FileOutputStream(localFilePath)
            val remotePath = if (remoteFolder.isNotEmpty()) "$remoteFolder/$remoteFilePath" else remoteFilePath
            val success = ftpClient.retrieveFile(remotePath, outputStream)
            outputStream.close()
            success
        } catch (ex: IOException) {
            ex.printStackTrace()
            false
        }
    }

    // NEW: List files in a remote folder
    fun listFilesInDirectory(remoteFolder: String = ""): Array<FTPFile>? {
        return try {
            val remotePath = if (remoteFolder.isNotEmpty()) remoteFolder else "/"
            ftpClient.listFiles(remotePath)
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }

    // Disconnect from the FTP Server
    fun disconnect() {
        try {
            if (ftpClient.isConnected) {
                ftpClient.logout()
                ftpClient.disconnect()
            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }
}
