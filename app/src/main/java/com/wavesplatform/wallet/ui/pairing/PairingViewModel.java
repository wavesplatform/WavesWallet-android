package com.wavesplatform.wallet.ui.pairing;

import android.support.annotation.StringRes;

import com.wavesplatform.wallet.injection.Injector;
import com.wavesplatform.wallet.ui.base.BaseViewModel;
import com.wavesplatform.wallet.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.util.AppUtil;
import com.wavesplatform.wallet.util.PrefsUtil;

import javax.inject.Inject;

public class PairingViewModel extends BaseViewModel {

    @Inject protected AppUtil appUtil;
    @Inject protected PrefsUtil prefsUtil;

    private DataListener dataListener;

    interface DataListener {

        void showToast(@StringRes int message, @ToastCustom.ToastType String toastType);

        void showProgressDialog(@StringRes int message);

        void dismissProgressDialog();

        void startSeedWalletActivity(String seed);

    }

    PairingViewModel(DataListener dataListener) {
        Injector.getInstance().getDataManagerComponent().inject(this);
        this.dataListener = dataListener;
    }

    @Override
    public void onViewReady() {
    }

    void pairWithQR(String raw) {
        if (dataListener != null)
            dataListener.startSeedWalletActivity(raw);
    }

}
