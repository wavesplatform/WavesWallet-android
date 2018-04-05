package com.wavesplatform.wallet.v1.injection;

import com.wavesplatform.wallet.BlockchainApplication;
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager;
import com.wavesplatform.wallet.v1.util.AppUtil;
import com.wavesplatform.wallet.v1.util.RxEventBus;
import com.wavesplatform.wallet.v1.util.exceptions.LoggingExceptionHandler;

import javax.inject.Singleton;

import dagger.Component;

@SuppressWarnings("WeakerAccess")
@Singleton
@Component(modules = {
        ApplicationModule.class,
        ApiModule.class
})
public interface ApplicationComponent {

    DataManagerComponent plus(DataManagerModule userModule);

    void inject(AppUtil appUtil);

    void inject(LoggingExceptionHandler loggingExceptionHandler);

    void inject(EnvironmentManager environmentManager);

    RxEventBus eventBus();
}
