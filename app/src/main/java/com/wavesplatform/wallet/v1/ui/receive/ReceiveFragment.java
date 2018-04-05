package com.wavesplatform.wallet.v1.ui.receive;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.databinding.FragmentReceiveBinding;
import com.wavesplatform.wallet.v1.payload.AssetBalance;
import com.wavesplatform.wallet.v1.ui.balance.TransactionsFragment;
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.v1.ui.send.AssetAccountAdapter;
import com.wavesplatform.wallet.v1.util.AddressUtil;
import com.wavesplatform.wallet.v1.util.AndroidUtils;
import com.wavesplatform.wallet.v1.util.MoneyUtil;
import com.wavesplatform.wallet.v1.util.PermissionUtil;
import com.wavesplatform.wallet.v1.util.ViewUtils;
import com.wavesplatform.wallet.v1.util.annotations.Thunk;

import java.util.List;
import java.util.Locale;

public class ReceiveFragment extends Fragment implements ReceiveViewModel.DataListener {

    private static final String TAG = ReceiveFragment.class.getSimpleName();
    private static final String ARG_IS_BTC = "is_btc";
    private static final String ARG_SELECTED_ACCOUNT_POSITION = "selected_account_position";
    private static final int COOL_DOWN_MILLIS = 2 * 1000;

    @Thunk ReceiveViewModel viewModel;
    @Thunk FragmentReceiveBinding binding;
    private BottomSheetDialog bottomSheetDialog;
    private AssetAccountAdapter assetAccountAdapter;
    private OnReceiveFragmentInteractionListener listener;

    @Thunk boolean textChangeAllowed = true;
    private String uri;
    private long backPressed;

