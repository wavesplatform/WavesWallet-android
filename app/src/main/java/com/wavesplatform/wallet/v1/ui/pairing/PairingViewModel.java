package com.wavesplatform.wallet.v1.ui.pairing;

import android.support.annotation.StringRes;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.v1.injection.Injector;
import com.wavesplatform.wallet.v1.ui.base.BaseViewModel;
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.v1.util.AddressUtil;
import com.wavesplatform.wallet.v1.util.AppUtil;
import com.wavesplatform.wallet.v1.util.PrefsUtil;

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

    private boolean isLooksLikeAddress(String seed) {
        return AddressUtil.isWavesUri(seed) || AddressUtil.isValidAddress(seed);
    }

    void pairWithQR(String raw) {
        if (dataListener != null) {
            if (isLooksLikeAddress(raw)) {
                dataListener.showToast(R.string.seed_like_address, ToastCustom.TYPE_ERROR);
            } else {
                dataListener.startSeedWalletActivity(raw);
            }
        }
    }

}
