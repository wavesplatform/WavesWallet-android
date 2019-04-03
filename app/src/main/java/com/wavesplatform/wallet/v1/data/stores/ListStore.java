/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v1.data.stores;

import com.wavesplatform.wallet.v1.util.ListUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class ListStore<T> {

    List<T> data;

    public ListStore() {
        data = new ArrayList<>();
    }

    public List<T> getList() {
        return data;
    }

    public void insertObjectIntoList(T object) {
        data.add(object);
    }

    public void insertBulk(List<T> objects) {
        ListUtil.addAllIfNotNull(data, objects);
    }

    public void sort(Comparator<T> objectComparator) {
        Collections.sort(data, objectComparator);
    }
}
