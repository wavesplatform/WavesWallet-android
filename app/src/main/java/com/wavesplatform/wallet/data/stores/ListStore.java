package com.wavesplatform.wallet.data.stores;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.wavesplatform.wallet.util.ListUtil;

public abstract class ListStore<T> {

    List<T> data;

    public ListStore() {
        data = new ArrayList<>();
    }

    public List<T> getList() {
        return data;
    }

    public void storeList(List<T> data) {
        this.data = data;
    }

    public void clearList() {
        data.clear();
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
