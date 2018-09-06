package com.wavesplatform.wallet.v1.data.access;

import android.util.Log;

import com.wavesplatform.wallet.v1.crypto.AESUtil;
import com.wavesplatform.wallet.v1.data.auth.WavesWallet;
import com.wavesplatform.wallet.v1.data.rxjava.RxUtil;
import com.wavesplatform.wallet.v1.data.services.PinStoreService;
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager;
import com.wavesplatform.wallet.v1.util.AppUtil;
import com.wavesplatform.wallet.v1.util.PrefsUtil;

import org.apache.commons.io.Charsets;
import org.spongycastle.util.encoders.Hex;

import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;

@Deprecated
public class AccessState {

    private static final String TAG = AccessState.class.getSimpleName();
    private static final long CLEAR_TIMEOUT_SECS = 60L;

    private PrefsUtil prefs;
    private PinStoreService pinStore;
    private AppUtil appUtil;
    private static AccessState instance;
    private Disposable disposable;
    private WavesWallet wavesWallet;
    private boolean onDexScreens = false;

    @Deprecated
    public void initAccessState(PrefsUtil prefs, PinStoreService pinStore, AppUtil appUtil) {
        this.prefs = prefs;
        this.pinStore = pinStore;
        this.appUtil = appUtil;
    }

    @Deprecated
    public static AccessState getInstance() {
        if (instance == null)
            instance = new AccessState();
        return instance;
    }

    @Deprecated
    public boolean restoreWavesWallet(String guid, String password) {
        String encryptedWallet = prefs.getValue(guid, PrefsUtil.KEY_ENCRYPTED_WALLET, "");

        try {
            setTemporary(new WavesWallet(encryptedWallet, password));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Deprecated
    public void setOnDexScreens(boolean onDexScreens) {
        this.onDexScreens = onDexScreens;
        if (!onDexScreens) {
            setTemporary(wavesWallet);
        }
    }

    @Deprecated
    public Observable<String> validatePin(String guid, String passedPin) {
        return createValidateObservable(guid, passedPin)
                .flatMap(password ->
                        createPin(guid, password, passedPin)
                                .andThen(Observable.just(password)))
                .compose(RxUtil.applySchedulersToObservable());
    }

    @Deprecated
    public Completable createPin(String guid, String password, String passedPin) {
        return createPinObservable(guid, password, passedPin)
                .compose(RxUtil.applySchedulersToCompletable());
    }

    @Deprecated
    private Observable<String> createValidateObservable(String guid, String passedPin) {
        int fails = prefs.getValue(PrefsUtil.KEY_PIN_FAILS, 0);

        return pinStore.readPassword(guid, passedPin, fails)
                .map(value -> {
                    try {
                        String encryptedPassword = prefs
                                .getValue(guid, PrefsUtil.KEY_ENCRYPTED_PASSWORD, "");
                        String password = AESUtil.decrypt(
                                encryptedPassword,
                                value,
                                AESUtil.PIN_PBKDF2_ITERATIONS);
                        if (!restoreWavesWallet(guid, password)) {
                            throw new RuntimeException("Failed password");
                        }
                        return password;
                    } catch (Exception e) {
                        throw Exceptions.propagate(new Throwable("Decrypt wallet failed"));
                    }
                });
    }

    @Deprecated
    private Completable createPinObservable(String guid, String password, String passedPin) {
        if (passedPin == null || passedPin.equals("0000") || passedPin.length() != 4) {
            return Completable.error(new RuntimeException("Prohibited pin"));
        }

        appUtil.applyPRNGFixes();

        return Completable.create(subscriber -> {
            try {
                byte[] bytes = new byte[16];
                SecureRandom random = new SecureRandom();
                random.nextBytes(bytes);
                String value = new String(Hex.encode(bytes), "UTF-8");

                pinStore.writePassword(guid, passedPin, value).subscribe(res -> {
                    String encryptedPassword = AESUtil.encrypt(
                            password.toString(), value, AESUtil.PIN_PBKDF2_ITERATIONS);
                    prefs.setValue(guid, PrefsUtil.KEY_ENCRYPTED_PASSWORD, encryptedPassword);
                    if (!subscriber.isDisposed()) {
                        subscriber.onComplete();
                    }
                }, err -> {
                    if (!subscriber.isDisposed()) {
                        subscriber.onError(err);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "createPinObservable", e);
                if (!subscriber.isDisposed()) {
                    subscriber.onError(new RuntimeException("Failed to ecrypt password"));
                }
            }
        });
    }

    @Deprecated
    private void setTemporary(WavesWallet newWallet) {
        if (disposable != null) {
            disposable.dispose();
        }

        wavesWallet = newWallet;
        if (!onDexScreens)
            disposable = Observable.just(1).delay(CLEAR_TIMEOUT_SECS, TimeUnit.SECONDS)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribe(res -> removeWavesWallet());
    }

    @Deprecated
    public byte[] getPrivateKey() {
        if (wavesWallet != null) {
            return wavesWallet.getPrivateKey();
        }
        return null;
    }

    @Deprecated
    public String getSeedStr() {
        if (wavesWallet != null) {
            return wavesWallet.getSeedStr();
        }
        return null;
    }

    @Deprecated
    public void removeWavesWallet() {
        wavesWallet = null;
    }

    @Deprecated
    public String storeWavesWallet(String seed, String password, String walletName, boolean skipBackup) {
        try {
            WavesWallet wallet = new WavesWallet(seed.getBytes(Charsets.UTF_8));
            String guid = UUID.randomUUID().toString();
            prefs.setGlobalValue(PrefsUtil.GLOBAL_LOGGED_IN_GUID, guid);
            prefs.addGlobalListValue(EnvironmentManager.get().current().getName() + PrefsUtil.LIST_WALLET_GUIDS, guid);
            prefs.setValue(PrefsUtil.KEY_PUB_KEY, wallet.getPublicKeyStr());
            prefs.setValue(PrefsUtil.KEY_WALLET_NAME, walletName);
            prefs.setValue(PrefsUtil.KEY_ENCRYPTED_WALLET, wallet.getEncryptedData(password));
            if (skipBackup) {
                prefs.setValue(PrefsUtil.KEY_SKIP_BACKUP, true);
            }
            return guid;
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "storeWalletData: ", e);
            return "";
        }
    }
}
