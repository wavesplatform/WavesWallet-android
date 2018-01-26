package com.wavesplatform.wallet.util;

import android.os.Handler;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ApplicationLifeCycle {

    private static final String TAG = ApplicationLifeCycle.class.getSimpleName();
    private static final long CHECK_DELAY = 500;

    private static ApplicationLifeCycle instance;
    private List<LifeCycleListener> listeners = new CopyOnWriteArrayList<>();
    private boolean foreground = false;
    private boolean paused = true;
    private Handler handler = new Handler();
    private Runnable runnable;

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

    public void removeListener(LifeCycleListener listener) {
        listeners.remove(listener);
    }

    /**
     * To be called from your base activity class in onResume
     */
    public void onActivityResumed() {
        paused = false;
        boolean wasBackground = !foreground;
        foreground = true;

        if (handler != null) {
            handler.removeCallbacks(runnable);
        }

        //noinspection StatementWithEmptyBody
        if (wasBackground) {
            for (LifeCycleListener listener : listeners) {
                try {
                    listener.onBecameForeground();
                } catch (Exception exc) {
                    Log.wtf(TAG, "Listener threw exception!", exc);
                }
            }
        } else {
            // Still in the foreground
        }
    }

    /**
     * To be called from your base activity class in onPause
     */
    public void onActivityPaused() {
        paused = true;

        if (handler != null) {
            handler.removeCallbacks(runnable);

            handler.postDelayed(runnable = () -> {
                //noinspection StatementWithEmptyBody
                if (foreground && paused) {
                    foreground = false;
                    for (LifeCycleListener l : listeners) {
                        try {
                            l.onBecameBackground();
                        } catch (Exception exc) {
                            Log.wtf(TAG, "Listener threw exception!", exc);
                        }
                    }
                } else {
                    // Still in the foreground
                }
            }, CHECK_DELAY);
        }
    }

}
