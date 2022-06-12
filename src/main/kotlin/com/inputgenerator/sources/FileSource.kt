package com.inputgenerator.sources

import com.inputgenerator.metrics.MetricsRepository
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader

class FileSource(
    sequenceName: String,
    sourceName: String = "fileSource",
    sourceCount: String = "0",
    val fileName: String?,
    val readFirstLine: Boolean = false,
    val restartFromBeginning: Boolean = false
): BaseDataSource<String>(sequenceName, "${sourceName}-${sourceCount}", MetricsRepository) {
    // File readers
    val file: File = fileName?.let { it -> File(it) } ?: throw Error("No file name in config object")
    var inputStreamReader: BufferedReader = getBufferedReaderFromFile(file)

    // Conditional behaviour
    var headers: String? = getHeader(readFirstLine, inputStreamReader)

    fun getLine(): String?{
        var line: String? = inputStreamReader.readLine()
        if(available() && line != null ){
            //println(line)
            return line
        }
        else {
            inputStreamReader.close()
            print("Source ended. ")
            if(restartFromBeginning){
                println("Opening again")

                inputStreamReader = getBufferedReaderFromFile(file)
                headers = getHeader(readFirstLine, inputStreamReader)

                return inputStreamReader.readLine()
            }
            else {
                println("Nothing to do")
                return null
            }
        }
    }

    override fun get(): String? {
        return this.getLine()
    }

    override fun available(): Boolean {
        return inputStreamReader.ready()
    }

    override fun getDescription(): String {
        return "\n      class: ${this.javaClass.name} \n      file: ${this.fileName} \n" +
                "      readFirstLine: ${this.readFirstLine} \n" +
                "      restartFromBeginning: ${this.restartFromBeginning}"
    }

    companion object {
        private fun getBufferedReaderFromFile(file: File): BufferedReader{
            return BufferedReader(InputStreamReader(FileInputStream(file)))
        }

        private fun getHeader(readHeader: Boolean, bufferedReader: BufferedReader): String?{
            return if (readHeader) bufferedReader.readLine() else null
        }
    }
}