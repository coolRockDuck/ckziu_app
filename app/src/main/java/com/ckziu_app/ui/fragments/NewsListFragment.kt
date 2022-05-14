package com.ckziu_app.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewGroupCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ckziu_app.model.Failure
import com.ckziu_app.model.InProgress
import com.ckziu_app.model.Success
import com.ckziu_app.ui.adapters.NewsAdapter
import com.ckziu_app.ui.helpers.ErrorInformant
import com.ckziu_app.ui.helpers.ScrollControllerInterface
import com.ckziu_app.ui.viewmodels.NewsViewModel
import com.ckziu_app.utils.getStyledColors
import com.ckziu_app.utils.makeGone
import com.ckziu_app.utils.makeVisible
import com.example.ckziuapp.R
import com.example.ckziuapp.databinding.FragmentNewslistBinding
import com.google.android.material.transition.MaterialElevationScale
import dagger.hilt.android.AndroidEntryPoint

/** Fragment presenting list of [News][com.ckziu_app.model.News].
 * When card with news it`s pressed then details are displayed
 * inside [NewsDetailsFragment] by navigating to it. */
@AndroidEntryPoint
class NewsListFragment : Fragment(R.layout.fragment_newslist), ScrollControllerInterface {

    companion object {
        const val TAG = "NewsListFragment"
        const val LAST_ACTIVE_PAGE_INDEX = "LAST_ACTIVE_PAGE_INDEX"
        const val MAX_PAGE_INDEX = "MAX_PAGE_INDEX"
    }

    private var _viewBinding: FragmentNewslistBinding? = null
    private val viewBinding get() = _viewBinding!!

    internal val viewModel by viewModels<NewsViewModel>()

    private lateinit var errorInformant: ErrorInformant

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _viewBinding = FragmentNewslistBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setTransitions()
        prepareNewsList()
    }

    /** Set reenter transition for [NewsListFragment]*/
    private fun setTransitions() {
        postponeEnterTransition()
        requireView().doOnPreDraw { startPostponedEnterTransition() }
        ViewGroupCompat.setTransitionGroup(requireView() as ViewGroup, true)

        reenterTransition = MaterialElevationScale(true).apply {
            duration = 450
        }
    }


    private fun prepareNewsList() {
        viewBinding.run {
            swiperefreshWrapper.run {
                val clr = context.getStyledColors(
                    *listOf(
                        R.attr.colorAccent,
                        R.attr.colorPrimary
                    ).toIntArray()
                )

                setColorSchemeColors(clr[0])
                setProgressBackgroundColorSchemeColor(clr[1])

                setOnRefreshListener {
                    swiperefreshWrapper.isRefreshing = true
                    val job = viewModel.refreshNewsList()
                    job.invokeOnCompletion {
                        swiperefreshWrapper.isRefreshing = false
                    }
                }
            }

            viewModel.activePageNews.observe(viewLifecycleOwner) { newsResult ->
                viewBinding.run {
                    when (newsResult) {
                        is Success -> {
                            loadingNewsPb.makeGone()

                            errorInformant.hideErrorSnackbar()
                            val newsListValue = newsResult.resultValue
                            newsRv.adapter = NewsAdapter(newsListValue, this@NewsListFragment)
                        }

                        is InProgress -> {
                            loadingNewsPb.makeVisible()
                            errorInformant.hideErrorSnackbar()
                        }

                        is Failure -> {
                            loadingNewsPb.makeGone()

                            val onActionListener =
                                View.OnClickListener { viewModel.refreshNewsList() }

                            errorInformant.showErrorSnackbar(
                                this@NewsListFragment,
                                resources.getString(R.string.no_interent_newspage),
                                resources.getString(R.string.tap_to_try_again),
                                onActionListener
                            )
                        }
                    }
                }
            }

            viewModel.maxPageIndex.observe(viewLifecycleOwner) { result ->
                if (result is Success) {
                    viewBinding.newsRv.adapter?.let { adapter ->
                        (adapter as NewsAdapter).refreshIndex()
                    }
                }
            }

            viewModel.activeNewsPageIndex.observe(viewLifecycleOwner) {
                viewBinding.newsRv.adapter?.let { adapter ->
                    (adapter as NewsAdapter).notifyItemChanged(adapter.itemCount)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveActivePageIndex() // saving temporary data in case that
        viewModel.saveMaxPageIndex()    // app it`s going to be closed

    }

    /** Scrolls to the top.
     * @see ScrollControllerInterface.scrollToTheTop*/
    override fun scrollToTheTop() {
        viewBinding.newsRv.smoothScrollToPosition(0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }
}