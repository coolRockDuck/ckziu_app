package com.ckziu_app.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.ckziu_app.data.local.NewsDataBase
import com.ckziu_app.data.network.LessonScheduleGetter
import com.ckziu_app.data.network.MainPageInfoGetter
import com.ckziu_app.data.network.NewsGetter
import com.ckziu_app.data.repositories.LessonsScheduleRepository
import com.ckziu_app.data.repositories.MainPageRepository
import com.ckziu_app.data.repositories.NewsRepository
import com.ckziu_app.di.RepositoryProvider
import com.ckziu_app.di.RepositoryProviderImpl
import com.ckziu_app.ui.helpers.ErrorInformant
import com.ckziu_app.ui.helpers.ScrollControllerInterface
import com.ckziu_app.ui.helpers.SnackbarController
import com.ckziu_app.ui.viewmodels.*
import com.ckziu_app.utils.makeGone
import com.ckziu_app.utils.makeVisible
import com.example.ckziuapp.R
import com.example.ckziuapp.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Main activity of the application.
 *
 * Implements [ErrorInformant] in order to be able to display error information
 * coming from the fragments. */
class MainActivity : AppCompatActivity(), ErrorInformant, RepositoryProvider {

    companion object {
        const val TAG = "MainActivity"
        const val PREFERENCES_KEY = "PREFERENCES"
    }

    private lateinit var viewBinding: ActivityMainBinding

    /** This object is needed in order to manually injects
     * repositories. */
    private val repositoryProviderImpl = RepositoryProviderImpl()

    /** Instance of the class which helps with displaying error to the user.
     * @see ErrorInformant */
    private val snackbarController = SnackbarController(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        initDI()
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(viewBinding.root)

        prepareNavigationUI()
        showSplashScreen()
    }

    /** Configures action bar and bottom navigation bar */
    private fun prepareNavigationUI() {
        prepareActionBar()

        val navHost = supportFragmentManager.findFragmentById(R.id.fragment_nav_host)
        prepareBottomNavBar((navHost as NavHostFragment).navController)
    }

    private fun prepareActionBar() {
        viewBinding.run {
            navigationView.setNavigationItemSelectedListener { item ->
                val url = when (item.itemId) {
                    R.id.item_oldverion_lessons_schedule -> "http://ckziu.olawa.pl/planlekcji/index.html"
                    R.id.item_classregister -> "https://uonetplus.vulcan.net.pl/powiatolawski/"
                    R.id.item_bip -> "http://www.ckziuolawa.szkolnybip.pl/"
                    else -> {
                        Log.w(TAG, "Unsupported item: ${item.title}")
                        return@setNavigationItemSelectedListener false
                    }
                }

                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                    startActivity(this)
                }

                return@setNavigationItemSelectedListener false
            }

            toolbar.materialToolbar.setOnClickListener {
                viewBinding.root.open() // open navigation drawer
            }

            setSupportActionBar(toolbar.materialToolbar)

            supportActionBar?.run {
                //adding padding to logo so title won't be too close to it
                val logo = ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_ckziu_toolbar)
                // the easiest way to add space between the logo and the title, InsetDrawable does NOT keeps proportions
                title = "\t" + title
                setIcon(logo)
            }
        }
    }

    private fun prepareBottomNavBar(navController: NavController) {
        viewBinding.run {
            bottomNavbar.setupWithNavController(navController)
            bottomNavbar.setOnNavigationItemSelectedListener { selectedItem ->

                // if destination which is already selected is selected again, fragment should be scrolled to the top
                if (selectedItem.itemId == navController.currentDestination?.id) {

                    val navHostFragment =
                        supportFragmentManager.findFragmentById(R.id.fragment_nav_host)!!
                    val activeFragment =
                        navHostFragment.childFragmentManager.primaryNavigationFragment

                    /** see docs of [ScrollControllerInterface] and [ScrollControllerInterface.scrollToTheTop]*/
                    if (activeFragment !is ScrollControllerInterface) {
                        val errmsg = "prepareBottomNavBar: Every destination fragment needs to " +
                                "implement ScrollToTheTop interface."
                        Log.e(TAG, errmsg)
                    } else {
                        (activeFragment).scrollToTheTop()
                    }

                    return@setOnNavigationItemSelectedListener false
                }

                navController.navigate(selectedItem.itemId)

                return@setOnNavigationItemSelectedListener true
            }
        }
    }

    override fun showErrorSnackbar(
        lifecycleOwner: LifecycleOwner,
        errorMsg: String,
        actionMsg: String?,
        actionClickListener: View.OnClickListener?
    ) {
        snackbarController.showSnackbar(lifecycleOwner, errorMsg, actionMsg, actionClickListener)
    }


    override fun hideErrorSnackbar() {
        snackbarController.hideSnackbar()
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.fragment_nav_host).navigateUp() || super.onSupportNavigateUp()
    }

    private fun showSplashScreen() {
        viewBinding.run {
            lifecycleScope.launch {
                splashScreen.makeVisible()
                delay(1_000)
                splashScreen.makeGone()
            }
        }
    }

    /** Creates all dependencies.
     * @see RepositoryProvider*/
    private fun initDI() {
        repositoryProviderImpl.run {
            createMainPageRepo(MainPageInfoGetter())
            createNewsRepo(
                applicationContext,
                NewsGetter(),
                NewsDataBase.getInstance(applicationContext)
            )
            createScheduleRepo(LessonScheduleGetter())
        }
    }

    override fun getMainPageRepo(): MainPageRepository {
        return repositoryProviderImpl.mainPageRepository
    }

    override fun getNewsRepo(): NewsRepository {
        return repositoryProviderImpl.newsRepository
    }

    override fun getScheduleRepo(): LessonsScheduleRepository {
        return repositoryProviderImpl.lessonsScheduleRepository
    }

    override fun onDestroy() {
        snackbarController.destroySnackbarController() // preventing memory leaks
        super.onDestroy()
    }
}

