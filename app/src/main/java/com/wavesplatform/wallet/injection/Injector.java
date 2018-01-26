package com.wavesplatform.wallet.injection;

import android.app.Application;
import android.content.Context;

public enum Injector {

    INSTANCE;

    private ApplicationComponent applicationComponent;
    private DataManagerComponent dataManagerComponent;

    public static Injector getInstance() {
        return INSTANCE;
    }

    public void init(Context applicationContext) {

        ApplicationModule applicationModule = new ApplicationModule((Application) applicationContext);
        ApiModule apiModule = new ApiModule();
        DataManagerModule managerModule = new DataManagerModule();

        initAppComponent(applicationModule, apiModule, managerModule);
    }

    protected void initAppComponent(ApplicationModule applicationModule, ApiModule apiModule, DataManagerModule managerModule) {

        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(applicationModule)
                .apiModule(apiModule)
                .build();

        dataManagerComponent = applicationComponent.plus(managerModule);
    }

    public ApplicationComponent getAppComponent() {
        return applicationComponent;
    }

    public DataManagerComponent getDataManagerComponent() {
        if (dataManagerComponent == null) {
            dataManagerComponent = applicationComponent.plus(new DataManagerModule());
        }
        return dataManagerComponent;
    }

    public void releaseViewModelScope() {
        dataManagerComponent = null;
    }
}
