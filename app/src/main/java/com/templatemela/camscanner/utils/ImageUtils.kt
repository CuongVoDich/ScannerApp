package com.templatemela.camscanner.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory

object ImageUtils

public fun createBimapBytes(bitmapdata: ByteArray): Bitmap? {
    return BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.size)
}