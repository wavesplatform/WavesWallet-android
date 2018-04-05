package com.wavesplatform.wallet.v1.util;

import android.content.Context;
import android.util.Log;

import org.thoughtcrime.ssl.pinning.util.PinningHelper;

import java.io.IOException;
import java.net.URL;

import javax.inject.Inject;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import com.wavesplatform.wallet.v1.data.connectivity.ConnectivityStatus;
import com.wavesplatform.wallet.v1.util.annotations.Thunk;

// openssl s_client -showcerts -connect blockchain.info:443

public class SSLVerifyUtil {

    @SuppressWarnings("WeakerAccess")
    @Thunk
    static final String TAG = SSLVerifyUtil.class.getSimpleName();
    @SuppressWarnings("WeakerAccess")
    @Thunk
    static final Subject<SslEvent> mSslPinningSubject = PublishSubject.create();

    private Context context;

    @Inject
    public SSLVerifyUtil(Context context) {
        this.context = context;
    }

    public void validateSSL() {
        if (ConnectivityStatus.hasConnectivity(context)) {
            checkSslStatus(new CertificateCheckInterface() {
                @Override
                public void onSuccess() {
                    Log.i(TAG, "SSL pinning completed successfully");
                    mSslPinningSubject.onNext(SslEvent.Success);
                }

                @Override
                public void onServerDown() {
                    mSslPinningSubject.onNext(SslEvent.ServerDown);
                }

                @Override
                public void onPinningFail() {
                    mSslPinningSubject.onNext(SslEvent.PinningFail);
                }
            });
        } else {
            mSslPinningSubject.onNext(SslEvent.NoConnection);
        }
    }

    public Subject<SslEvent> getSslPinningSubject() {
        return mSslPinningSubject;
    }

    @SuppressWarnings("WeakerAccess")
    public enum SslEvent {
        Success,
        ServerDown,
        PinningFail,
        NoConnection
    }

    interface CertificateCheckInterface {

        void onSuccess();

        void onServerDown();

        void onPinningFail();
    }

    private void checkSslStatus(CertificateCheckInterface listener) {
        getPinnedConnection()
                .subscribe(httpsURLConnection -> {
                    try {
                        byte[] data = new byte[4096];
                        int read = httpsURLConnection.getInputStream().read(data);
                        listener.onSuccess();

                    } catch (IOException e) {
                        e.printStackTrace();

                        if (e instanceof SSLHandshakeException) {
                            listener.onPinningFail();
                        } else {
                            listener.onServerDown();
                        }
                    }
                }, throwable -> {
                    listener.onServerDown();
                });
    }

    private Observable<HttpsURLConnection> getPinnedConnection() {
        return Observable.fromCallable(() -> {
            String[] pins = new String[]{"10902ad9c6fb7d84c133b8682a7e7e30a5b6fb90"};
            URL url = new URL("https://blockchain.info/");
            return PinningHelper.getPinnedHttpsURLConnection(context, pins, url);
        }).subscribeOn(Schedulers.io());
    }
}