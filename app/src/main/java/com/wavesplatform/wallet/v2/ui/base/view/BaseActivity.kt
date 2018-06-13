package com.wavesplatform.wallet.v2.ui.base.view

import android.app.FragmentContainer
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.annotation.IdRes
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.ActionBar
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import com.arellomobile.mvp.MvpAppCompatActivity
import com.franmontiel.localechanger.LocaleChanger
import com.franmontiel.localechanger.utils.ActivityRecreationHelper
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Events
import com.wavesplatform.wallet.v2.data.local.PreferencesHelper
import com.wavesplatform.wallet.v2.data.manager.DataManager
import com.wavesplatform.wallet.v2.data.manager.ErrorManager
import com.wavesplatform.wallet.v2.util.RxEventBus
import com.wavesplatform.wallet.v2.util.RxUtil
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasFragmentInjector
import dagger.android.support.HasSupportFragmentInjector
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import org.fingerlinks.mobile.android.navigator.Navigator
import timber.log.Timber
import javax.inject.Inject

abstract class BaseActivity : MvpAppCompatActivity(), BaseView, BaseMvpView, HasFragmentInjector, HasSupportFragmentInjector {
    private val mCompositeDisposable = CompositeDisposable()
    var eventSubscriptions: CompositeDisposable = CompositeDisposable()

    lateinit var toolbar: Toolbar
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
    lateinit var mErrorManager: ErrorManager
    @Inject
    lateinit var dataManager: DataManager
    @Inject
    lateinit var preferencesHelper: PreferencesHelper
    var progressDialog: ProgressDialog? = null


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
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(configLayoutRes())

        Timber.tag(javaClass.simpleName)

        onViewReady(savedInstanceState)
    }

    public override fun onResume() {
        super.onResume()
        ActivityRecreationHelper.onResume(this)
        mCompositeDisposable.add(mRxEventBus.filteredObservable(Events.ErrorEvent::class.java)
                .compose(RxUtil.applyDefaultSchedulers())
                .subscribe({ errorEvent -> mErrorManager.showError(this, errorEvent.retrofitException, errorEvent.retrySubject) },
                        { t: Throwable? -> t?.printStackTrace() }))
    }

    override fun onDestroy() {
        ActivityRecreationHelper.onDestroy(this);
        eventSubscriptions.clear()
        super.onDestroy()
    }

    public override fun onPause() {
        mCompositeDisposable.clear()
        super.onPause()
    }

    @JvmOverloads
    fun setupToolbar(toolbar: Toolbar, onClickListener: View.OnClickListener? = null, homeEnable: Boolean = false,
                     title: String = "", @DrawableRes icon: Int = R.drawable.ic_arrow_back_white_24dp) {
        this.toolbar = toolbar
        setSupportActionBar(toolbar)
        mActionBar = supportActionBar

        mActionBar?.setHomeButtonEnabled(homeEnable)
        mActionBar?.setDisplayHomeAsUpEnabled(homeEnable)


        mActionBar?.setHomeAsUpIndicator(AppCompatResources.getDrawable(this, icon))

        if (title.isNotEmpty()) mActionBar?.title = title
        else mActionBar?.title = " "

        onClickListener?.let { toolbar.setNavigationOnClickListener(it) }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
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

    fun showSnackbar(@StringRes msg: Int) {
        Snackbar.make(findViewById(android.R.id.content), getString(msg), Snackbar.LENGTH_LONG)
                .show()
    }

    fun showSnackbar(msg: String) {
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG)
                .show()
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
//                    progressDialog?.setMessage(getString(R.string.dialog_loading))
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

    protected abstract fun onViewReady(savedInstanceState: Bundle?)

}
