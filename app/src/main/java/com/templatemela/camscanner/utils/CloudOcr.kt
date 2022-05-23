package com.templatemela.camscanner.utils

import android.graphics.Bitmap
import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.services.vision.v1.Vision
import com.google.api.services.vision.v1.VisionRequestInitializer
import com.google.api.services.vision.v1.model.*
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.*


object CloudOcr {
   private var visionBuilder : Vision.Builder? = null
   fun  getVisionBuider () :  Vision.Builder? {
      if (visionBuilder== null) {
          visionBuilder  = Vision.Builder(
              NetHttpTransport(),
              AndroidJsonFactory(),
              null
          )

          visionBuilder?.setVisionRequestInitializer(
              VisionRequestInitializer("AIzaSyC7dMKrw8phwjSjsU4I-eewPCp-ZZsPsto")
          )
      }
       return  visionBuilder
   }


    fun convertBitmapToText(bitmap: Bitmap?, vision: Vision? ) : String?  {
        try {
            val stream = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val byteArray: ByteArray = stream.toByteArray()
            var image  = com.google.api.services.vision.v1.model.Image().encodeContent(byteArray)
            var feature = Feature().apply {
                type = "DOCUMENT_TEXT_DETECTION"
//                type = "TEXT_DETECTION"
            }
            val request = AnnotateImageRequest()
            request.image = image
            request.features = listOf(feature)

            val batchRequest = BatchAnnotateImagesRequest()
            batchRequest.requests = Arrays.asList(request)

            val batchResponse: BatchAnnotateImagesResponse? =
                vision?.images()?.annotate(batchRequest)?.execute()
            val firstRes =  batchResponse?.getResponses()?.get(0)
            if (firstRes?.error != null  && firstRes?.error?.message!= null ) {
                return "Lỗi phản hồi"
            }
            var text =firstRes?.getFullTextAnnotation()
            return  text?.text
        }
        catch (e : Exception){
            e.printStackTrace()
            return ""
        }

    }
}