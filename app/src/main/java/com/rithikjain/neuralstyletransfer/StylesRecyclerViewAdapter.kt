package com.rithikjain.neuralstyletransfer

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.style_item.view.*

class StylesRecyclerViewAdapter(private val border: Drawable) :
    RecyclerView.Adapter<StylesRecyclerViewAdapter.StylesViewHolder>() {

    private var stylesList: List<String> = listOf()
    private var stylesNameList: List<String> = listOf()
    var selectedPos = 0

    fun setStylesList(styles: List<String>, styleNames: List<String>) {
        stylesList = styles
        stylesNameList = styleNames
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = StylesViewHolder(
        LayoutInflater.from(parent.context).inflate(
            R.layout.style_item,
            parent,
            false
        ),
        parent.context
    )

    override fun onBindViewHolder(holder: StylesViewHolder, position: Int) {
        holder.bind(stylesList[position], stylesNameList[position])

        if (selectedPos == position) {
            holder.itemView.image.foreground = border
        } else {
            holder.itemView.image.foreground = null
        }
    }

    override fun getItemCount(): Int = stylesList.size

    class StylesViewHolder(view: View, private val context: Context) :
        RecyclerView.ViewHolder(view) {
        private val imageView = view.image
        private val title = view.title

        fun bind(imageName: String, styleName: String) {
            Glide.with(context)
                .load(Uri.parse("file:///android_asset/styles/$imageName"))
                .into(imageView)
            title.text = styleName
        }
    }
}