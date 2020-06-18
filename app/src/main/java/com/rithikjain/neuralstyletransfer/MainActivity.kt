package com.rithikjain.neuralstyletransfer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var styleTransferModel: StyleTransferModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        styleTransferModel = StyleTransferModel(this)

        styleTransferModel.setUpFilter("style1.jpg")

        goButton.setOnClickListener {
            goButton.isEnabled = false
            val image = styleTransferModel.styleImage("content2.jpg")
            tempImage.setImageBitmap(image)
            goButton.isEnabled = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        styleTransferModel.close()
    }
}