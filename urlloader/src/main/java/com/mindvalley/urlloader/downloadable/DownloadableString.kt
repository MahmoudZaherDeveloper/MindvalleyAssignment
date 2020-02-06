package com.mindvalley.urlloader.downloadable

import com.mindvalley.urlloader.utils.DownloadableContent

class DownloadableString(url: String) : Downloadable<String>(url) {
    //converts ByteArray to string
    override fun transform(content: DownloadableContent): String {
        return content.toString(Charsets.UTF_8)
    }
}