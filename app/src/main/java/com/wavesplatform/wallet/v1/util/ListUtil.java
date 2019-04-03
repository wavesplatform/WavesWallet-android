/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v1.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ListUtil {

    public static <E> void addAllIfNotNull(@NonNull List<E> list,
                                           @Nullable Collection<? extends E> collection) {
        if (collection != null) {
            list.addAll(collection);
        }
    }
}
