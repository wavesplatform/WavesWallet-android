package com.wavesplatform.wallet.v1.data.access;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.wavesplatform.wallet.v1.crypto.AESUtil;
import com.wavesplatform.wallet.v1.data.auth.WavesWallet;
import com.wavesplatform.wallet.v1.data.rxjava.RxUtil;
import com.wavesplatform.wallet.v1.data.services.PinStoreService;
import com.wavesplatform.wallet.v1.db.DBHelper;
import com.wavesplatform.wallet.v1.ui.auth.EnvironmentManager;
import com.wavesplatform.wallet.v1.util.AddressUtil;
import com.wavesplatform.wallet.v1.util.AppUtil;
import com.wavesplatform.wallet.v1.util.PrefsUtil;
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser;

import org.apache.commons.io.Charsets;
import org.spongycastle.util.encoders.Hex;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.exceptions.Exceptions;
import io.realm.RealmConfiguration;

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

    public void initAccessState(Context context, PrefsUtil prefs, PinStoreService pinStore, AppUtil appUtil) {
        this.prefs = prefs;
        this.pinStore = pinStore;
        this.appUtil = appUtil;
    }

    public static AccessState getInstance() {
        if (instance == null)
            instance = new AccessState();
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

    public void setOnDexScreens(boolean onDexScreens) {
        this.onDexScreens = onDexScreens;
        if (!onDexScreens) {
            setTemporary(wavesWallet);
        }
    }

    public Observable<String> validatePin(String pin) {
        return createValidateObservable(pin).flatMap(pwd ->
                createPin(prefs.getGuid(), pwd, pin).andThen(Observable.just(pwd))
        ).compose(RxUtil.applySchedulersToObservable());
    }

    private Observable<String> createValidateObservable(String passedPin) {
        int fails = prefs.getValue(PrefsUtil.KEY_PIN_FAILS, 0);

        return pinStore.readPassword(fails, prefs.getGuid(), passedPin)
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

                pinStore.savePasswordByKey(walletGuid, value, passedPin).subscribe(res -> {
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

    public void storePassword(String guid, String publicKeyStr, String encryptedPassword) {
        prefs.setGlobalValue(PrefsUtil.GLOBAL_LOGGED_IN_GUID, guid);
        prefs.setValue(PrefsUtil.KEY_PUB_KEY, publicKeyStr);
        prefs.setValue(PrefsUtil.KEY_ENCRYPTED_WALLET, encryptedPassword);
    }

    public String storeWavesWallet(String seed, String password, String walletName, boolean skipBackup) {
        try {
            WavesWallet newWallet = new WavesWallet(seed.getBytes(Charsets.UTF_8));
            String walletGuid = UUID.randomUUID().toString();
            prefs.setGlobalValue(PrefsUtil.GLOBAL_LOGGED_IN_GUID, walletGuid);
            prefs.addGlobalListValue(EnvironmentManager.get().current().getName()
                    + PrefsUtil.LIST_WALLET_GUIDS, walletGuid);
            prefs.setValue(PrefsUtil.KEY_PUB_KEY, newWallet.getPublicKeyStr());
            prefs.setValue(PrefsUtil.KEY_WALLET_NAME, walletName);
            prefs.setValue(PrefsUtil.KEY_ENCRYPTED_WALLET, newWallet.getEncryptedData(password));

            if (skipBackup) {
                prefs.setValue(PrefsUtil.KEY_SKIP_BACKUP, true);
            }

            setTemporary(newWallet);


            RealmConfiguration config = new RealmConfiguration.Builder()
                    .name(String.format("%s.realm", newWallet.getPublicKeyStr()))
                    .deleteRealmIfMigrationNeeded()
                    .build();
            DBHelper.getInstance().setRealmConfig(config);

            return walletGuid;
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "storeWavesWallet: ", e);
            return null;
        }
    }

    public void deleteWavesWallet(String address) {
        String searchWalletGuid = findGuidBy(address);

        prefs.removeValue(searchWalletGuid, PrefsUtil.KEY_PUB_KEY);
        prefs.removeValue(searchWalletGuid, PrefsUtil.KEY_WALLET_NAME);
        prefs.removeValue(searchWalletGuid, PrefsUtil.KEY_ENCRYPTED_WALLET);

        prefs.setGlobalValue(
                EnvironmentManager.get().current().getName() + PrefsUtil.LIST_WALLET_GUIDS,
                createGuidsListWithout(searchWalletGuid));

        if (searchWalletGuid.equals(prefs.getGlobalValue(PrefsUtil.GLOBAL_LOGGED_IN_GUID, ""))) {
            prefs.removeGlobalValue(PrefsUtil.GLOBAL_LOGGED_IN_GUID);
        }
    }

    public WavesWallet getWavesWallet() {
        return wavesWallet;
    }

    public void saveWavesWalletNewName(String address, String name) {
        if (!TextUtils.isEmpty(address) && !TextUtils.isEmpty(name)) {
            String searchWalletGuid = findGuidBy(address);
            prefs.setGlobalValue(searchWalletGuid + PrefsUtil.KEY_WALLET_NAME, name);
        }
    }

    public String getCurrentWavesWalletEncryptedData() {
        String guid = getCurrentGuid();
        if (TextUtils.isEmpty(guid)) {
            return "";
        }
        return prefs.getGlobalValue(guid + PrefsUtil.KEY_ENCRYPTED_WALLET, "");
    }

    public String getCurrentGuid() {
        return prefs.getGuid();
    }

    public AddressBookUser getCurrentWavesWallet() {
        String guid = getCurrentGuid();
        if (TextUtils.isEmpty(guid)) {
            return null;
        }

        String name = prefs.getGlobalValue(guid + PrefsUtil.KEY_WALLET_NAME, "");
        String publicKey = prefs.getGlobalValue(guid + PrefsUtil.KEY_PUB_KEY, "");

        if (TextUtils.isEmpty(publicKey) || TextUtils.isEmpty(name)) {
            return null;
        }

        return new AddressBookUser(AddressUtil.addressFromPublicKey(publicKey), name);
    }

    public boolean deleteCurrentWavesWallet() {
        AddressBookUser currentUser = getCurrentWavesWallet();
        if (currentUser == null) {
            return false;
        } else {
            deleteWavesWallet(currentUser.getAddress());
            return true;
        }
    }

    public String findPublicKeyBy(String address) {
        return prefs.getValue(findGuidBy(address), PrefsUtil.KEY_PUB_KEY, "");
    }

    @NonNull
    private String[] createGuidsListWithout(String guidToRemove) {
        String[] guids = prefs.getGlobalValueList(
                EnvironmentManager.get().current().getName() + PrefsUtil.LIST_WALLET_GUIDS);
        List<String> resultGuidsList = new ArrayList<>();
        for (String guid : guids) {
            if (!guid.equals(guidToRemove)) {
                resultGuidsList.add(guid);
            }
        }
        return resultGuidsList.toArray(new String[0]);
    }

    public String findGuidBy(String address) {
        String[] guids = prefs.getGlobalValueList(
                EnvironmentManager.get().current().getName() + PrefsUtil.LIST_WALLET_GUIDS);
        String resultGuid = "";
        for (String guid : guids) {
            String publicKey = prefs.getValue(guid, PrefsUtil.KEY_PUB_KEY, "");
            if (AddressUtil.addressFromPublicKey(publicKey).equals(address)) {
                resultGuid = guid;
            }
        }
        return resultGuid;
    }

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

    public void removePinFails() {
        prefs.removeValue(PrefsUtil.KEY_PIN_FAILS);
    }

    public void incrementPinFails() {
        int fails = prefs.getValue(PrefsUtil.KEY_PIN_FAILS, 0);
        prefs.setValue(PrefsUtil.KEY_PIN_FAILS, ++fails);
    }

    public int getPinFails() {
        return prefs.getValue(PrefsUtil.KEY_PIN_FAILS, 0);
    }

    public void setCurrentAccountBackupCompleted() {
        prefs.removeValue(PrefsUtil.KEY_SKIP_BACKUP);
    }

    public boolean isCurrentAccountBackupSkipped() {
        return prefs.getValue(PrefsUtil.KEY_SKIP_BACKUP, false);
    }

    public void setUseFingerPrint(boolean use) {
        prefs.setValue(PrefsUtil.KEY_USE_FINGERPRINT, use);
    }

    public boolean isGuidUseFingerPrint(String guid) {
        return prefs.getGuidValue(guid, PrefsUtil.KEY_USE_FINGERPRINT, false);
    }

    public boolean isUseFingerPrint() {
        return prefs.getValue(PrefsUtil.KEY_USE_FINGERPRINT, false);
    }

    public void setEncryptedPin(String data) {
        prefs.setGlobalValue(PrefsUtil.KEY_ENCRYPTED_PIN, data);
    }

    public String getEncryptedPin() {
        return prefs.getGlobalValue(PrefsUtil.KEY_ENCRYPTED_PIN, "");
    }
}
