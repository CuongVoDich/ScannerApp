package com.templatemela.camscanner.utils

import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVision
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionClient
import com.microsoft.azure.cognitiveservices.vision.computervision.implementation.ComputerVisionImpl
import com.microsoft.azure.cognitiveservices.vision.computervision.models.OperationStatusCodes
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ReadInStreamHeaders
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ReadOperationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import rx.Observable
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.file.Files
import java.util.*

object MicrosoftOrc {
    var subscriptionKey = "9542a62b2e2341e69758a0d563d14fb3"
    var endpoint = "https://computervision-cuongdt.cognitiveservices.azure.com/"

    @RequiresApi(Build.VERSION_CODES.O)
     fun ReadFromFile(client: ComputerVisionClient, file: File? = null , callback: (result : String?)-> Unit) {
        runBlocking {
            withContext(Dispatchers.IO) {
                println("-----------------------------------------------")
                val localFilePath = file?.absolutePath ?: "src\\main\\resources\\myImage.png"
                println("Read with local file: $localFilePath")
                // </snippet_read_setup>
                // <snippet_read_call>
                try {
                    val rawImage = File(localFilePath)
                    val localImageBytes: ByteArray = Files.readAllBytes(rawImage.toPath())

                    // Cast Computer Vision to its implementation to expose the required methods
                    val vision = client.computerVision() as ComputerVisionImpl

                    // Read in remote image and response header
                    val responseHeader =
                        vision.readInStreamWithServiceResponseAsync(
                            localImageBytes,
                            null,
                            null,
                            null,
                            null
                        )
                            .toBlocking()
                            .single()
                            .headers()
                    // </snippet_read_call>
                    // <snippet_read_response>
                    // Extract the operationLocation from the response header
                    val operationLocation = responseHeader.operationLocation()
                    println("Operation Location:$operationLocation")
                     val result = getAndPrintReadResult(vision, operationLocation)
                    callback(result)
                    // </snippet_read_response>
                    // <snippet_read_catch>
                } catch (e: Exception) {
                    callback("")
                    println(e.message)
                    e.printStackTrace()
                }
            }

        }
    }

        fun ReadFromFile(client: ComputerVisionClient, bitMap: Bitmap? = null): String? {
           try {
               val stream = ByteArrayOutputStream()
               bitMap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
               val localImageBytes: ByteArray = stream.toByteArray()
               // Cast Computer Vision to its implementation to expose the required methods
               val vision = client.computerVision() as ComputerVisionImpl

               // Read in remote image and response header
               val responseHeader =
                   vision.readInStreamWithServiceResponseAsync(
                       localImageBytes,
                       null,
                       null,
                       null,
                       null
                   )
                       .toBlocking()
                       .single()
                       .headers()
               // </snippet_read_call>
               // <snippet_read_response>
               // Extract the operationLocation from the response header
               val operationLocation =  getLocalInage(responseHeader)
               println("Operation Location:$operationLocation")

               return ((getAndPrintReadResult(vision, operationLocation)))
               // </snippet_read_response>
               // <snippet_read_catch>
           } catch (e: Exception) {

               println(e.message)
               e.printStackTrace()
               return ""
           }

    }

     fun getLocalInage(responseHeader: ReadInStreamHeaders): String? {
       return responseHeader.operationLocation()
    }

    private fun extractOperationIdFromOpLocation(operationLocation: String?): String {
        if (operationLocation != null && !operationLocation.isEmpty()) {
            val splits = operationLocation.split("/").toTypedArray()
            if (splits != null && splits.size > 0) {
                return splits[splits.size - 1]
            }
        }
        throw IllegalStateException("Something went wrong: Couldn't extract the operation id from the operation location")
    }
    // </snippet_opid_extract>

    // <snippet_read_result_helper_call>
    // </snippet_opid_extract>
    // <snippet_read_result_helper_call>
    /**
     * Polls for Read result and prints results to console
     * @param vision Computer Vision instance
     * @return operationLocation returned in the POST Read response header
     */
    @Throws(InterruptedException::class)
    private fun getAndPrintReadResult(vision: ComputerVision, operationLocation: String?): String? {
        // Extract OperationId from Operation Location
        val operationId = extractOperationIdFromOpLocation(operationLocation)
        var pollForResult = true
        var readResults: ReadOperationResult? = null
        while (pollForResult) {
            // Poll for result every second
            Thread.sleep(1000)
            readResults = vision.getReadResult(UUID.fromString(operationId))

            // The results will no longer be null when the service has finished processing the request.
            if (readResults != null) {
                // Get request status
                val status = readResults.status()
                if (status == OperationStatusCodes.FAILED || status == OperationStatusCodes.SUCCEEDED) {
                    pollForResult = false
                }
            }
        }
        // </snippet_read_result_helper_call>

        // <snippet_read_result_helper_print>
        // Print read results, page per page
        for (pageResult in readResults!!.analyzeResult().readResults()) {
            Log.d("getAndPrintReadResult", pageResult?.language()?:"")
            System.out.println("Printing Read results for page " + pageResult.page())
            val builder = StringBuilder()
            for (line in pageResult.lines()) {
                builder.append(line.text())
                builder.append("\n")
            }
            return builder?.toString()
        }
        return null
    }

}