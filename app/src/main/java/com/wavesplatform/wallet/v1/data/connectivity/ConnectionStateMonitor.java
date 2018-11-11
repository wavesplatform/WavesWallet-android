package com.wavesplatform.wallet.v1.data.connectivity;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;

import com.wavesplatform.wallet.v1.data.rxjava.IgnorableDefaultObserver;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class ConnectionStateMonitor extends ConnectivityManager.NetworkCallback {

    private final NetworkRequest networkRequest;
    private Context context;

    ConnectionStateMonitor(Context context) {
        this.context = context;
        networkRequest = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();
    }

    public void enable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        connectivityManager.registerNetworkCallback(networkRequest, this);
    }

    @Override
    public void onAvailable(Network network) {
        broadcastOnMainThread().subscribe(new IgnorableDefaultObserver<>());
    }

    private Completable broadcastOnMainThread() {
        return Completable.fromAction(() ->
                LocalBroadcastManager.getInstance(context)
                        .sendBroadcastSync(new Intent(NetworkStateReceiver.ACTION_INTENT)))
                .subscribeOn(AndroidSchedulers.mainThread());
    }

}
