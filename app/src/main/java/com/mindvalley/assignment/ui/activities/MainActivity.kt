package com.mindvalley.assignment.ui.activities

import android.animation.Animator
import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityOptionsCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.recyclerview.extensions.AsyncListDiffer
import android.support.v7.util.DiffUtil
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.mindvalley.assignment.R
import com.mindvalley.assignment.data.model.ItemPin
import com.mindvalley.assignment.viewmodel.MainViewModel
import com.mindvalley.urlloader.Loader
import com.mindvalley.urlloader.downloadable.Downloadable
import com.mindvalley.urlloader.downloadable.DownloadableBitmap
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.cache_config_layout.view.*
import kotlinx.android.synthetic.main.item_layout.view.*
import java.io.Serializable


class MainActivity : AppCompatActivity() {

    private lateinit var model: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Called here only for demonstrative purposes, in practice should be moved to the Application class
        //or library implementation
        Loader.resizeCache(10 * 1024 * 1024)//Set cache size to 10MB

        //get the ViewModel for this activity
        model = ViewModelProviders.of(this).get(MainViewModel::class.java)

        //configure views
        main_r_v.layoutManager = LinearLayoutManager(this)
        main_r_v.adapter = MainPinRecyclerViewAdapter()
        //attach events
        swiper.setOnRefreshListener {
            model.doRefresh()
        }

        fab.setOnClickListener {
            showCoolAnimation()
            (main_r_v.adapter as MainPinRecyclerViewAdapter).sendList(model.pinList.value!!.shuffled())
            Snackbar.make(mainView, "Pins shuffled!", Snackbar.LENGTH_SHORT).show()
        }

        //attach observers
        model.isRefreshing.observe(this, Observer {
            swiper.isRefreshing = it!!
        })

        model.pinList.observe(this, Observer {
            (main_r_v.adapter as MainPinRecyclerViewAdapter).sendList(it!!)
        })

        model.snackBarText.observe(this, Observer {
            if (it != null)
                Snackbar.make(mainView, it, Snackbar.LENGTH_SHORT).show()
        })
    }

    private fun showCoolAnimation() {
        val centerX = (fab.left + fab.right) / 2
        val centerY = (fab.top + fab.bottom) / 2

        val startRadius = 0
        val endRadius = Math.max(coolAnimationView.width, coolAnimationView.height)
        val anim = ViewAnimationUtils.createCircularReveal(coolAnimationView, centerX, centerY, startRadius.toFloat(), endRadius.toFloat())
        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}

            override fun onAnimationEnd(animation: Animator?) {
                coolAnimationView.visibility = View.INVISIBLE
            }

            override fun onAnimationCancel(animation: Animator?) {}

            override fun onAnimationStart(animation: Animator?) {}

        })
        coolAnimationView.visibility = View.VISIBLE
        anim.start()
    }


    inner class MainPinRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        //ListDiffer used to perform item animations on changes
        private val mDiffer = AsyncListDiffer(this, object : DiffUtil.ItemCallback<ItemPin>() {
            override fun areItemsTheSame(oldItem: ItemPin, newItem: ItemPin): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                    oldItemPin: ItemPin, newItemPin: ItemPin): Boolean {
                return oldItemPin.likedByUser == newItemPin.likedByUser &&
                        oldItemPin.color == newItemPin.color &&
                        oldItemPin.url == newItemPin.url &&
                        oldItemPin.width == newItemPin.width &&
                        oldItemPin.height == newItemPin.height &&
                        oldItemPin.createdAt == newItemPin.createdAt
            }
        })

        //send new list of pins to adapter
        fun sendList(itemPins: List<ItemPin>) {
            mDiffer.submitList(itemPins)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val v = layoutInflater.inflate(R.layout.item_layout, parent, false)
            val vh = PinHolder(v)
            vh.imageView = v.pict
            vh.textView = v.nameTv
            return vh
        }

        override fun getItemCount(): Int {
            return mDiffer.currentList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val pinHolder = holder as PinHolder
            val pin = mDiffer.currentList[position]
            pinHolder.bindTo(pin)
        }

        internal inner class PinHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            lateinit var imageView: ImageView
            private var downloadable: Downloadable<*>? = null
            lateinit var textView: TextView

            fun bindTo(itemPin: ItemPin) {
                //clear dirty views
                downloadable?.cancel()
                imageView.setImageDrawable(null)

                //attach new content
                textView.text = itemPin.userName
                imageView.setBackgroundColor(Color.parseColor(itemPin.color))

                //preset height for future image load
                imageView.post {
                    if (itemPin.width < imageView.width) {
                        imageView.minimumHeight = itemPin.height
                        return@post
                    }

                    val scale = itemPin.width.toFloat() / imageView.width.toFloat()
                    val scaledHeight = itemPin.height.toFloat() / scale
                    imageView.minimumHeight = scaledHeight.toInt()

                }

                //load image using library
                val down = DownloadableBitmap(itemPin.url)
                downloadable = down
                down.load { result, throwable ->
                    if (throwable != null)//if image_holder show placeholder
                    {
                        Log.e("Placeholder", "Exception: $throwable")
                        imageView.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.image_holder))
                    } else//show loaded image
                        imageView.setImageBitmap(result!!)
                }

                //attach events to open pin details
                itemView.setOnClickListener {
                    val intent = Intent(this@MainActivity, DetailActivity::class.java).putExtra("pin", itemPin as Serializable)
                    val activityOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            this@MainActivity,
                            android.support.v4.util.Pair<View, String>(imageView,
                                    getString(R.string.image_item_for_transition)))
                    startActivity(intent, activityOptions.toBundle())

                }
            }
        }

    }

    //Ugly fix for bug of lifecycle extensions https://issuetracker.google.com/issues/73644080
    //this bug is corrected on the last update.
    override fun onDestroy() {
        if (!isFinishing) {
            val oldViewModel = obtainViewModel()
            val pins = oldViewModel.pinList.value
            val isRefreshing = oldViewModel.isRefreshing.value
            super.onDestroy()
            val newViewModel = obtainViewModel()
            if (newViewModel != oldViewModel) {
                newViewModel.pinList.value = pins
                newViewModel.isRefreshing.value = isRefreshing
            }
        } else {
            super.onDestroy()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_menu, menu)
        return true
    }

    @SuppressLint("InflateParams")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_set_cache_size -> {
                val mBottomSheetDialog = BottomSheetDialog(this)

                val sheetView = layoutInflater.inflate(R.layout.cache_config_layout, null)
                sheetView.cacheSizeText.setText((Loader.cacheSize() / 1024 / 1024).toString())
                sheetView.cacheSizeButton.setOnClickListener {
                    try {
                        Loader.resizeCache(sheetView.cacheSizeText.text.toString().toInt() * 1024 * 1024)
                    } catch (ex: NumberFormatException) {
                        Loader.resizeCache(10 * 1024 * 1024)//10MB
                        Toast.makeText(this, "Invalid value, defaulting to 10MB", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Loader.resizeCache(10 * 1024 * 1024)//10MB
                        Toast.makeText(this, "Error, defaulting to 10MB", Toast.LENGTH_LONG).show()
                    }
                    mBottomSheetDialog.dismiss()
                }
                mBottomSheetDialog.setContentView(sheetView)
                mBottomSheetDialog.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun obtainViewModel(): MainViewModel {
        return ViewModelProviders.of(this).get(MainViewModel::class.java)
    }
}
