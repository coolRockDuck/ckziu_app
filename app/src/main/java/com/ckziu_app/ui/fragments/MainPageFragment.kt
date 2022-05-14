package com.ckziu_app.ui.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.ckziu_app.model.*
import com.ckziu_app.ui.adapters.MiniNewsRecViewAdapter
import com.ckziu_app.ui.helpers.ErrorInformant
import com.ckziu_app.ui.helpers.ScrollControllerInterface
import com.ckziu_app.ui.viewmodels.MainPageViewModel
import com.ckziu_app.utils.makeGone
import com.ckziu_app.utils.makeVisible
import com.example.ckziuapp.R
import com.example.ckziuapp.databinding.FragmentMainpageBinding
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Fragment displaying information about main page from school`s [website](http://ckziu.olawa.pl/) */
@AndroidEntryPoint
class MainPageFragment : Fragment(R.layout.fragment_mainpage), ScrollControllerInterface {

    companion object {
        const val TAG = "MainPageFragment"
    }

    private var _viewBinding: FragmentMainpageBinding? = null
    private val viewBinding get() = _viewBinding!!

    private lateinit var errorInformant: ErrorInformant

    private val viewModel by viewModels<MainPageViewModel>()

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
        _viewBinding = FragmentMainpageBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showProgressBar()
        setObservables()
    }

    private fun showMiniNews(listOfMiniNews: List<News>) {
        viewBinding.newsRvMainpage.adapter = MiniNewsRecViewAdapter(listOfMiniNews)
    }

    private fun loadPromoPhotoIntoView(url: String) {
        val callback = object : Callback {
            override fun onSuccess() {
                hideProgressBar()
            }

            override fun onError(e: Exception?) {
                hideProgressBar()
            }
        }

        Picasso.get()
            .load(url)
            .placeholder(R.drawable.ic_loading_placeholder_dark)
            .into(viewBinding.ivPromoPhotos, callback)

    }


    private fun setObservables() {
        viewModel.mainPageInfo.observe(viewLifecycleOwner) { mainPageInfoResult ->
            when (mainPageInfoResult) {
                is Success -> {
                    val mainPageInfo = mainPageInfoResult.resultValue
                    loadPromoPhotoIntoView(mainPageInfo.promoPhotosLinks[0])
                    showMiniNews(mainPageInfo.miniListOfNews)
                    populateUiWithInfo(mainPageInfo)
                    errorInformant.hideErrorSnackbar()
                }

                is InProgress -> {
                    showProgressBar()
                }

                is Failure -> {
                    hideProgressBar()
                    showErrorInfo()
                }
            }
        }
    }

    private fun showErrorInfo() {
        hideProgressBar()

        errorInformant.showErrorSnackbar(
            this,
            resources.getString(R.string.no_internet_mainpage),
            resources.getString(R.string.tap_to_try_again),
            null
        )
    }

    private fun populateUiWithInfo(mainPageInfo: MainPageInfo) {
        viewBinding.run {
            mainPageInfo.let { info ->
                tvTitleMp.text = info.title
                animatePromoNumbers(info.promoNumbers)
                tvPromotextMainpage.text = info.promoText

                listOf(
                    dividerTvTitleMp.root,
                    tvPromotextMainpage,
                    promoNumbersLayout.root,
                    tvRcMiniNews,
                    dividerTvRcMiniNews.root,
                ).forEach { view ->
                    view.makeVisible()
                }
            }
        }
    }

    /** Displays iteration through statistics of which the school is proud.*/
    private fun animatePromoNumbers(promoNumbers: PromoNumbers) {

        fun animateCounter(counter: TextView?, targetAmount: Int) {
            lifecycleScope.launch {
                for (index: Int in 1..targetAmount) {
                    val preferredDelay = (300 - (index * index * index)).toLong()
                    val delayMils = if (preferredDelay > 0) preferredDelay else 1
                    delay(delayMils)
                    counter?.text = index.toString()
                }
            }
        }
        viewBinding.promoNumbersLayout.let { vb ->
            promoNumbers.run {
                animateCounter(vb.tvCounterNumOfStudents, amountOfStudents)
                animateCounter(vb.tvCounterNumOfBestStudents, amountOfBestStudents)
                animateCounter(vb.tvCounterNumOfTeachers, amountOfTeachers)
                animateCounter(vb.tvCounterYearsExperience, amountOfExperienceYrs)
            }
        }
    }

    private fun hideProgressBar() {
        // viewBinding could be equal to null at this point so safe call is necessary
        _viewBinding?.pbMainpage.makeGone()
    }

    private fun showProgressBar() {
        _viewBinding?.pbMainpage.makeVisible()
    }

    override fun scrollToTheTop() {
        viewBinding.svTopScrollMainpage.smoothScrollTo(0, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }
}
