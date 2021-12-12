package com.ckziu_app.ui.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.getDrawableOrThrow
import androidx.core.view.ViewCompat
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.ckziu_app.model.News
import com.ckziu_app.model.Result
import com.ckziu_app.model.Success
import com.ckziu_app.ui.fragments.NewsListFragment
import com.ckziu_app.ui.viewmodels.NewsViewModel
import com.ckziu_app.utils.makeGone
import com.example.ckziuapp.R
import com.example.ckziuapp.databinding.CardNewsBinding
import com.example.ckziuapp.databinding.PageIndexLayoutBinding
import com.squareup.picasso.Picasso

/** Adapts news for displaying inside [NewsListFragment]
 * @param activeNews list of the news for displaying
 * @param newsListFragment fragment inside of which news will displayed
 * */
class NewsAdapter(
    private val activeNews: List<News>,
    newsListFragment: NewsListFragment
) : RecyclerView.Adapter<NewsAdapter.AbstractViewHolder>() {

    companion object {
        const val CARD_VIEWTYPE = 0
        const val PAGESELECTOR_VIEWTYPE = 1
    }

    private val viewModel = newsListFragment.viewModel
    private val onNewsClickListener = OnNewsClickListener(newsListFragment)

    abstract class AbstractViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(activeNews: List<News>, position: Int)
    }

    class NewsCardViewHolder internal constructor(
        viewBinding: CardNewsBinding,
        private val onNewsClickListener: OnNewsClickListener
    ) : AbstractViewHolder(viewBinding.root) {

        private val body = viewBinding.cvWrapper
        private val ivPreview = viewBinding.ivCardImage
        private val tvTitle = viewBinding.cardTitleTv
        private val tvBody = viewBinding.cardMainTextTv
        private val tvCreationInfo = viewBinding.creationDateCardTv

        override fun bind(activeNews: List<News>, position: Int) {
            val currentNews = activeNews[position]

            currentNews.run {

                itemView.setOnClickListener {
                    onNewsClickListener.onNewsClick(this, body)
                }

                tvTitle.text = title
                tvBody.text = textPreview

                tvCreationInfo.text = when {
                    (dateOfCreation != null && creatorName != null) -> {
                        dateOfCreation + " - " + creatorName
                    }

                    (dateOfCreation == null && creatorName == null) -> {
                        tvCreationInfo.makeGone()
                        null
                    }

                    (dateOfCreation == null) -> creatorName
                    (creatorName == null) -> dateOfCreation
                    // redundant because we covered all cases but compiler won't let us skip 'else'
                    else -> null
                }

                this.linkToImage.let { link ->
                    if (link != null) {
                        showImagePreview(ivPreview, link)
                    } else { // some news do NOT contain image
                        ivPreview.makeGone()
                        tvCreationInfo.makeGone()
                    }
                }
            }
        }

        private fun showImagePreview(imageView: ImageView, imageUrl: String) {
            val placeholder = imageView.context.theme.run {
                val placeholderId = listOf(R.attr.imageLoadingDrawable).toIntArray()
                obtainStyledAttributes(placeholderId).getDrawableOrThrow(0)
            }

            Picasso.get()
                .load(imageUrl)
                .placeholder(placeholder)
                .error(android.R.drawable.stat_notify_error)
                .into(imageView)
        }
    }

    class PageIndexViewHolder(
        viewBinding: PageIndexLayoutBinding,
        private val viewModel: NewsViewModel
    ) : AbstractViewHolder(viewBinding.root) {

        private val tvFirstPage: TextView = viewBinding.tvFirstPage
        private val dotsLeft: TextView = viewBinding.tvDotsLeft
        private val tvPreviousPage: TextView = viewBinding.tvPreviousPage
        private val tvActivePage: TextView = viewBinding.tvActivePage
        private val tvNextPage: TextView = viewBinding.tvNextPage
        private val dotsRight: TextView = viewBinding.tvDotsRight
        private val tvLastPage: TextView = viewBinding.tvLastPage


        private fun resultToDefaultNumber(result: Result<Int>?): String = when (result) {
            null -> "1"
            is Success -> result.resultValue.toString()
            else -> "1"
        }

        override fun bind(activeNews: List<News>, position: Int) {

            val activeNewsPageIndex = viewModel.activeNewsPageIndex.value ?: 1

            tvFirstPage.text = "1"
            tvPreviousPage.text = (activeNewsPageIndex - 1).toString()
            tvActivePage.text = activeNewsPageIndex.toString()
            tvNextPage.text = (activeNewsPageIndex + 1).toString()
            tvLastPage.text = resultToDefaultNumber(viewModel.maxPageIndex.value)

            setVisibility()

            val onPageIndexListener = View.OnClickListener { textView ->
                val chosenPageIndex = (textView as TextView).text.toString().toInt()

                // this change of color is showing user which index of page is loading
                val normalTextColor = textView.textColors
                textView.setTextColor(tvActivePage.textColors)
                tvActivePage.setTextColor(normalTextColor)

                viewModel.changePageAndFetchNews(chosenPageIndex)
            }


            listOf(tvFirstPage, tvPreviousPage, tvNextPage, tvLastPage).forEach { textView ->
                textView.setOnClickListener(onPageIndexListener)
            }
        }

        private fun setVisibility() {
            val maxIndex = viewModel.maxPageIndex.value.let { maxPage ->
                if (maxPage is Success) {
                    maxPage.resultValue
                } else {
                    1
                }
            }


            when (viewModel.activeNewsPageIndex.value!!) {
                0, 1 -> {
                    tvFirstPage.makeGone()
                    tvPreviousPage.makeGone()
                    dotsLeft.makeGone()
                }

                2 -> {
                    tvFirstPage.makeGone()
                    dotsLeft.makeGone()
                }

                3 -> {
                    dotsLeft.makeGone()
                }

                4 -> {
                    dotsLeft.text = "2"
                    dotsLeft.alpha = 1.0f
                }

                (maxIndex - 3) -> {
                    dotsRight.text = (maxIndex - 1).toString()
                    dotsRight.alpha = 1.0f
                }

                (maxIndex - 2) -> {
                    dotsRight.makeGone()
                }

                (maxIndex - 1) -> {
                    dotsRight.makeGone()
                    tvLastPage.makeGone()
                }

                (maxIndex) -> {
                    tvNextPage.makeGone()
                    dotsRight.makeGone()
                    tvLastPage.makeGone()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbstractViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            CARD_VIEWTYPE -> {
                NewsCardViewHolder(
                    CardNewsBinding.inflate(layoutInflater, parent, false),
                    onNewsClickListener
                )
            }

            PAGESELECTOR_VIEWTYPE -> {
                PageIndexViewHolder(
                    PageIndexLayoutBinding.inflate(layoutInflater, parent, false),
                    viewModel
                )
            }

            else -> throw UnsupportedOperationException("Unszupported view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: AbstractViewHolder, position: Int) {
        holder.bind(activeNews, position)
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            (position == activeNews.size) -> PAGESELECTOR_VIEWTYPE
            else -> CARD_VIEWTYPE
        }
    }

    /** Refreshing [PageIndexViewHolder]*/
    fun refreshIndex() {
        notifyItemChanged(itemCount)
    }

    // last item is a page selector so one is added to the size of the list
    override fun getItemCount(): Int = activeNews.size + 1

    internal class OnNewsClickListener(private val newsListFragmentView: NewsListFragment) {

        fun onNewsClick(targetNews: News, itemView: View) {

            newsListFragmentView.run {

                val newsBundle = Bundle().apply {
                    putParcelable(News.NEWS_PARCELABLE_KEY, targetNews)
                }

                newsListFragmentView.viewModel.loadArticleHtml(targetNews)

                ViewCompat.setTransitionName(itemView, itemView.id.toString() + targetNews.newsID)

                val navExtras = FragmentNavigatorExtras(
                    itemView to "cardToDetails"
                )

                findNavController().navigate(
                    R.id.action_mainFragment_to_newsDetailsFragment,
                    newsBundle,
                    null,
                    navExtras
                )
            }
        }
    }
}