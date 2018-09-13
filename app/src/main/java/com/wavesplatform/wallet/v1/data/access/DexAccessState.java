package com.wavesplatform.wallet.v1.data.access;

import android.content.Context;
import android.util.Log;

import com.wavesplatform.wallet.BlockchainApplication;
import com.wavesplatform.wallet.v1.crypto.AESUtil;
import com.wavesplatform.wallet.v1.data.auth.WavesWallet;
import com.wavesplatform.wallet.v1.data.rxjava.RxUtil;
import com.wavesplatform.wallet.v1.data.services.PinStoreService;
import com.wavesplatform.wallet.v1.db.DBHelper;
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager;
import com.wavesplatform.wallet.v1.util.AppUtil;
import com.wavesplatform.wallet.v1.util.PrefsUtil;

import org.apache.commons.io.Charsets;
import org.spongycastle.util.encoders.Hex;

import java.security.SecureRandom;
import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.realm.RealmConfiguration;

public class DexAccessState {

    private static final String TAG = DexAccessState.class.getSimpleName();

    private PrefsUtil prefs;
    private PinStoreService pinStore;
    private AppUtil appUtil;
    private static DexAccessState instance;
    private Disposable disposable;

    private WavesWallet wavesWallet;

    public void initAccessState(Context context, PrefsUtil prefs, PinStoreService pinStore, AppUtil appUtil) {
        this.prefs = prefs;
        this.pinStore = pinStore;
        this.appUtil = appUtil;
    }

    public static DexAccessState getInstance() {
        if (instance == null)
            instance = new DexAccessState();
        return instance;
    }

    public Completable createPin(String walletGuid, String password, String passedPin) {
        return createPinObservable(walletGuid, password, passedPin)
                .compose(RxUtil.applySchedulersToCompletable());
    }

    public boolean restoreWavesWallet(String password) {
        String encryptedWallet = prefs.getValue(PrefsUtil.KEY_ENCRYPTED_WALLET, "");

        try {
            setTemporary(new WavesWallet(encryptedWallet, password));
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public Observable<String> validatePin(String pin) {
        return createValidateObservable(pin).flatMap(pwd ->
                createPin(BlockchainApplication.getAccessManager().getLastLoggedInGuid(), pwd, pin)
                        .andThen(Observable.just(pwd))
        ).compose(RxUtil.applySchedulersToObservable());
    }

    private Observable<String> createValidateObservable(String passedPin) {
        int fails = prefs.getValue(PrefsUtil.KEY_PIN_FAILS, 0);

        return pinStore.readPassword(BlockchainApplication.getAccessManager()
                .getLastLoggedInGuid(), passedPin, fails)
                .map(value -> {
                    try {
                        String encryptedPassword = prefs.getValue(PrefsUtil.KEY_ENCRYPTED_PASSWORD, "");
                        String password = AESUtil.decrypt(encryptedPassword,
                                value,
                                AESUtil.PIN_PBKDF2_ITERATIONS);
                        if (!restoreWavesWallet(password)) {
                            throw new RuntimeException("Failed password");
                        }
                        return password;
                    } catch (Exception e) {
                        throw Exceptions.propagate(new Throwable("Decrypt wallet failed"));
                    }
                });
    }

    private Completable createPinObservable(String walletGuid, String password, String passedPin) {
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

                pinStore.writePassword(walletGuid, passedPin, value).subscribe(res -> {
                    String encryptedPassword = AESUtil.encrypt(
                            password.toString(), value, AESUtil.PIN_PBKDF2_ITERATIONS);
                    prefs.setValue(PrefsUtil.KEY_ENCRYPTED_PASSWORD, encryptedPassword);
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

    public String storeWavesWallet(String seed, String password, String walletName) {
        try {
            WavesWallet newWallet = new WavesWallet(seed.getBytes(Charsets.UTF_8));
            String walletGuid = UUID.randomUUID().toString();
            prefs.setGlobalValue(PrefsUtil.GLOBAL_LOGGED_IN_GUID, walletGuid);
            prefs.addGlobalListValue(EnvironmentManager.get().current().getName() + PrefsUtil.LIST_WALLET_GUIDS, walletGuid);
            prefs.setValue(PrefsUtil.KEY_PUB_KEY, newWallet.getPublicKeyStr());
            prefs.setValue(PrefsUtil.KEY_WALLET_NAME, walletName);
            prefs.setValue(PrefsUtil.KEY_ENCRYPTED_WALLET, newWallet.getEncryptedData(password));

            setTemporary(newWallet);


            RealmConfiguration config = new RealmConfiguration.Builder()
                    .name(String.format("%s.realm", newWallet.getPublicKeyStr()))
                    .deleteRealmIfMigrationNeeded()
                    .build();
            DBHelper.getInstance().setRealmConfig(config);

            return walletGuid;
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "storeWalletData: ", e);
            return null;
        }
    }

    private void setTemporary(WavesWallet newWallet) {
        if (disposable != null) {
            disposable.dispose();
        }

        wavesWallet = newWallet;
        disposable = Observable.just(1).subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(res -> removeWavesWallet());
    }

    public byte[] getPrivateKey() {
        if (wavesWallet != null) {
            return wavesWallet.getPrivateKey();
        }
        return null;
    }

    public String getSeedStr() {
        if (wavesWallet != null) {
            return wavesWallet.getSeedStr();
        }
        return null;
    }

    public void removeWavesWallet() {
        wavesWallet = null;
    }
}
