package com.templatemela.camscanner.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.gms.tasks.Task
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

import java.io.File
import java.io.IOException

object MlKitOrcUnit {
    fun getMapText(uri: File?, callback: (map: HashMap<String?, String?>) -> Unit) {

        var mapTexts = hashMapOf<String?, String?>()
        for (i in 0..3) {
            try {
                val textRecognizer =
                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val bitmap = BitmapFactory.decodeFile(uri?.absolutePath)
                val image = InputImage.fromBitmap(bitmap, 90 * i)
                var task: Task<Text> = textRecognizer.process(image)
                    .addOnSuccessListener {
                        val text = it.text
                        val languageIdentifier = LanguageIdentification.getClient()
                        languageIdentifier.identifyLanguage(text)
                            .addOnSuccessListener { languageCode ->
                                val objUnd = mapTexts.get(languageCode)
                                if ((objUnd ?: "").length <= text.length) {
                                    mapTexts[languageCode] = text
                                    if (i == 3) callback(mapTexts)
                                }
                            }
                            .addOnFailureListener {
                                // Model couldn’t be loaded or other internal error.
                                // ...
                            }
                    }
                    .addOnFailureListener {

                    }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun getMapText(bitmap: Bitmap?, callback: (map: HashMap<String?, String?>) -> Unit) {
        if (bitmap == null ) {
            callback(hashMapOf())
            return
        }
        var mapTexts = hashMapOf<String?, String?>()
        for (i in 0..3) {
            try {
                val textRecognizer =
                    TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
//                val bitmap = BitmapFactory.decodeFile(uri?.absolutePath)
                val image = InputImage.fromBitmap(bitmap, 90 * i)
                var task: Task<Text> = textRecognizer.process(image)
                    .addOnSuccessListener {
                        val text = it.text
                        val languageIdentifier = LanguageIdentification.getClient()
                        languageIdentifier.identifyLanguage(text)
                            .addOnSuccessListener { languageCode ->
                                val objUnd = mapTexts.get(languageCode)
                                if ((objUnd ?: "").length <= text.length) {
                                    mapTexts[languageCode] = text

                                }

                                if (i == 3) {
                                    callback(mapTexts)
                                    return@addOnSuccessListener
                                }
                            }
                            .addOnFailureListener {
                                // Model couldn’t be loaded or other internal error.
                                // ...
                                    callback(mapTexts?: hashMapOf())
                                    return@addOnFailureListener

                            }
                    }
                    .addOnFailureListener {
                        callback(mapTexts?: hashMapOf())
                        return@addOnFailureListener
                    }
            } catch (e: IOException) {
                e.printStackTrace()
                callback(hashMapOf())
                return
            }
        }
    }


}