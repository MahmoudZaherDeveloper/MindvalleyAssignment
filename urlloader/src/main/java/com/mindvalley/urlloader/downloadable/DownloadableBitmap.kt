package com.mindvalley.urlloader.downloadable

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.mindvalley.urlloader.utils.DownloadableContent

//converts ByteArray to Bitmap
class DownloadableBitmap(url: String) : Downloadable<Bitmap>(url) {

    override fun transform(content: DownloadableContent): Bitmap {
        return BitmapFactory.decodeByteArray(content, 0, content.size)
    }
}