package com.rithikjain.neuralstyletransfer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var mJob: Job
    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main

    private lateinit var styleTransferModel: StyleTransferModel
    private var isModelRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progressBar.visibility = View.INVISIBLE

        mJob = Job()

        val styleList = mutableListOf<String>()
        val styleNameList = mutableListOf<String>()
        for (i in 0..25) {
            styleList.add("style$i.jpg")
            styleNameList.add("Style $i")
        }

        val stylesRecyclerViewAdapter = StylesRecyclerViewAdapter()
        stylesRecyclerViewAdapter.setStylesList(styleList, styleNameList)
        stylesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = stylesRecyclerViewAdapter
        }
        LinearSnapHelper().attachToRecyclerView(stylesRecyclerView)

        styleTransferModel = StyleTransferModel(this)

        stylesRecyclerView.addOnItemClickListener(object : OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                if (!isModelRunning) {
                    isModelRunning = true
                    launch {
                        withContext(Dispatchers.IO) {
                            withContext(Dispatchers.Main) {
                                outputImage.visibility = View.INVISIBLE
                                progressBar.visibility = View.VISIBLE
                            }
                            styleTransferModel.setUpFilter(styleList[position])
                            val img = styleTransferModel.styleImage("content2.jpg")
                            withContext(Dispatchers.Main) {
                                progressBar.visibility = View.INVISIBLE
                                outputImage.visibility = View.VISIBLE
                                outputImage.setImageBitmap(img)
                                isModelRunning = false
                            }
                        }
                    }
                } else {
                    Log.d("esh", "Model Running")
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        styleTransferModel.close()
        mJob.cancel()
    }
}