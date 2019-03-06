package com.wavesplatform.wallet.v2.ui.base.view

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import android.support.annotation.ColorRes
import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.ActionBar
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.Button
import com.akexorcist.localizationactivity.core.LocalizationActivityDelegate
import com.akexorcist.localizationactivity.core.OnLocaleChangedListener
import com.arellomobile.mvp.MvpAppCompatActivity
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork
import com.google.firebase.analytics.FirebaseAnalytics
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.PrefsUtil
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.local.PreferencesHelper
import com.wavesplatform.wallet.v2.data.manager.ErrorManager
import com.wavesplatform.wallet.v2.data.manager.NodeDataManager
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPassCodeActivity
import com.wavesplatform.wallet.v2.ui.splash.SplashActivity
import com.wavesplatform.wallet.v2.ui.welcome.WelcomeActivity
import com.wavesplatform.wallet.v2.util.*
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasFragmentInjector
import dagger.android.support.HasSupportFragmentInjector
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.no_internet_bottom_message_layout.view.*
import org.fingerlinks.mobile.android.navigator.Navigator
import pers.victor.ext.click
import pyxis.uzuki.live.richutilskt.utils.hideKeyboard
import pyxis.uzuki.live.richutilskt.utils.inflate
import timber.log.Timber
import java.util.*
import javax.inject.Inject

abstract class BaseActivity : MvpAppCompatActivity(), BaseView, BaseMvpView, HasFragmentInjector,
        HasSupportFragmentInjector, OnLocaleChangedListener {
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
    private val localizationDelegate = LocalizationActivityDelegate(this)
    protected lateinit var firebaseAnalytics: FirebaseAnalytics

    private var noInternetLayout: View? = null

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return supportFragmentInjector
    }

    override fun fragmentInjector(): AndroidInjector<android.app.Fragment>? {
        return frameworkFragmentInjector
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(localizationDelegate.attachBaseContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        localizationDelegate.addOnLocaleChangedListener(this)
        localizationDelegate.onCreate(savedInstanceState)
        super.onCreate(savedInstanceState)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        if (!translucentStatusBar) {
            setStatusBarColor(R.color.white)
            setNavigationBarColor(R.color.white)
        }
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(configLayoutRes())
        Timber.tag(javaClass.simpleName)
        onViewReady(savedInstanceState)

        noInternetLayout = inflate(R.layout.no_internet_bottom_message_layout)

        eventSubscriptions.add(ReactiveNetwork
                .observeInternetConnectivity()
                .distinctUntilChanged()
                .onErrorResumeNext(Observable.empty())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ connected ->
                    onNetworkConnectionChanged(connected)
                }, {
                    it.printStackTrace()
                }))
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
                    exit()
                    return@OnKeyListener true
                }
                false
            })
            dialog.show()
        }
    }

    protected fun exit() {
        launchActivity<SplashActivity>(clear = true) {
            putExtra(SplashActivity.EXIT, true)
        }
    }

    public override fun onResume() {
        super.onResume()

        if (this is SplashActivity) {
            return
        }

        localizationDelegate.onResume(this)

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
    inline fun setupToolbar(
        toolbar: Toolbar,
        homeEnable: Boolean = false,
        title: String = "",
        @DrawableRes icon: Int = R.drawable.ic_arrow_back_white_24dp,
        crossinline onClickListener: () -> Unit = { onBackPressed() }
    ) {
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
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = ContextCompat.getColor(this, intColorRes)
        } else {
            window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        }
    }

    protected fun setNavigationBarColor(@ColorRes intColorRes: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            window.navigationBarColor = ContextCompat.getColor(this, intColorRes)
        } else {
            window.navigationBarColor = ContextCompat.getColor(this, R.color.black)
        }
    }

    protected fun restartApp() {
        App.getAccessManager().restartApp(this)
    }

    protected fun clearAndLogout() {
        App.getAccessManager().setLastLoggedInGuid("")
        App.getAccessManager().resetWallet()
        launchActivity<WelcomeActivity>(clear = true)
    }

    protected abstract fun onViewReady(savedInstanceState: Bundle?)

    override fun getApplicationContext(): Context {
        return localizationDelegate.getApplicationContext(super.getApplicationContext())
    }

    override fun getResources(): Resources {
        return localizationDelegate.getResources(super.getResources())
    }

    fun setLanguage(language: String) {
        localizationDelegate.setLanguage(this, language)
    }

    fun setLanguage(locale: Locale) {
        localizationDelegate.setLanguage(this, locale)
    }

    fun setDefaultLanguage(language: String) {
        localizationDelegate.setDefaultLanguage(language)
    }

    fun setDefaultLanguage(locale: Locale) {
        localizationDelegate.setDefaultLanguage(locale)
    }

    fun getCurrentLanguage(): Locale {
        return localizationDelegate.getLanguage(this)
    }

    override fun onBeforeLocaleChanged() {}

    override fun onAfterLocaleChanged() {}

    override fun onNetworkConnectionChanged(networkConnected: Boolean) {
        if (needToShowNetworkMessage()) {
            if (networkConnected) {
                if (noInternetLayout?.parent != null) {
                    noInternetLayout?.image_no_internet?.clearAnimation()
                    rootLayoutToShowNetworkMessage().removeView(noInternetLayout)
                }
            } else {
                if (noInternetLayout?.parent == null) {
                    noInternetLayout?.linear_no_internet_message?.setMargins(0, 0, 0, extraBottomMarginToShowNetworkMessage())
                    rootLayoutToShowNetworkMessage().addView(noInternetLayout)
                    noInternetLayout?.image_no_internet?.startAnimation(AnimationUtils.loadAnimation(this, R.anim.easy_rotate))
                }
            }
        }
    }

    fun snakeAnimationForNetworkMsg() {
        val animation = AnimationUtils.loadAnimation(this, R.anim.easy_shake_error)
        noInternetLayout?.startAnimation(animation)
    }

    open fun needToShowNetworkMessage(): Boolean = false

    open fun extraBottomMarginToShowNetworkMessage(): Int = 0

    open fun rootLayoutToShowNetworkMessage(): ViewGroup = findViewById(android.R.id.content)
}
