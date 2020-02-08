package com.mindvalley.urlloader

import com.mindvalley.urlloader.downloadable.DownloadableBitmap
import junit.framework.Assert
import org.junit.Test

class UnitTest {
    //Only instrumentation tests are provided, see DownloadableTests.kt
    @Test
       fun downloadablesCancellation() {
        val dow = DownloadableBitmap("https://via.placeholder.com/150")
           val list= mutableListOf(dow)
           dow.cancel()
           Assert.assertEquals(dow.cancelled,true)
           Assert.assertEquals(list[0].cancelled,true)
    }
}