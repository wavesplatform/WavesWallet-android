package com.wavesplatform.wallet.v1.util;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.wavesplatform.wallet.R;

import java.security.Security;

import javax.inject.Inject;

public class AppUtil {


    private Context context;

    @Inject
    public AppUtil(@com.wavesplatform.wallet.v2.injection.qualifier.ApplicationContext Context context) {
        this.context = context;
    }
}