package com.wavesplatform.wallet.v1.ui.home;

import android.content.Context;
import android.util.Log;

import com.wavesplatform.wallet.v1.api.NodeManager;
import com.wavesplatform.wallet.v1.data.connectivity.ConnectivityStatus;
import com.wavesplatform.wallet.v1.data.rxjava.RxUtil;
import com.wavesplatform.wallet.v1.injection.Injector;
import com.wavesplatform.wallet.v1.ui.base.BaseViewModel;
import com.wavesplatform.wallet.v1.util.AppUtil;
import com.wavesplatform.wallet.v1.util.PrefsUtil;
import com.wavesplatform.wallet.v1.util.RootUtil;

import javax.inject.Inject;

@SuppressWarnings("WeakerAccess")
public class MainViewModel extends BaseViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();

    private Context context;
    private DataListener dataListener;
    @Inject protected PrefsUtil prefs;
    @Inject protected AppUtil appUtil;

    public interface DataListener {
        void onRooted();

        void onConnectivityFail();

        void onFetchTransactionsStart();

        void onFetchTransactionCompleted();

        void onScanInput(String strUri);

        void onStartBalanceFragment();

        void kickToLauncherPage();

        void clearAllDynamicShortcuts();
    }

    public MainViewModel(Context context, DataListener dataListener) {
        Injector.getInstance().getDataManagerComponent().inject(this);
        this.context = context;
        this.dataListener = dataListener;
    }

    @Override
    public void onViewReady() {
        checkRooted();
        checkConnectivity();
    }

    public String getIcomingUri() {
        //return "waves://3NCwEeAeVKdPySfsTeAoroPHDUg54mSDY5w?asset=Do4TJ2kXdgoaoHLr5XAXGQNK5eThVm3r1UARQNrVyb5H&amount=5002";
        return prefs.getGlobalValue(PrefsUtil.GLOBAL_SCHEME_URL, "");
    }

    public void clearIcomingUri() {
        prefs.removeGlobalValue(PrefsUtil.GLOBAL_SCHEME_URL);
    }

    private void checkRooted() {
        if (new RootUtil().isDeviceRooted() &&
                !prefs.getValue(PrefsUtil.KEY_DISABLE_ROOT_WARNING, false)) {
            dataListener.onRooted();
            prefs.setValue(PrefsUtil.KEY_DISABLE_ROOT_WARNING, true);
        }
    }

    private void checkConnectivity() {
        if (ConnectivityStatus.hasConnectivity(context)) {
            preLaunchChecks();
        } else {
            dataListener.onConnectivityFail();
        }
    }

    private void preLaunchChecks() {
        dataListener.onFetchTransactionsStart();

        if (NodeManager.get() != null) {
            NodeManager.get().loadBalancesAndTransactions()
                    .compose(RxUtil.applySchedulersToCompletable())
                    .subscribe(() -> {
                        if (dataListener != null) dataListener.onFetchTransactionCompleted();

                        String incomingUri = getIcomingUri();
                        if (!incomingUri.isEmpty()) {
                            if (dataListener != null) dataListener.onScanInput(incomingUri);
                            clearIcomingUri();
                        } else {
                            if (dataListener != null) dataListener.onStartBalanceFragment();
                        }
                    }, err -> {
                        Log.e(TAG, "preLaunchChecks: ", err);
                        if (dataListener != null) {
                            dataListener.onFetchTransactionCompleted();
                            dataListener.onConnectivityFail();
                        }
                    });
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        context = null;
        dataListener = null;
    }

    public void logout() {
        dataListener.clearAllDynamicShortcuts();
        prefs.logOut();
        appUtil.restartApp();
    }

    public boolean areLauncherShortcutsEnabled() {
        return prefs.getValue(PrefsUtil.KEY_RECEIVE_SHORTCUTS_ENABLED, true);
    }

}
