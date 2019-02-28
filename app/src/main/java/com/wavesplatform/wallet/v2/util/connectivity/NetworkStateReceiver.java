package com.wavesplatform.wallet.v2.util.connectivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;


class NetworkStateReceiver extends BroadcastReceiver {

    public static final String ACTION_INTENT = "com.wavesplatform.wallet.v1.v1.ui..REFRESH";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent.getExtras() != null) {
            final ConnectivityManager connectivityManager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
                LocalBroadcastManager.getInstance(context).sendBroadcastSync(new Intent(ACTION_INTENT));
            }
        }
    }
}