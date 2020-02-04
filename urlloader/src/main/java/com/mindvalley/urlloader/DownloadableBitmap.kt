package com.mindvalley.urlloader

import android.graphics.Bitmap
import android.graphics.BitmapFactory

//converts ByteArray to Bitmap
class DownloadableBitmap(url: String) : Downloadable<Bitmap>(url) {
    override fun transform(content: DownloadableContent): Bitmap {
        return BitmapFactory.decodeByteArray(content, 0, content.size)
    }
}