package com.mindvalley.urlloader

import android.graphics.Bitmap
import com.mindvalley.urlloader.downloadable.DownloadableBitmap
import com.mindvalley.urlloader.downloadable.DownloadableString
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@RunWith(JUnit4::class)
class DownloadableTests {

    // Majority of tests in this particular case can be performed more efficiently with
    // "log debugging" (provided for Loader class) rather than using instrumentation/unit tests.
    // In this special case tests can run more complex than the implementation itself.

    //Here we provide basic tests for convenience Downloadable classes

    @Test
    fun checkLoadBitmap() {
        runBlocking {
            val meh = suspendCoroutine<Pair<Bitmap?, Throwable?>> { cont ->
                DownloadableBitmap("https://via.placeholder.com/150").load { s, throwable ->
                    cont.resume(Pair(s, throwable))
                }
            }
            Assert.assertNotNull(meh.first)
            Assert.assertNull(meh.second)
        }
    }

    @Test
    fun checkLoadUrl() {
        runBlocking {
            val meh = suspendCoroutine<Pair<String?, Throwable?>> { cont ->
                DownloadableString("http://pastebin.com/raw/wgkJgazE").load { s, throwable ->
                    cont.resume(Pair(s, throwable))
                }
            }
            Assert.assertNotNull(meh.first)
            Assert.assertNull(meh.second)
        }
    }
}
