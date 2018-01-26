package com.wavesplatform.wallet.ui.receive;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.util.Pair;
import android.webkit.MimeTypeMap;

import com.google.common.collect.HashBiMap;
import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.api.NodeManager;
import com.wavesplatform.wallet.data.datamanagers.ReceiveDataManager;
import com.wavesplatform.wallet.injection.Injector;
import com.wavesplatform.wallet.payload.AssetBalance;
import com.wavesplatform.wallet.ui.assets.AssetsHelper;
import com.wavesplatform.wallet.ui.assets.ItemAccount;
import com.wavesplatform.wallet.ui.base.BaseViewModel;
import com.wavesplatform.wallet.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.util.AndroidUtils;
import com.wavesplatform.wallet.util.AppUtil;
import com.wavesplatform.wallet.util.PrefsUtil;
import com.wavesplatform.wallet.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

@SuppressWarnings("WeakerAccess")
public class ReceiveViewModel extends BaseViewModel {

    public static final String TAG = ReceiveViewModel.class.getSimpleName();
    static final String KEY_WARN_WATCH_ONLY_SPEND = "warn_watch_only_spend";
    private static final int DIMENSION_QR_CODE = 600;

    private DataListener mDataListener;
    @Inject AppUtil mAppUtil;
    @Inject PrefsUtil mPrefsUtil;
    @Inject StringUtils mStringUtils;
    @Inject ReceiveDataManager mDataManager;
    @Inject AssetsHelper mAssetsHelper;
    @Inject Context mApplicationContext;
    @VisibleForTesting HashBiMap<Integer, Object> mAccountMap;

    public interface DataListener {

        Bitmap getQrBitmap();

        void onSpinnerDataChanged();

        void showQrLoading();

        void showQrCode(@Nullable Bitmap bitmap);

        void showToast(String message, @ToastCustom.ToastType String toastType);

        void updateBtcTextField(String text);

    }

    public ReceiveViewModel(DataListener listener, Locale locale) {
        Injector.getInstance().getDataManagerComponent().inject(this);
        mDataListener = listener;

        mAccountMap = HashBiMap.create();
    }

    @Override
    public void onViewReady() {
    }

    @NonNull
    public List<ItemAccount> getAssetsList() {
        return mAssetsHelper.getAccountItems();
    }

    @NonNull
    public PrefsUtil getPrefsUtil() {
        return mPrefsUtil;
    }

    public void generateQrCode(String uri) {
        mDataListener.showQrLoading();
        compositeDisposable.clear();
        compositeDisposable.add(
                mDataManager.generateQrCode(uri, DIMENSION_QR_CODE)
                        .subscribe(
                                qrCode -> mDataListener.showQrCode(qrCode),
                                throwable -> mDataListener.showQrCode(null)));
    }

    public int getDefaultSpinnerPosition() {
       return 0;
    }

    @Nullable
    public AssetBalance getCurrentAsset(int position) {
        List<ItemAccount> assets = getAssetsList();
        return position >= 0 && assets.size() > position &&  assets.get(position) != null  ?
                assets.get(position).accountObject : NodeManager.get().wavesAsset;
    }

    public boolean warnWatchOnlySpend() {
        return mPrefsUtil.getValue(KEY_WARN_WATCH_ONLY_SPEND, true);
    }

    public void setWarnWatchOnlySpend(boolean warn) {
        mPrefsUtil.setValue(KEY_WARN_WATCH_ONLY_SPEND, warn);
    }

    @Nullable
    private File getQrFile() {
        String strFileName = mAppUtil.getReceiveQRFilename();
        File file = new File(strFileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                Log.e(TAG, "getQrFile: ", e);
            }
        }
        file.setReadable(true, false);
        return file;
    }

    @Nullable
    private FileOutputStream getFileOutputStream(File file) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "getFileOutputStream: ", e);
        }
        return fos;
    }

    @Nullable
    public List<SendPaymentCodeData> getIntentDataList(String uri) {
        File file = getQrFile();
        FileOutputStream outputStream;
        outputStream = getFileOutputStream(file);

        if (outputStream != null) {
            Bitmap bitmap = mDataListener.getQrBitmap();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);

            try {
                outputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "getIntentDataList: ", e);
                mDataListener.showToast(e.getMessage(), ToastCustom.TYPE_ERROR);
                return null;
            }

            List<SendPaymentCodeData> dataList = new ArrayList<>();

            PackageManager packageManager = mAppUtil.getPackageManager();

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setType("application/image");
            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));

            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1);
            String type = mime.getMimeTypeFromExtension(ext);

            Intent imageIntent = new Intent();
            imageIntent.setAction(Intent.ACTION_SEND);
            imageIntent.setType(type);

            if (AndroidUtils.is23orHigher()) {
                Uri uriForFile = FileProvider.getUriForFile(mApplicationContext, getAppUtil().getPackageName() + ".fileProvider", file);
                imageIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                imageIntent.putExtra(Intent.EXTRA_STREAM, uriForFile);
            } else {
                imageIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
                imageIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            HashMap<String, Pair<ResolveInfo, Intent>> intentHashMap = new HashMap<>();

            List<ResolveInfo> emailInfos = packageManager.queryIntentActivities(emailIntent, 0);
            addResolveInfoToMap(emailIntent, intentHashMap, emailInfos);

            List<ResolveInfo> imageInfos = packageManager.queryIntentActivities(imageIntent, 0);
            addResolveInfoToMap(imageIntent, intentHashMap, imageInfos);

            SendPaymentCodeData d;

            Iterator it = intentHashMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry mapItem = (Map.Entry) it.next();
                Pair<ResolveInfo, Intent> pair = (Pair<ResolveInfo, Intent>) mapItem.getValue();
                ResolveInfo resolveInfo = pair.first;
                String context = resolveInfo.activityInfo.packageName;
                String packageClassName = resolveInfo.activityInfo.name;
                CharSequence label = resolveInfo.loadLabel(packageManager);
                Drawable icon = resolveInfo.loadIcon(packageManager);

                Intent intent = pair.second;
                intent.setClassName(context, packageClassName);

                d = new SendPaymentCodeData(label.toString(), icon, intent);
                dataList.add(d);

                it.remove();
            }

            return dataList;

        } else {
            mDataListener.showToast(mStringUtils.getString(R.string.unexpected_error), ToastCustom.TYPE_ERROR);
            return null;
        }

    }

    /**
     * Prevents apps being added to the list twice, as it's confusing for users. Full email Intent
     * takes priority.
     */
    private void addResolveInfoToMap(Intent intent, HashMap<String, Pair<ResolveInfo, Intent>> intentHashMap, List<ResolveInfo> resolveInfo) {
        for (ResolveInfo info : resolveInfo) {
            if (!intentHashMap.containsKey(info.activityInfo.name)) {
                intentHashMap.put(info.activityInfo.name, new Pair<>(info, new Intent(intent)));
            }
        }
    }

    @NonNull
    public AppUtil getAppUtil() {
        return mAppUtil;
    }

    public static class SendPaymentCodeData {
        private Drawable mLogo;
        private String mTitle;
        private Intent mIntent;

        SendPaymentCodeData(String title, Drawable logo, Intent intent) {
            mTitle = title;
            mLogo = logo;
            mIntent = intent;
        }

        public Intent getIntent() {
            return mIntent;
        }

        public String getTitle() {
            return mTitle;
        }

        public Drawable getLogo() {
            return mLogo;
        }
    }
}
