package com.rithikjain.neuralstyletransfer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var styleTransferModel: StyleTransferModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

    }

    override fun onDestroy() {
        super.onDestroy()
        styleTransferModel.close()
    }
}