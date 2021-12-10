package com.ckziu_app.ui.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.getDrawableOrThrow
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.ckziu_app.model.MainPageInfo
import com.ckziu_app.model.News
import com.example.ckziuapp.R
import com.squareup.picasso.Picasso

/** Adapts news for displaying inside [MainPageInfo].
 * @param listOfMiniNews list of the [News] for displaying */
class MiniNewsRecViewAdapter(private val listOfMiniNews: List<News>) :
    RecyclerView.Adapter<MiniNewsRecViewAdapter.MiniNewsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiniNewsViewHolder {
        val miniNewsView = LayoutInflater.from(parent.context).inflate(
            R.layout.card_mininews_layout,
            parent,
            false
        )

        return MiniNewsViewHolder(miniNewsView)
    }

    override fun onBindViewHolder(holder: MiniNewsViewHolder, position: Int) {
        val currentNews = listOfMiniNews[position]
        holder.title.text = currentNews.title

        currentNews.linkToImage?.let { uri ->
            loadImage(holder.image, uri)
        }

        holder.itemView.setOnClickListener { miniNewsView ->
            val navController = Navigation.findNavController(miniNewsView)

            val newsBundle = Bundle().apply { putParcelable(News.NEWS_PARCELABLE_KEY, currentNews) }
            navController.navigate(R.id.action_mainPageFragment_to_newsDetailsFragment, newsBundle)
        }
    }

    private fun loadImage(imageView: ImageView, uri: String) {
        val placeholder = imageView.context.theme.run {
            val placeholderId = listOf(R.attr.imageLoadingDrawable).toIntArray()
            obtainStyledAttributes(placeholderId).getDrawableOrThrow(0)
        }

        Picasso.get().load(uri).placeholder(placeholder).into(imageView)
    }

    override fun getItemCount(): Int = listOfMiniNews.size

    /** View holder designed for working with [MiniNewsRecViewAdapter].*/
    class MiniNewsViewHolder(miniNewsView: View) : RecyclerView.ViewHolder(miniNewsView) {
        val image: ImageView = miniNewsView.findViewById(R.id.iv_mininews_photo)
        val title: TextView = miniNewsView.findViewById(R.id.tv_title_card_mininews)
    }
}