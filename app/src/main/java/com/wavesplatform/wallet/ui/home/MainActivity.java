package com.wavesplatform.wallet.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.wavesplatform.wallet.BuildConfig;
import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.api.NodeManager;
import com.wavesplatform.wallet.data.access.AccessState;
import com.wavesplatform.wallet.databinding.ActivityMainBinding;
import com.wavesplatform.wallet.ui.assets.AssetsActivity;
import com.wavesplatform.wallet.ui.auth.PinEntryActivity;
import com.wavesplatform.wallet.ui.backup.BackupWalletActivity;
import com.wavesplatform.wallet.ui.balance.TransactionsFragment;
import com.wavesplatform.wallet.ui.base.BaseAuthActivity;
import com.wavesplatform.wallet.ui.customviews.MaterialProgressDialog;
import com.wavesplatform.wallet.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.ui.dex.watchlist_markets.WatchlistMarketsFragment;
import com.wavesplatform.wallet.ui.launcher.LauncherActivity;
import com.wavesplatform.wallet.ui.receive.ReceiveFragment;
import com.wavesplatform.wallet.ui.send.SendFragment;
import com.wavesplatform.wallet.ui.zxing.CaptureActivity;
import com.wavesplatform.wallet.util.AndroidUtils;
import com.wavesplatform.wallet.util.AppUtil;
import com.wavesplatform.wallet.util.DateUtil;
import com.wavesplatform.wallet.util.PermissionUtil;
import com.wavesplatform.wallet.util.PrefsUtil;
import com.wavesplatform.wallet.util.StringBuilderPlus;
import com.wavesplatform.wallet.util.ViewUtils;
import com.wavesplatform.wallet.util.annotations.Thunk;

import java.util.Arrays;

import static com.wavesplatform.wallet.ui.auth.PinEntryFragment.KEY_VALIDATING_PIN_FOR_RESULT;
import static com.wavesplatform.wallet.ui.auth.PinEntryFragment.REQUEST_CODE_VALIDATE_PIN;

