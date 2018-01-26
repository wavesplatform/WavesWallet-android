package com.wavesplatform.wallet.ui.dex.details.orderbook;

import android.databinding.DataBindingUtil;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.data.Events;
import com.wavesplatform.wallet.databinding.FragmentOrderBookBinding;
import com.wavesplatform.wallet.payload.OrderBook;
import com.wavesplatform.wallet.payload.Price;
import com.wavesplatform.wallet.ui.customviews.MaterialProgressDialog;
import com.wavesplatform.wallet.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.ui.dex.details.DexDetailsActivity;
import com.wavesplatform.wallet.ui.dex.details.order.PlaceOrderActivity;
import com.wavesplatform.wallet.util.annotations.Thunk;

import java.util.ArrayList;
import java.util.Collections;

public class OrderBookFragment extends Fragment implements OrderBookViewModel.DataListener, BaseQuickAdapter.OnItemClickListener {

    @Thunk
    FragmentOrderBookBinding binding;
    @Thunk
    OrderBookViewModel viewModel;
    private PriceAdapter mPriceAdapter;
    private AsksAdapter mAsksAdapter;
    private BidsAdapter mBidsAdapter;
    private MaterialProgressDialog materialProgressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_order_book, container, false);


        viewModel = new OrderBookViewModel(this);

        viewModel.onViewReady();

        viewModel.compositeDisposable.add(viewModel.mRxEventBus
                .filteredObservable(Events.NeedUpdateDataAfterPlaceOrder.class)
                .subscribe(o ->  viewModel.getOrderBook(((DexDetailsActivity) getActivity()).viewModel.dexDetailsModel.getWatchMarket())));

        setupAdapter();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.getOrderBook(((DexDetailsActivity) getActivity()).viewModel.dexDetailsModel.getWatchMarket());
    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.compositeDisposable.clear();
        dismissProgressDialog();
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

    private void setupAdapter() {
        mAsksAdapter = new AsksAdapter();
        mBidsAdapter = new BidsAdapter();
        mPriceAdapter = new PriceAdapter();

        mAsksAdapter.setTickerMarket(((DexDetailsActivity) getActivity()).viewModel.dexDetailsModel.getWatchMarket());
        mBidsAdapter.setTickerMarket(((DexDetailsActivity) getActivity()).viewModel.dexDetailsModel.getWatchMarket());
        mPriceAdapter.setTickerMarket(((DexDetailsActivity) getActivity()).viewModel.dexDetailsModel.getWatchMarket());

        mAsksAdapter.bindToRecyclerView(binding.recycleAsks);
        mBidsAdapter.bindToRecyclerView(binding.recycleBids);
        mPriceAdapter.bindToRecyclerView(binding.recyclePrice);

        binding.recycleAsks.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recycleBids.setLayoutManager(new LinearLayoutManager(getActivity()));
        binding.recyclePrice.setLayoutManager(new LinearLayoutManager(getActivity()));

        binding.recycleAsks.setNestedScrollingEnabled(false);
        binding.recycleBids.setNestedScrollingEnabled(false);
        binding.recyclePrice.setNestedScrollingEnabled(false);

        mAsksAdapter.setFooterView(getView(R.layout.recycle_asks_footer_view));
        mBidsAdapter.setHeaderView(getView(R.layout.recycle_bids_header_view));

        mAsksAdapter.setOnItemClickListener(this);
        mBidsAdapter.setOnItemClickListener((adapter, view, position) -> {
            Price item = mPriceAdapter.getItem(mAsksAdapter.getData().size() + position);
            startActivity(PlaceOrderActivity.newInstance(getActivity(), ((DexDetailsActivity) getActivity()).viewModel.dexDetailsModel.getWatchMarket(), item));
        });
        mPriceAdapter.setOnItemClickListener(this);

        binding.recycleAsks.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                View childAt = binding.recycleAsks.getLayoutManager().getChildAt(mAsksAdapter.getData().size()-1);
                if (childAt == null) return;
                Rect r = new Rect();
                binding.nestedScrollView.getWindowVisibleDisplayFrame(r);
                double heightDiff = binding.nestedScrollView.getRootView().getHeight();
                binding.nestedScrollView.scrollTo(0, Double.valueOf(childAt.getY() - (heightDiff / 2.5)).intValue());
                binding.recycleAsks.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });

    }

    @Override
    public void onShowToast(@StringRes int message, @ToastCustom.ToastType String toastType) {
        ToastCustom.makeText(getActivity(), getString(message), ToastCustom.LENGTH_SHORT, toastType);
    }

    @Override
    public void successfullyGetOrderBook(OrderBook orderBook) {
        Collections.reverse(orderBook.asks);

        mAsksAdapter.setNewData(orderBook.asks);
        mBidsAdapter.setNewData(orderBook.bids);
        mPriceAdapter.setNewData(fillPriceList(orderBook));

        binding.recycleAsks.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                View childAt = binding.recycleAsks.getLayoutManager().getChildAt(0);
                updateView(mAsksAdapter.getFooterLayout(), childAt.getHeight(), orderBook.bids.size());
                updateView(mBidsAdapter.getHeaderLayout(), childAt.getHeight(), orderBook.asks.size());
                binding.recycleAsks.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void updateView(View layout, int height, int offset) {
        RelativeLayout relativeLayout = (RelativeLayout) layout.findViewById(R.id.relative_offset);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height * offset);
        relativeLayout.setLayoutParams(params);
        relativeLayout.requestLayout();
    }

    private View getView(@LayoutRes int layout) {
        return LayoutInflater.from(getActivity()).inflate(layout, null);
    }

    private ArrayList<Price> fillPriceList(OrderBook orderBook) {
        ArrayList<Price> prices = new ArrayList();
        for (OrderBook.Asks ask : orderBook.asks) {
            prices.add(new Price(ask));
        }
        for (OrderBook.Bids bids : orderBook.bids) {
            prices.add(new Price(bids));
        }
        return prices;
    }

    @Override
    public void finishPage() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        viewModel.destroy();
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        Price item = mPriceAdapter.getItem(position);
        startActivity(PlaceOrderActivity.newInstance(getActivity(), ((DexDetailsActivity) getActivity()).viewModel.dexDetailsModel.getWatchMarket(), item));
    }
}
