package com.wavesplatform.wallet.injection;

import com.wavesplatform.wallet.BlockchainApplication;
import com.wavesplatform.wallet.ui.auth.EnvironmentManager;
import com.wavesplatform.wallet.util.AppUtil;
import com.wavesplatform.wallet.util.RxEventBus;
import com.wavesplatform.wallet.util.exceptions.LoggingExceptionHandler;

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

    void inject(BlockchainApplication blockchainApplication);

    RxEventBus eventBus();
}
