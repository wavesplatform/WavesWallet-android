package com.wavesplatform.wallet.v2.ui.base.view

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.KeyEvent
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Button
import com.arellomobile.mvp.MvpAppCompatActivity
import com.franmontiel.localechanger.LocaleChanger
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.local.PreferencesHelper
import com.wavesplatform.wallet.v2.data.manager.ErrorManager
import com.wavesplatform.wallet.v2.data.manager.NodeDataManager
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPassCodeActivity
import com.wavesplatform.wallet.v2.ui.splash.SplashActivity
import com.wavesplatform.wallet.v2.util.*
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasFragmentInjector
import dagger.android.support.HasSupportFragmentInjector
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import org.fingerlinks.mobile.android.navigator.Navigator
import pers.victor.ext.click
import pyxis.uzuki.live.richutilskt.utils.hideKeyboard
import timber.log.Timber
import javax.inject.Inject

abstract class BaseActivity : MvpAppCompatActivity(), BaseView, BaseMvpView, HasFragmentInjector, HasSupportFragmentInjector {
    private val mCompositeDisposable = CompositeDisposable()
    var eventSubscriptions: CompositeDisposable = CompositeDisposable()

    lateinit var toolbar: Toolbar
    var translucentStatusBar = false
    protected var mActionBar: ActionBar? = null
    val baseFragmentManager: FragmentManager
        get() = super.getSupportFragmentManager()
    val fragmentContainer: Int
        @IdRes get() = 0

    @Inject
    lateinit var supportFragmentInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    lateinit var frameworkFragmentInjector: DispatchingAndroidInjector<android.app.Fragment>


    @Inject
    lateinit var mRxEventBus: RxEventBus
    @Inject
    lateinit var prefsUtil: PrefsUtil
    @Inject
    lateinit var mErrorManager: ErrorManager
    @Inject
    lateinit var nodeDataManager: NodeDataManager
    @Inject
    lateinit var preferencesHelper: PreferencesHelper

