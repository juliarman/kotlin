/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.kotlin.android.tests.download

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.util.SystemInfo
import com.intellij.openapi.util.io.FileUtil
import org.jetbrains.kotlin.android.tests.PathManager
import org.jetbrains.kotlin.android.tests.run.RunUtils
import java.io.*
import java.net.URL
import java.net.URLConnection
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class SDKDownloader(private val pathManager: PathManager) {
    private val gradleZipPath: String

    init {
        gradleZipPath = pathManager.rootForDownload + "/gradle" + GRADLE_VERSION + ".zip"
    }

    private fun downloadGradle() {
        download(
            "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip",
            gradleZipPath
        )
    }

    fun downloadAndInstallPackages() {
        downloadGradle()
        unzipAll()
    }

    private fun unzipAll() {
        unzip(gradleZipPath, pathManager.dependenciesRoot)
    }

    private fun unzip(pathToFile: String, outputFolder: String) {
        println("Start unzipping: $pathToFile to $outputFolder")
        val pathToUnzip =
            if (outputFolder == pathManager.platformFolderInAndroidSdk) {
                outputFolder
            } else {
                outputFolder + "/" + FileUtil.getNameWithoutExtension(File(pathToFile))
            }
        if (File(pathToUnzip).listFiles() != null) {
            println("File was already unzipped: $pathToFile")
            return
        }
        try {
            val buf = ByteArray(1024)
            var zipEntry: ZipEntry? = null
            try {
                ZipInputStream(FileInputStream(pathToFile)).use { zipInputStream ->
                    zipEntry = zipInputStream.nextEntry
                    while (zipEntry != null) {
                        val entryName = zipEntry!!.name
                        var n: Int
                        val outputFile =
                            File("$outputFolder/$entryName")
                        if (zipEntry!!.isDirectory) {
                            outputFile.mkdirs()
                            zipInputStream.closeEntry()
                            zipEntry = zipInputStream.nextEntry
                            continue
                        } else {
                            val parentFile = outputFile.parentFile
                            if (parentFile != null && !parentFile.exists()) {
                                parentFile.mkdirs()
                            }
                            outputFile.createNewFile()
                        }
                        FileOutputStream(outputFile).use { fileOutputStream ->
                            while (zipInputStream.read(
                                    buf,
                                    0,
                                    1024
                                ).also { n = it } > -1
                            ) {
                                fileOutputStream.write(
                                    buf,
                                    0,
                                    n
                                )
                            }
                        }
                        zipInputStream.closeEntry()
                        zipEntry = zipInputStream.nextEntry
                    }
                }
            } catch (e: IOException) {
                if (zipEntry != null) {
                    System.err.println("Entry name: " + zipEntry!!.name)
                }
                e.printStackTrace()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        println("Finish unzipping: $pathToFile to $outputFolder")
    }

    companion object {
        private const val PLATFORM_TOOLS = "28.0.1"
        private const val SDK_TOOLS = "4333796" //"26.1.1";
        const val BUILD_TOOLS = "29.0.3"
        const val GRADLE_VERSION = "5.6.4"
        const val EMULATOR_TOOLS_VERSION = "5264690" //"28.0.23";
        const val COMMAND_LINE_TOOLS = "6200805_latest"
        private fun getDownloadUrl(prefix: String): String {
            val suffix: String
            suffix = if (SystemInfo.isWindows) {
                "windows"
            } else if (SystemInfo.isMac) {
                "mac"
            } else if (SystemInfo.isUnix) {
                "linux"
            } else {
                throw IllegalStateException("Your operating system isn't supported yet.")
            }
            return prefix + suffix
        }

        private val platformName: String
            private get() = if (SystemInfo.isWindows) {
                "windows"
            } else if (SystemInfo.isMac) {
                "darwin"
            } else if (SystemInfo.isUnix) {
                "linux"
            } else {
                throw IllegalStateException("Your operating system isn't supported yet.")
            }

        private fun download(urlString: String, output: String) {
            println("Start downloading: $urlString to $output")
            var outStream: OutputStream? = null
            val urlConnection: URLConnection
            val `is`: InputStream
            try {
                val Url: URL
                val buf: ByteArray
                var read: Int
                //int written = 0;
                Url = URL(urlString)
                val outputFile = File(output)
                outputFile.parentFile.mkdirs()
                if (outputFile.exists()) {
                    println("File was already downloaded: $output")
                    return
                }
                outputFile.createNewFile()
                val outputStream = FileOutputStream(outputFile)
                outStream = BufferedOutputStream(outputStream)
                urlConnection = Url.openConnection()
                `is` = urlConnection.getInputStream()
                buf = ByteArray(1024)
                while (`is`.read(buf).also { read = it } != -1) {
                    outStream.write(buf, 0, read)
                    //written += read;
                }
            } catch (e: Exception) {
                throw RuntimeException(e)
            } finally {
                RunUtils.close(outStream)
            }
            println("Finish downloading: $urlString to $output")
        }

        private fun delete(filePath: String) {
            File(filePath).delete()
        }
    }
}