package com.wavesplatform.wallet.v1.util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ApplicationLifeCycle {

    private static ApplicationLifeCycle instance;
    private List<LifeCycleListener> listeners = new CopyOnWriteArrayList<>();
    private boolean foreground = false;

    public interface LifeCycleListener {
        void onBecameForeground();
        void onBecameBackground();
    }

    private ApplicationLifeCycle() {
        // Hidden constructor
    }

    public static ApplicationLifeCycle getInstance() {
        if (instance == null)
            instance = new ApplicationLifeCycle();
        return instance;
    }

    public boolean isForeground() {
        return foreground;
    }

    public boolean isBackground() {
        return !foreground;
    }

    public void addListener(LifeCycleListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
}
