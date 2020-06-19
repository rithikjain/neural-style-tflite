package com.rithikjain.neuralstyletransfer

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.File
import java.lang.Exception
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {
    private lateinit var mJob: Job
    override val coroutineContext: CoroutineContext
        get() = mJob + Dispatchers.Main

    private lateinit var styleTransferModel: StyleTransferModel
    private var isModelRunning = false
    private var contentImage: Bitmap? = null
    private val pickImage = 1

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

        runWithPermissions(android.Manifest.permission.READ_EXTERNAL_STORAGE) {
            selectImageButton.setOnClickListener {
                val gallery = Intent(Intent.ACTION_PICK)
                gallery.type = "image/*"

                startActivityForResult(gallery, pickImage)
            }
        }

        stylesRecyclerView.addOnItemClickListener(object : OnItemClickListener {
            override fun onItemClicked(position: Int, view: View) {
                if (!isModelRunning) {
                    if (contentImage != null) {
                        isModelRunning = true
                        launch {
                            withContext(Dispatchers.IO) {
                                withContext(Dispatchers.Main) {
                                    outputImage.visibility = View.INVISIBLE
                                    progressBar.visibility = View.VISIBLE
                                }
                                styleTransferModel.setUpFilter(styleList[position])
                                val img = styleTransferModel.styleImage(contentImage!!)
                                withContext(Dispatchers.Main) {
                                    progressBar.visibility = View.INVISIBLE
                                    outputImage.visibility = View.VISIBLE
                                    outputImage.setImageBitmap(img)
                                    isModelRunning = false
                                }
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Select an image first",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Log.d("esh", "Model Running")
                }
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == pickImage && resultCode == Activity.RESULT_OK && data != null) {
            try {
                val uri = data.data!!
                val imageStream = contentResolver.openInputStream(uri)
                contentImage = BitmapFactory.decodeStream(imageStream)
                outputImage.setImageBitmap(contentImage)
                Log.d("esh", "Images selected")
            } catch (e: Exception) {
                Log.d("esh", "Something went wrong in selecting photo")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        styleTransferModel.close()
        mJob.cancel()
    }
}