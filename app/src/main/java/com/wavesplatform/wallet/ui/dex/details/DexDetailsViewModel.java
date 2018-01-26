package com.wavesplatform.wallet.ui.dex.details;

import android.content.Context;

import com.wavesplatform.wallet.data.Events;
import com.wavesplatform.wallet.injection.Injector;
import com.wavesplatform.wallet.ui.base.BaseViewModel;
import com.wavesplatform.wallet.util.RxEventBus;
import com.wavesplatform.wallet.util.SSLVerifyUtil;
import com.wavesplatform.wallet.util.annotations.Thunk;

import javax.inject.Inject;

@SuppressWarnings("WeakerAccess")
public class DexDetailsViewModel extends BaseViewModel {

    private Context context;
    public DexDetailsModel dexDetailsModel;
    @Inject
    RxEventBus mRxEventBus;

    @Thunk
    DataListener dataListener;
    @Inject
    SSLVerifyUtil sslVerifyUtil;

    DexDetailsViewModel(Context context, DataListener dataListener) {

        Injector.getInstance().getDataManagerComponent().inject(this);

        this.context = context;
        this.dataListener = dataListener;
        dexDetailsModel = new DexDetailsModel();

        sslVerifyUtil.validateSSL();
    }

    public void sendEventChangeTimeFrame(CharSequence title) {
        mRxEventBus.post(new Events.ChangeTimeFrame(title.toString()));
    }

    public interface DataListener {

    }

    @Override
    public void onViewReady() {

    }
}
