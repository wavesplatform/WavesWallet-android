package com.wavesplatform.wallet.injection;

import android.content.Context;

import com.wavesplatform.wallet.data.datamanagers.ReceiveDataManager;
import com.wavesplatform.wallet.data.datamanagers.TransactionListDataManager;
import com.wavesplatform.wallet.data.fingerprint.FingerprintAuthImpl;
import com.wavesplatform.wallet.data.stores.TransactionListStore;
import com.wavesplatform.wallet.ui.assets.AssetsHelper;
import com.wavesplatform.wallet.ui.fingerprint.FingerprintHelper;
import com.wavesplatform.wallet.util.PrefsUtil;

import dagger.Module;
import dagger.Provides;

@Module
public class DataManagerModule {


    @Provides
    @ViewModelScope
    protected ReceiveDataManager provideReceiveDataManager() {
        return new ReceiveDataManager();
    }

    @Provides
    @ViewModelScope
    protected AssetsHelper provideWalletAccountHelper(PrefsUtil prefsUtil) {
        return new AssetsHelper(prefsUtil);
    }

    @Provides
    @ViewModelScope
    protected TransactionListDataManager provideTransactionListDataManager(TransactionListStore transactionListStore) {
        return new TransactionListDataManager(transactionListStore);
    }

    @Provides
    @ViewModelScope
    protected FingerprintHelper provideFingerprintHelper(Context applicationContext,
                                                         PrefsUtil prefsUtil) {
        return new FingerprintHelper(applicationContext, prefsUtil, new FingerprintAuthImpl());
    }

}
