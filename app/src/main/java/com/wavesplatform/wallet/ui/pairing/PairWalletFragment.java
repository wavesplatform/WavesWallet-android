package com.wavesplatform.wallet.ui.pairing;

import android.Manifest;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v13.app.FragmentCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.databinding.FragmentPairWalletBinding;
import com.wavesplatform.wallet.ui.auth.SeedWalletActivity;
import com.wavesplatform.wallet.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.ui.zxing.CaptureActivity;
import com.wavesplatform.wallet.util.AppUtil;
import com.wavesplatform.wallet.util.PermissionUtil;

public class PairWalletFragment extends Fragment implements FragmentCompat.OnRequestPermissionsResultCallback {
    public static final String LITE_WALLET_URL ="https://waveswallet.io";

    private FragmentPairWalletBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pair_wallet, container, false);

        getActivity().setTitle(getResources().getString(R.string.pair_your_wallet));

        binding.pairingFirstStep.setText(getString(R.string.pair_wallet_step_1, LITE_WALLET_URL + " wallet"));

        binding.commandScan.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                PermissionUtil.requestCameraPermissionFromFragment(binding.mainLayout, getActivity(), this);
            } else {
                startScanActivity();
            }
        });

        binding.commandManual.setOnClickListener(
                v -> startActivity(new Intent(getActivity(), SeedWalletActivity.class)));

        return binding.getRoot();
    }

    private void startScanActivity() {
        if (!new AppUtil(getActivity()).isCameraOpen()) {
            Intent intent = new Intent(getActivity(), CaptureActivity.class);
            intent.putExtra("SCAN_FORMATS", "QR_CODE");
            getActivity().startActivityForResult(intent, ImportOrCreateWalletActivity.PAIRING_QR);
        } else {
            ToastCustom.makeText(getActivity(), getString(R.string.camera_unavailable), ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_ERROR);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionUtil.PERMISSION_REQUEST_CAMERA) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanActivity();
            } else {
                // Permission request was denied.
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
