package com.ckziu_app.ui.fragments

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebSettingsCompat.FORCE_DARK_ON
import androidx.webkit.WebViewFeature
import com.ckziu_app.model.News
import com.ckziu_app.ui.helpers.ErrorInformant
import com.ckziu_app.ui.viewmodels.NewsViewModel
import com.ckziu_app.utils.makeGone
import com.ckziu_app.utils.makeVisible
import com.example.ckziuapp.R
import com.example.ckziuapp.databinding.FragmentNewsDetailBinding
import com.google.android.material.transition.MaterialContainerTransform
import dagger.hilt.android.AndroidEntryPoint

/** Fragment showing detailed information about chosen [News] */
@AndroidEntryPoint
class NewsDetailsFragment : Fragment() {

    companion object {
        const val TAG = "NewsDetailsFragment"
    }

    private var _viewBinding: FragmentNewsDetailBinding? = null
    private val viewBinding get() = _viewBinding!!

    private val viewModel by viewModels<NewsViewModel>()

    /** News of which details should be displayed*/
    private lateinit var chosenNews: News

    /** Activity should be implementing [ErrorInformant] in order to display to the user information about errors */
    private lateinit var errorInformant: ErrorInformant

    /** Indicates loading detailed information about the [News] */
    private val isLoading = MutableLiveData(false)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        when (context) {
            !is ErrorInformant -> {
                throw IllegalStateException("Activity needs to implement ErrorInformant")
            }
            else -> {
                errorInformant = context
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTransitions()
        getArgs()
        loadAllInfo()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _viewBinding = FragmentNewsDetailBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setObservables()
        configureVebView()
    }

    /**  Sets enter transition for [NewsDetailsFragment]*/
    private fun setTransitions() {
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragment_nav_host
            scrimColor = Color.TRANSPARENT
            duration = resources.getInteger(R.integer.duration_anim_news_to_details).toLong()
            setAllContainerColors(Color.TRANSPARENT)
        }
    }

    private fun getArgs() {
        requireArguments().getParcelable<News>(News.NEWS_PARCELABLE_KEY).let { target_news ->
            if (target_news == null) {
                throw IllegalStateException("Bundle do not includes news: news = $target_news")
            }
            chosenNews = target_news
        }
    }

    private fun setObservables() {
        isLoading.observe(viewLifecycleOwner) { loading ->
            when {
                loading -> showProgressBar()
                else -> hideProgressBar()
            }
        }
    }

    private fun configureVebView() {
        viewBinding.newsDetailsWebView.run {
            // this should have the same background color as card on top of which is placed
            setBackgroundColor(Color.TRANSPARENT)

            settings.run { // works as 'auto zoom'
                loadWithOverviewMode = true
                useWideViewPort = true
                layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
            }

            webViewClient = object : WebViewClient() {
                /** ONLY when [News.articleHtml] is fetched and page has finished rendering [isLoading] should be set to false.*/
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    isLoading.value = false
                }
            }


            // turns on dark mode if supported and enabled
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK)) {
                        WebSettingsCompat.setForceDark(this.settings, FORCE_DARK_ON)
                    }
                }
            }
        }
    }

    private fun loadAllInfo() {

        isLoading.value = true

        viewModel.loadArticleHtml(chosenNews).invokeOnCompletion {
            isLoading.value = false

            chosenNews.articleHtml?.let { html ->
                setArticleHtml(html)
            } ?: run {
                informUserAboutError()
            }
        }
    }

    // website is using Webpress and inconsistent text tags and text formatting
    // so using web view seems the simplest and bets option for displaying info
    private fun setArticleHtml(articleHtml: String) {
        viewBinding.newsDetailsWebView.loadData(articleHtml, "text/html; charset=utf-8", "UTF-8")
    }

    private fun informUserAboutError() {
        isLoading.value = false
        val actionClickListener = View.OnClickListener {
            loadAllInfo()
        }

        errorInformant.showErrorSnackbar(
            this,
            resources.getString(R.string.no_interent_newspage),
            resources.getString(R.string.tap_to_try_again),
            actionClickListener
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }

    private fun showProgressBar() {
        viewBinding.loadingNewsDetailsPb.makeVisible()
    }

    private fun hideProgressBar() {
        viewBinding.loadingNewsDetailsPb.makeGone()
    }
}