    private IntentFilter intentFilter = new IntentFilter(TransactionsFragment.ACTION_INTENT);
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction().equals(TransactionsFragment.ACTION_INTENT)) {
                if (viewModel != null) {
                    // Update UI with new Address + QR
                    viewModel.getAssetsList();
                    displayQRCode(binding.content.accounts.spinner.getSelectedItemPosition());
                }
            }
        }
    };

    public ReceiveFragment() {
        // Required empty public constructor
    }

    public static ReceiveFragment newInstance(int selectedAccountPosition) {
        ReceiveFragment fragment = new ReceiveFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SELECTED_ACCOUNT_POSITION, selectedAccountPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_receive, container, false);
        viewModel = new ReceiveViewModel(this, Locale.getDefault());

        setupToolbar();

        viewModel.onViewReady();

        setupLayout();

        selectAccount(viewModel.getDefaultSpinnerPosition());

        setHasOptionsMenu(true);

        return binding.getRoot();
    }

    private void setupToolbar() {
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.receive_nav);

            ViewUtils.setElevation(
                    getActivity().findViewById(R.id.appbar_layout),
                    ViewUtils.convertDpToPixel(5F, getContext()));
        } else {
            finishPage();
        }
    }

    private void setupLayout() {
        if (viewModel.getAssetsList().size() == 1) {
            binding.content.fromRow.setVisibility(View.GONE);
        }

        //binding.content.amount.setKeyListener(
        //        DigitsKeyListener.getInstance("0123456789" + MoneyUtil.getDefaultDecimalSeparator()));

        binding.content.amount.addTextChangedListener(mAmountTextWatcher);
        binding.content.amount.setSelectAllOnFocus(true);
        binding.content.amount.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.content.amount.postDelayed(() ->
                        binding.content.amount.setHint("0.00"), 200);
            } else {
                binding.content.amount.setHint("");
            }
        });

        // Spinner
        assetAccountAdapter = new AssetAccountAdapter(
                getActivity(),
                R.layout.spinner_item,
                viewModel.getAssetsList());

        assetAccountAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        binding.content.accounts.spinner.setAdapter(assetAccountAdapter);
        binding.content.accounts.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                binding.content.accounts.spinner.setSelection(binding.content.accounts.spinner.getSelectedItemPosition());
                displayQRCode(binding.content.accounts.spinner.getSelectedItemPosition());
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // No-op
            }
        });

        binding.content.accounts.spinner.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    binding.content.accounts.spinner.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    //noinspection deprecation
                    binding.content.accounts.spinner.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    binding.content.accounts.spinner.setDropDownWidth(binding.content.accounts.spinner.getWidth());
                }
            }
        });

        // QR Code
        binding.content.qr.setOnClickListener(v -> showClipboardWarning());
        binding.content.qr.setOnLongClickListener(view -> {
            onShareClicked();
            return true;
        });
    }

    private TextWatcher mAmountTextWatcher = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
            String input = s.toString();
            AssetBalance ab = viewModel.getCurrentAsset(binding.content.accounts.spinner.getSelectedItemPosition());

            binding.content.amount.removeTextChangedListener(this);
            String converted = MoneyUtil.convertToCorrectFormat(input, ab);
            if (!input.equals(converted)) {
                s.replace(0, s.length(), converted);
            }
            if (converted.isEmpty()) {
                showToast(getString(R.string.invalid_amount), ToastCustom.TYPE_ERROR);
            }
            binding.content.amount.addTextChangedListener(this);

            if (textChangeAllowed) {
                textChangeAllowed = false;

                displayQRCode(binding.content.accounts.spinner.getSelectedItemPosition());
                textChangeAllowed = true;
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // No-op
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // No-op
        }
    };

    private void selectAccount(int position) {
        if (binding.content.accounts.spinner != null) {
            displayQRCode(position);
        }
    }

    @Thunk
    void displayQRCode(int spinnerIndex) {
        AssetBalance ab = viewModel.getCurrentAsset(spinnerIndex);

        long amountLong = MoneyUtil.getUnscaledValue(binding.content.amount.getText().toString(), ab);

        uri = AddressUtil.generateReceiveUri(amountLong, ab, null);
        binding.content.receivingAddress.setText(uri);

        viewModel.generateQrCode(uri);
    }

    @Override
    public void updateBtcTextField(String text) {
        binding.content.amount.setText(text);
    }

    @Override
    public void onResume() {
        super.onResume();
        assetAccountAdapter.updateData(viewModel.getAssetsList());

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public void showQrLoading() {
        binding.content.qr.setVisibility(View.GONE);
        binding.content.receivingAddress.setVisibility(View.GONE);
        binding.content.progressBar2.setVisibility(View.VISIBLE);
    }

    @Override
    public void showQrCode(@Nullable Bitmap bitmap) {
        binding.content.progressBar2.setVisibility(View.GONE);
        binding.content.qr.setVisibility(View.VISIBLE);
        binding.content.receivingAddress.setVisibility(View.VISIBLE);
        binding.content.qr.setImageBitmap(bitmap);
    }

    private void setupBottomSheet(String uri) {
        List<ReceiveViewModel.SendPaymentCodeData> list = viewModel.getIntentDataList(uri);
        if (list != null) {
            ShareReceiveIntentAdapter adapter = new ShareReceiveIntentAdapter(list);
            adapter.setItemClickedListener(() -> bottomSheetDialog.dismiss());

            View sheetView = getActivity().getLayoutInflater().inflate(R.layout.bottom_sheet_receive, null);
            RecyclerView recyclerView = (RecyclerView) sheetView.findViewById(R.id.recycler_view);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            bottomSheetDialog = new BottomSheetDialog(getContext(), R.style.BottomSheetDialog);
            bottomSheetDialog.setContentView(sheetView);

            adapter.notifyDataSetChanged();
        }
    }

    private void onShareClicked() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, uri);
        startActivity(Intent.createChooser(shareIntent, "Share link using"));
        /*if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            PermissionUtil.requestWriteStoragePermissionFromFragment(binding.getRoot(), this);
        } else {

            showBottomSheet();
        }*/
    }

    private void showBottomSheet() {
        setupBottomSheet(uri);
        bottomSheetDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionUtil.PERMISSION_REQUEST_WRITE_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showBottomSheet();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void showClipboardWarning() {
        ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = android.content.ClipData.newPlainText("Send address", uri);
        ToastCustom.makeText(getActivity(), getString(R.string.copied_to_clipboard), ToastCustom.LENGTH_LONG, ToastCustom.TYPE_GENERAL);
        clipboard.setPrimaryClip(clip);
    }

    @Override
    public Bitmap getQrBitmap() {
        return ((BitmapDrawable) binding.content.qr.getDrawable()).getBitmap();
    }

    @Override
    public void showToast(String message, @ToastCustom.ToastType String toastType) {
        ToastCustom.makeText(getActivity(), message, ToastCustom.LENGTH_SHORT, toastType);
    }

    @Override
    public void onSpinnerDataChanged() {
        if (assetAccountAdapter != null) assetAccountAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_receive, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem menuItem = menu.findItem(R.id.action_qr_main);
        menuItem.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                onShareClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void onBackPressed() {
        hideKeyboard();
        handleBackPressed();
    }

    public void handleBackPressed() {
        if (backPressed + COOL_DOWN_MILLIS > System.currentTimeMillis()) {
            if (AndroidUtils.is16orHigher()) {
                getActivity().finishAffinity();
            } else {
                getActivity().finish();
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

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.destroy();
    }


    public void finishPage() {
        if (listener != null) {
            listener.onReceiveFragmentClose();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnReceiveFragmentInteractionListener) {
            listener = (OnReceiveFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnReceiveFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnReceiveFragmentInteractionListener {

        void onReceiveFragmentClose();

    }
}
