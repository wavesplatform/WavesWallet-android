package com.wavesplatform.wallet.ui.base;

import android.databinding.BaseObservable;
import android.support.annotation.CallSuper;

import io.reactivex.disposables.CompositeDisposable;
import com.wavesplatform.wallet.injection.Injector;

public abstract class BaseViewModel extends BaseObservable {

    public CompositeDisposable compositeDisposable;

    public BaseViewModel() {
        compositeDisposable = new CompositeDisposable();
    }

    public abstract void onViewReady();

    @CallSuper
    public void destroy() {
        /** Clear all subscriptions so that:
         * 1) all processes are cancelled
         * 2) processes don't try to update a null View
         * 3) background processes don't leak memory
         */
        compositeDisposable.clear();

        /**
         * Clear DataManagerComponent, thereby releasing all objects with a
         * {@link com.wavesplatform.wallet.injection.ViewModelScope} annotation for GC
         */
        Injector.getInstance().releaseViewModelScope();
    }

}
