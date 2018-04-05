package com.wavesplatform.wallet.v1.ui.base;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import io.reactivex.disposables.CompositeDisposable;
import com.wavesplatform.wallet.BuildConfig;
import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.v1.data.access.AccessState;
import com.wavesplatform.wallet.v1.data.rxjava.RxUtil;
import com.wavesplatform.wallet.v1.util.ApplicationLifeCycle;
import com.wavesplatform.wallet.v1.util.SSLVerifyUtil;

/**
 * A base Activity for all activities which need auth timeouts & screenshot prevention
 */

public class BaseAuthActivity extends AppCompatActivity {

    private AlertDialog mAlertDialog;
    private SSLVerifyUtil mSSLVerifyUtil = new SSLVerifyUtil(this);
    private static CompositeDisposable compositeDisposable;

    @CallSuper
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (!BuildConfig.DOGFOOD && !BuildConfig.DEBUG) {
            disallowScreenshots();
        }

        compositeDisposable = new CompositeDisposable();

        // Subscribe to SSL pinning events
        compositeDisposable.add(
                mSSLVerifyUtil.getSslPinningSubject()
                        .compose(RxUtil.applySchedulersToObservable())
                        .subscribe(sslEvent -> {
                                    switch (sslEvent) {
                                        case ServerDown:
                                            showAlertDialog(getString(R.string.ssl_no_connection), false);
                                            break;
                                        case PinningFail:
                                            showAlertDialog(getString(R.string.ssl_pinning_invalid), true);
                                            break;
                                        case NoConnection:
                                            showAlertDialog(getString(R.string.ssl_no_connection), false);
                                            break;
                                        case Success:
                                            // No-op
                                        default:
                                            // No-op
                                    }
                                },
                                Throwable::printStackTrace));
    }

    @CallSuper
    @Override
    protected void onResume() {
        super.onResume();
        ApplicationLifeCycle.getInstance().onActivityResumed();
    }

    @CallSuper
    @Override
    protected void onPause() {
        super.onPause();
        ApplicationLifeCycle.getInstance().onActivityPaused();
    }

    @CallSuper
    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        if (mAlertDialog != null) {
            mAlertDialog.dismiss();
        }
    }

    /**
     * Override if you want a particular activity to be able to be screenshot.
     */
    protected void disallowScreenshots() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
    }

    private void showAlertDialog(final String message, final boolean forceExit) {
        if (mAlertDialog != null) mAlertDialog.dismiss();

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setMessage(message)
                .setCancelable(false);

        if (!forceExit) {
            builder.setPositiveButton(R.string.retry, (d, id) -> {
                // Retry
                mSSLVerifyUtil.validateSSL();
            });
        }

        builder.setNegativeButton(R.string.exit, (d, id) -> finish());

        mAlertDialog = builder.create();

        if (!isFinishing()) {
            mAlertDialog.show();
        }
    }
}
