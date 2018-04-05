package com.wavesplatform.wallet.v1.ui.auth;

import com.wavesplatform.wallet.v1.injection.Injector;
import com.wavesplatform.wallet.v1.ui.base.BaseViewModel;
import com.wavesplatform.wallet.v1.util.PrefsUtil;

import javax.inject.Inject;

public class LandingViewModel extends BaseViewModel {
    @Inject protected PrefsUtil prefsUtil;

    public LandingViewModel() {
        Injector.getInstance().getDataManagerComponent().inject(this);
    }

    private String[] guids;

    public boolean isNoStoredKeys() {
        return guids.length == 0;
    }

    @Override
    public void onViewReady() {
        guids = prefsUtil.getGlobalValueList(EnvironmentManager.get().current().getName() + PrefsUtil.LIST_WALLET_GUIDS);
    }

}
