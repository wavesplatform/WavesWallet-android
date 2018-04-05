package com.wavesplatform.wallet.v1.ui.dex.details.my_orders;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.v1.data.Events;
import com.wavesplatform.wallet.v1.data.enums.OrderStatus;
import com.wavesplatform.wallet.databinding.FragmentMyOrdersBinding;
import com.wavesplatform.wallet.v1.payload.MyOrder;
import com.wavesplatform.wallet.v1.payload.WatchMarket;
import com.wavesplatform.wallet.v1.ui.auth.PinEntryActivity;
import com.wavesplatform.wallet.v1.ui.customviews.MaterialProgressDialog;
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.v1.ui.dex.details.DexDetailsActivity;
import com.wavesplatform.wallet.v1.util.annotations.Thunk;

import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.wavesplatform.wallet.v1.data.Keys.KEY_PAIR_MODEL;
import static com.wavesplatform.wallet.v1.ui.auth.PinEntryFragment.KEY_VALIDATED_PASSWORD;
import static com.wavesplatform.wallet.v1.ui.auth.PinEntryFragment.KEY_VALIDATING_PIN_FOR_RESULT;
import static com.wavesplatform.wallet.v1.ui.auth.PinEntryFragment.REQUEST_CODE_VALIDATE_PIN;

public class MyOrdersFragment extends Fragment implements MyOrdersViewModel.DataListener {

    @Thunk
    FragmentMyOrdersBinding binding;
    @Thunk
    MyOrdersViewModel viewModel;
    private MaterialProgressDialog materialProgressDialog;
    private MyOrdersAdapter mMyOrdersAdapter;

    public static MyOrdersFragment newInstance(WatchMarket pairModel) {
        Bundle bundle = new Bundle();
        MyOrdersFragment myOrdersFragment = new MyOrdersFragment();
        bundle.putParcelable(KEY_PAIR_MODEL, pairModel);
        myOrdersFragment.setArguments(bundle);
        return myOrdersFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_orders, container, false);
        viewModel = new MyOrdersViewModel(getActivity(), this);

        viewModel.compositeDisposable.add(viewModel.mRxEventBus
                .filteredObservable(Events.NeedUpdateDataAfterPlaceOrder.class)
                .subscribe(o -> sendRequest()));

        mMyOrdersAdapter = new MyOrdersAdapter();
        mMyOrdersAdapter.setWatchMarket(((DexDetailsActivity) getActivity()).viewModel.dexDetailsModel.getWatchMarket());
        mMyOrdersAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            switch (view.getId()) {
                case R.id.image_delete: {
                    MyOrder item = (MyOrder) adapter.getItem(position);
                    if (viewModel.signTransaction(item.id)) {
                        if (item.getStatus() == OrderStatus.Accepted || item.getStatus() == OrderStatus.PartiallyFilled) viewModel.cancelOrder();
                        else viewModel.deleteOrder(position);
                    } else {
                        requestPinDialog();
                    }
                }
            }
        });

        binding.recycleMyOrders.setLayoutManager(new LinearLayoutManager(getActivity()));

        mMyOrdersAdapter.bindToRecyclerView(binding.recycleMyOrders);
        mMyOrdersAdapter.setEmptyView(R.layout.dex_empty_view);

        binding.swipeContainer.setOnRefreshListener(this::sendRequest);

        initArgs(getArguments());

        return binding.getRoot();
    }

    public void sendRequest() {
        if (viewModel.signTransaction()) {
            viewModel.getMyOrders();
        } else {
            requestPinDialog();
        }
    }

    private void initArgs(Bundle bundle) {
        if (bundle == null) return;
        if (bundle.containsKey(KEY_PAIR_MODEL))
            viewModel.myOrderModel.setPairModel(bundle.getParcelable(KEY_PAIR_MODEL));
    }

    private void requestPinDialog() {
        Intent intent = new Intent(getActivity(), PinEntryActivity.class);
        intent.putExtra(KEY_VALIDATING_PIN_FOR_RESULT, true);
        startActivityForResult(intent, REQUEST_CODE_VALIDATE_PIN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_VALIDATE_PIN && resultCode == RESULT_OK && data != null
                && data.getStringExtra(KEY_VALIDATED_PASSWORD) != null) {
            if (viewModel.signTransaction()) {
                viewModel.getMyOrders();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.destroy();
    }

    @Override
    public void showMyOrders(List<MyOrder> myOrders) {
        binding.swipeContainer.setRefreshing(false);
        mMyOrdersAdapter.setNewData(myOrders);
    }

    @Override
    public void afterSuccessfullyDelete(int position) {
        mMyOrdersAdapter.remove(position);
    }

    @Override
    public void onShowToast(@StringRes int message, @ToastCustom.ToastType String toastType) {
        binding.swipeContainer.setRefreshing(false);
        ToastCustom.makeText(getActivity(), getString(message), ToastCustom.LENGTH_SHORT, toastType);
    }

    @Override
    public void showProgressDialog(@StringRes int messageId, @Nullable String suffix) {
        dismissProgressDialog();
        materialProgressDialog = new MaterialProgressDialog(getActivity());
        materialProgressDialog.setCancelable(false);
        if (suffix != null) {
            materialProgressDialog.setMessage(getString(messageId) + suffix);
        } else {
            materialProgressDialog.setMessage(getString(messageId));
        }

        if (!getActivity().isFinishing()) materialProgressDialog.show();
    }

    @Override
    public void dismissProgressDialog() {
        if (materialProgressDialog != null && materialProgressDialog.isShowing()) {
            materialProgressDialog.dismiss();
            materialProgressDialog = null;
        }
    }
}