    private var progressDialog: ProgressDialog? = null

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return supportFragmentInjector
    }

    override fun fragmentInjector(): AndroidInjector<android.app.Fragment>? {
        return frameworkFragmentInjector
    }

    override fun attachBaseContext(newBase: Context?) {
        val baseContext = LocaleChanger.configureBaseContext(newBase)
        super.attachBaseContext(baseContext)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        if (!translucentStatusBar) {
            setStatusBarColor(R.color.white)
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(configLayoutRes())
        Timber.tag(javaClass.simpleName)
        onViewReady(savedInstanceState)
    }

    protected fun checkInternet() {
        if (!isNetworkConnection()) {
            val dialog = Dialog(this, R.style.AppThemeV2_NoActionBar_Translucent_DarkStatusBar)
            dialog.setContentView(R.layout.dialog_no_internet)
            dialog.setCancelable(false)
            dialog.findViewById<Button>(R.id.button_retry).click {
                if (isNetworkConnection()) {
                    launchActivity<SplashActivity>(clear = true)
                } else {
                    showMessage(getString(R.string.no_internet_title), dialog.findViewById<Button>(R.id.root))
                }
            }
            dialog.setOnKeyListener(DialogInterface.OnKeyListener { _, keyCode, _ ->
                if ((keyCode == KeyEvent.KEYCODE_BACK)) {
                    launchActivity<SplashActivity>(clear = true) {
                        putExtra(SplashActivity.EXIT, true)
                    }
                    return@OnKeyListener true
                }
                false
            })
            dialog.show()
        }
    }

    public override fun onResume() {
        super.onResume()

        if (this is SplashActivity) {
            return
        }

        if (!isNetworkConnection()) {
            checkInternet()
            return
        }

        askPassCodeIfNeed()
        mCompositeDisposable.add(mRxEventBus.filteredObservable(Events.ErrorEvent::class.java)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ errorEvent ->
                    mErrorManager.showError(this,
                            errorEvent.retrofitException, errorEvent.retrySubject)
                }, { t: Throwable? -> t?.printStackTrace() }))
    }

    public override fun onPause() {
        mCompositeDisposable.clear()
        super.onPause()
    }

    override fun onDestroy() {
        eventSubscriptions.clear()
        super.onDestroy()
    }

    protected open fun askPassCode() = true

    private fun askPassCodeIfNeed() {
        val guid = App.getAccessManager().getLastLoggedInGuid()

        val notAuthenticated = App.getAccessManager().getWallet() == null
        val hasGuidToLogin = !TextUtils.isEmpty(guid)

        if (hasGuidToLogin && notAuthenticated && askPassCode()) {
            launchActivity<EnterPassCodeActivity>(
                    requestCode = EnterPassCodeActivity.REQUEST_ENTER_PASS_CODE) {
                putExtra(EnterPassCodeActivity.KEY_INTENT_GUID, guid)
            }
        }
    }

    @JvmOverloads
    inline fun setupToolbar(toolbar: Toolbar, homeEnable: Boolean = false,
                            title: String = "", @DrawableRes icon: Int = R.drawable.ic_arrow_back_white_24dp,
                            crossinline onClickListener: () -> Unit = { onBackPressed() }) {
        this.toolbar = toolbar
        setSupportActionBar(toolbar)
        mActionBar = supportActionBar

        mActionBar?.setHomeButtonEnabled(homeEnable)
        mActionBar?.setDisplayHomeAsUpEnabled(homeEnable)


        mActionBar?.setHomeAsUpIndicator(AppCompatResources.getDrawable(this, icon))

        if (title.isNotEmpty()) mActionBar?.title = title
        else mActionBar?.title = " "

        toolbar.setNavigationOnClickListener {
            hideKeyboard()
            onClickListener()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun setTitle(title: Int) {
        super.setTitle(title)
        if (mActionBar != null)
            mActionBar!!.title = getString(title)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1)
            supportFragmentManager.popBackStack()
        else
            finish()
    }

    override fun showNetworkError() {
        showSnackbar(R.string.check_connectivity_exit)
    }

    fun showNetworkErrorWithRetry(retrySubject: PublishSubject<Events.RetryEvent>) {
        Snackbar.make(findViewById(android.R.id.content), getString(R.string.check_connectivity_exit), Snackbar.LENGTH_INDEFINITE)
//                .setAction(getString(R.string.error_network_retry_text), { retrySubject.onNext(Events.RetryEvent()) })
                .show()
    }

    override fun showProgressBar(isShowProgress: Boolean) {
        pyxis.uzuki.live.richutilskt.utils.runOnUiThread {
            if (isShowProgress) {
                if (progressDialog == null || !progressDialog?.isShowing!!) {
                    progressDialog = ProgressDialog(this)
                    progressDialog?.setCancelable(false)
                    progressDialog?.setMessage(getString(R.string.dialog_processing))
                    progressDialog?.show()
                } else {
                    progressDialog?.show()
                }
            } else if (progressDialog != null && progressDialog!!.isShowing) {
                progressDialog?.dismiss()
                progressDialog = null
            }
        }
    }

    fun openFragment(fragmentContainer: Int, fragment: Fragment) {
        val canGoBack = Navigator.with(this).utils()
                .canGoBackToSpecificPoint(fragment::class.java.simpleName, fragmentContainer, supportFragmentManager)
        if (canGoBack) {
            Navigator.with(this)
                    .utils()
                    .goBackToSpecificPoint(fragment::class.java.simpleName)
        } else {
            Navigator.with(this)
                    .build()
                    .goTo(fragment, fragmentContainer)
                    .addToBackStack()
                    .tag(fragment::class.java.simpleName)
                    .replace()
                    .commit()
        }
    }

    protected fun setStatusBarColor(@ColorRes intColorRes: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, intColorRes)
        }
    }

    protected fun setNavigationBarColor(@ColorRes intColorRes: Int) {
        window.navigationBarColor = ContextCompat.getColor(this, intColorRes)
    }

    protected fun restartApp() {
        App.getAccessManager().restartApp(this)
    }

    protected abstract fun onViewReady(savedInstanceState: Bundle?)

}