public class MainActivity extends BaseAuthActivity implements TransactionsFragment.Communicator,
        MainViewModel.DataListener,
        SendFragment.OnSendFragmentInteractionListener,
        ReceiveFragment.OnReceiveFragmentInteractionListener,
        WatchlistMarketsFragment.OnDexFragmentInteractionListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String SUPPORT_EMAIL = "support@wavesplatform.com";
    private static final int REQUEST_BACKUP = 2225;
    public static final int SCAN_URI = 2007;
    private static final int COOL_DOWN_MILLIS = 2 * 1000;

    @Thunk boolean drawerIsOpen = false;

    private MainViewModel mainViewModel;
    private ActivityMainBinding binding;
    private MaterialProgressDialog fetchTransactionsProgress;
    private AlertDialog mRootedDialog;
    private AppUtil appUtil;
    private long backPressed;
    private boolean returningResult = false;
    private Toolbar toolbar;
    private PrefsUtil prefsUtil;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefsUtil = new PrefsUtil(this);
        appUtil = new AppUtil(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mainViewModel = new MainViewModel(this, this);

        mainViewModel.onViewReady();

        binding.drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                // No-op
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                drawerIsOpen = true;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                drawerIsOpen = false;
            }

            @Override
            public void onDrawerStateChanged(int newState) {
                // No-op
            }
        });

        // Create items
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.send_nav, R.drawable.vector_send, R.color.blockchain_pearl_white);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.transactions_nav, R.drawable.ic_transactions, R.color.blockchain_pearl_white);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.receive_nav, R.drawable.vector_receive, R.color.blockchain_pearl_white);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.dex_nav, R.drawable.vector_dex, R.color.blockchain_pearl_white);

        // Add items
        binding.bottomNavigation.addItems(Arrays.asList(item1, item2, item3, item4));

        // Styling
        binding.bottomNavigation.setAccentColor(ContextCompat.getColor(this, R.color.blockchain_blue));
        binding.bottomNavigation.setInactiveColor(ContextCompat.getColor(this, R.color.blockchain_grey));
        binding.bottomNavigation.setForceTint(true);
        binding.bottomNavigation.setBehaviorTranslationEnabled(false);
        binding.bottomNavigation.setUseElevation(true);

        // Select transactions by default
        binding.bottomNavigation.setCurrentItem(1);
        binding.bottomNavigation.setOnTabSelectedListener((position, wasSelected) -> {
            if (!wasSelected) {
                AccessState.getInstance().setOnDexScreens(false);
                switch (position) {
                    case 0:
                        if (!(getCurrentFragment() instanceof SendFragment)) {
                            // This is a bit of a hack to allow the selection of the correct button
                            // On the bottom nav bar, but without starting the fragment again
                            startSendFragment(null);
                        }
                        setDefaultBottomBarAndToolbarColor();
                        break;
                    case 1:
                        onStartBalanceFragment();
                        setDefaultBottomBarAndToolbarColor();
                        break;
                    case 2:
                        startReceiveFragment();
                        setDefaultBottomBarAndToolbarColor();
                        break;
                    case 3:
//                        requestPinDialog();
                        AccessState.getInstance().setOnDexScreens(true);
                        startDexFragment();
                        break;
                }
            } else {
                if (position == 1 && getCurrentFragment() instanceof TransactionsFragment) {
                    ((TransactionsFragment) getCurrentFragment()).onScrollToTop();
                }
            }

            return true;
        });

        // Configure spam filter
        MenuItem spamFilterItem = binding.nvView.getMenu().findItem(R.id.action_spam_filter);

        boolean spamFilterValue = prefsUtil.getValue(PrefsUtil.KEY_DISABLE_SPAM_FILTER, false);
        ((SwitchCompat) spamFilterItem.getActionView()).setChecked(spamFilterValue);

        ((SwitchCompat) spamFilterItem.getActionView()).setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefsUtil.setValue(PrefsUtil.KEY_DISABLE_SPAM_FILTER, isChecked);
        });


        View header = binding.nvView.getHeaderView(0);
        final TextView myAddress = (TextView) header.findViewById(R.id.nav_address);
        if (myAddress != null) {
            NodeManager nm = NodeManager.get();
            myAddress.setText(nm != null ? nm.getAddress() : "");
        }

        View addressRow = header.findViewById(R.id.address_row);
        addressRow.setOnClickListener(v -> copyToClipboard(NodeManager.get().getAddress()));
    }

    private void requestPinDialog() {
        Intent intent = new Intent(this, PinEntryActivity.class);
        intent.putExtra(KEY_VALIDATING_PIN_FOR_RESULT, true);
        startActivityForResult(intent, REQUEST_CODE_VALIDATE_PIN);
    }

    private void setDefaultBottomBarAndToolbarColor(){
        toolbar.setBackgroundColor(ContextCompat.getColor(this,R.color.blockchain_blue));
        binding.bottomNavigation.setAccentColor(ContextCompat.getColor(this, R.color.blockchain_blue));
        binding.bottomNavigation.setInactiveColor(ContextCompat.getColor(this, R.color.blockchain_grey));
        binding.bottomNavigation.setDefaultBackgroundColor(Color.WHITE);
    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        resetNavigationDrawer();

        binding.bottomNavigation.restoreBottomNavigation(false);
        // Reset state of the bottom nav bar, but not if returning from a scan
        /*if (!returningResult) {
            runOnUiThread(() -> binding.bottomNavigation.setCurrentItem(1));
        }*/
        returningResult = false;

        if (NodeManager.get() == null) {
            appUtil.restartApp();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainViewModel.destroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                binding.drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_qr_main:
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    PermissionUtil.requestCameraPermissionFromActivity(binding.getRoot(), this);
                } else {
                    startScanActivity();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Thunk
    Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.content_frame);
    }

    public boolean getDrawerOpen() {
        return drawerIsOpen;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_VALIDATE_PIN) {
            if (resultCode == RESULT_CANCELED) {
                onBackPressed();
            }
            return;
        }
        if (resultCode == RESULT_OK && requestCode == SCAN_URI
                && data != null && data.getStringExtra(CaptureActivity.SCAN_RESULT) != null) {
            String strResult = data.getStringExtra(CaptureActivity.SCAN_RESULT);

            doScanInput(strResult);

        } else if (resultCode == RESULT_OK && requestCode == REQUEST_BACKUP) {
            resetNavigationDrawer();
        } else {
            if (data != null) {
                returningResult = true;
            }
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerIsOpen) {
            binding.drawerLayout.closeDrawers();
        } else if (getCurrentFragment() instanceof TransactionsFragment) {
            handleBackPressed();
        } else if (getCurrentFragment() instanceof SendFragment) {
            ((SendFragment) getCurrentFragment()).onBackPressed();
        } else if (getCurrentFragment() instanceof ReceiveFragment) {
            ((ReceiveFragment) getCurrentFragment()).onBackPressed();
        } else if (getCurrentFragment() instanceof WatchlistMarketsFragment) {
            ((WatchlistMarketsFragment) getCurrentFragment()).onBackPressed();
        } else {
            // Switch to balance fragment
            TransactionsFragment fragment = new TransactionsFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        }
    }

    public void copyToClipboard(String address) {
        ClipboardManager clipboard = (android.content.ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = android.content.ClipData.newPlainText("Send address", address);
        ToastCustom.makeText(this, getString(R.string.copied_to_clipboard), ToastCustom.LENGTH_LONG, ToastCustom.TYPE_GENERAL);
        clipboard.setPrimaryClip(clip);
    }

    public void handleBackPressed() {
        if (backPressed + COOL_DOWN_MILLIS > System.currentTimeMillis()) {
            if (AndroidUtils.is16orHigher()) {
                finishAffinity();
            } else {
                finish();
                // Shouldn't call System.exit(0) if it can be avoided
                System.exit(0);
            }
            return;
        } else {
            onExitConfirmToast();
        }

        backPressed = System.currentTimeMillis();
    }

    public void onExitConfirmToast() {
        ToastCustom.makeText(getActivity(), getString(R.string.exit_confirm), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_GENERAL);
    }

    private void startScanActivity() {
        if (!appUtil.isCameraOpen()) {
            Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
            startActivityForResult(intent, SCAN_URI);
        } else {
            ToastCustom.makeText(MainActivity.this, getString(R.string.camera_unavailable), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
        }
    }

    private void doScanInput(String strResult) {
        startSendFragment(strResult);
    }

    public void selectDrawerItem(MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.nav_backup:
                startActivityForResult(new Intent(MainActivity.this, BackupWalletActivity.class), REQUEST_BACKUP);
                break;
            case R.id.nav_addresses:
                startActivity(new Intent(MainActivity.this, AssetsActivity.class));
                break;
            case R.id.nav_support:
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{SUPPORT_EMAIL});
                intent.putExtra(Intent.EXTRA_SUBJECT,  String.format("Android app. Support request (%s)", DateUtil.formattedCurrentDate()));
                intent.putExtra(Intent.EXTRA_TEXT, getDeviceInfo());
                startActivity(Intent.createChooser(intent, SUPPORT_EMAIL));
                break;
            case R.id.nav_logout:
                new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                        .setTitle(R.string.logout_wallet)
                        .setMessage(R.string.ask_you_sure_logout)
                        .setPositiveButton(R.string.logout, (dialog, which) -> mainViewModel.logout())
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
                break;
        }
        binding.drawerLayout.closeDrawers();
    }

    private String getDeviceInfo(){
        StringBuilderPlus sb = new StringBuilderPlus();
        sb.appendLine("My Device Info: \n");
        sb.appendLine("Manufacturer: ", Build.MANUFACTURER);
        sb.appendLine("Build: ",  Build.DISPLAY);
        sb.appendLine("Model: ", Build.PRODUCT);
        sb.appendLine("Android: ", Build.VERSION.RELEASE);
        sb.appendLine("App version: ", BuildConfig.VERSION_NAME);
        sb.appendLine("");
        sb.appendLine("Waves address: \n", NodeManager.get().getAddress());
        sb.appendLine("");
        return  sb.toString();
    }

    @Override
    public void resetNavigationDrawer() {
        toolbar = (Toolbar) findViewById(R.id.toolbar_general);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(ContextCompat.getDrawable(this, R.drawable.ic_menu_white_24dp));
//        toolbar.setTitle(""); // TODO: 28.07.17 Need? Bug with empty title


        ViewUtils.setElevation(toolbar, 0F);

        MenuItem backUpMenuItem = binding.nvView.getMenu().findItem(R.id.nav_backup);
        backUpMenuItem.setVisible(true);

        binding.nvView.setNavigationItemSelectedListener(
                menuItem -> {
                    selectDrawerItem(menuItem);
                    return true;
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mRootedDialog != null && mRootedDialog.isShowing()) {
            mRootedDialog.dismiss();
        }
    }

    private void startSingleActivity(Class clazz) {
        Intent intent = new Intent(MainActivity.this, clazz);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        returningResult = true;

        if (requestCode == PermissionUtil.PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanActivity();
            } else {
                // Permission request was denied.
            }
        }
    }

    @Override
    public void onRooted() {
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            if (!isFinishing()) {
                mRootedDialog = new AlertDialog.Builder(getActivity(), R.style.AlertDialogStyle)
                        .setMessage(getString(R.string.device_rooted))
                        .setCancelable(false)
                        .setPositiveButton(R.string.dialog_continue, null)
                        .create();

                mRootedDialog.show();
            }
        }, 500);
    }

    private Context getActivity() {
        return this;
    }


    @Override
    public void onConnectivityFail() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AlertDialogStyle);
        final String message = getString(R.string.check_connectivity_exit);
        builder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.dialog_continue, (d, id) -> d.dismiss());

        builder.create().show();
    }

    @Override
    public void kickToLauncherPage() {
        startSingleActivity(LauncherActivity.class);
    }

    @Override
    public void onFetchTransactionsStart() {
        fetchTransactionsProgress = new MaterialProgressDialog(this);
        fetchTransactionsProgress.setCancelable(false);
        fetchTransactionsProgress.setMessage(getString(R.string.please_wait));
        fetchTransactionsProgress.show();
    }

    @Override
    public void onFetchTransactionCompleted() {
        if (fetchTransactionsProgress != null && fetchTransactionsProgress.isShowing()) {
            fetchTransactionsProgress.dismiss();
        }
    }

    @Override
    public void onScanInput(String strUri) {
        doScanInput(strUri);
    }

    @Override
    public void onStartBalanceFragment() {
        getSupportActionBar().setTitle("Transactions");
        TransactionsFragment fragment = new TransactionsFragment();
        startFragmentWithAnimation(fragment);
    }

    public void startSendFragment(String uri) {
        int selectedAccountPosition;
        try {
            selectedAccountPosition = ((TransactionsFragment) getCurrentFragment()).getSelectedAccountPosition();
        } catch (Exception e) {
            Log.e(TAG, "startSendFragment: ", e);
            selectedAccountPosition = -1;
        }

        SendFragment sendFragment = SendFragment.newInstance(uri, selectedAccountPosition);
        startFragmentWithAnimation(sendFragment);
    }

    public void startReceiveFragment() {
        int selectedAccountPosition;
        try {
            selectedAccountPosition = ((TransactionsFragment) getCurrentFragment()).getSelectedAccountPosition();
        } catch (Exception e) {
            Log.e(TAG, "startReceiveFragment: ", e);
            selectedAccountPosition = -1;
        }

        ReceiveFragment receiveFragment = ReceiveFragment.newInstance(selectedAccountPosition);
        startFragmentWithAnimation(receiveFragment);
    }


    public void startDexFragment() {
        int selectedAccountPosition;
        try {
            selectedAccountPosition = ((TransactionsFragment) getCurrentFragment()).getSelectedAccountPosition();
        } catch (Exception e) {
            Log.e(TAG, "startDexFragment: ", e);
            selectedAccountPosition = -1;
        }

        WatchlistMarketsFragment watchlistMarketsFragment = WatchlistMarketsFragment.newInstance(selectedAccountPosition);
        startFragmentWithAnimation(watchlistMarketsFragment);
    }

    private void startFragmentWithAnimation(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        transaction.replace(R.id.content_frame, fragment).commitAllowingStateLoss();
    }

    public AHBottomNavigation getBottomNavigationView() {
        return binding.bottomNavigation;
    }

    @Override
    public void clearAllDynamicShortcuts() {
    }

    @Override
    public void onSendFragmentClose() {
        binding.bottomNavigation.setCurrentItem(1);
    }

    // Ensure bottom nav button selected after scanning for result
    @Override
    public void onSendFragmentStart() {
        binding.bottomNavigation.setCurrentItem(0);
    }

    @Override
    public void onReceiveFragmentClose() {
        binding.bottomNavigation.setCurrentItem(1);
    }

    @Override
    public void onDexFragmentClose() {
        binding.bottomNavigation.setCurrentItem(3);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }



   /*@Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            String json = savedInstanceState.getString("pendingTransactions");
            Type listType = new TypeToken<ArrayList<Transaction>>() {}.getType();
            List<Transaction> pending = NodeManager.get().getGson().fromJson(json, listType);
            if (pending != null) {
                NodeManager.get().pendingTransactions = pending;
            }
        } catch (Exception ex) {
            Log.e(TAG, "onRestoreInstanceState", ex);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("pendingTransactions", NodeManager.get().getGson().toJson(NodeManager.get().pendingTransactions));
        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);
    }*/
}
