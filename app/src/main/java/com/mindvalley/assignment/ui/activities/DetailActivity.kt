package com.mindvalley.assignment.ui.activities

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.ViewCompat
import android.support.v7.app.AppCompatActivity
import com.mindvalley.assignment.R
import com.mindvalley.assignment.data.model.ItemPin
import com.mindvalley.urlloader.downloadable.DownloadableBitmap
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val pin = intent.extras.getSerializable("pin") as ItemPin

        pict.setBackgroundColor(Color.parseColor(pin.color))
        created.text = pin.createdAt
        user.text = pin.userName

        ViewCompat.setTransitionName(pict, getString(R.string.image_item_for_transition))

        DownloadableBitmap(pin.url).load { result, throwable ->
            if (throwable != null)
                pict.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.image_holder))
            else
                pict.setImageBitmap(result!!)
        }

    }
}
