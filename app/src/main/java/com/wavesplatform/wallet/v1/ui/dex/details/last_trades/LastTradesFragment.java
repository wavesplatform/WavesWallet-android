package com.wavesplatform.wallet.v1.ui.dex.details.last_trades;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.v1.data.Events;
import com.wavesplatform.wallet.databinding.FragmentLastTradesBinding;
import com.wavesplatform.wallet.v1.payload.TradesMarket;
import com.wavesplatform.wallet.v1.payload.WatchMarket;
import com.wavesplatform.wallet.v1.ui.dex.details.DexDetailsActivity;
import com.wavesplatform.wallet.v1.util.annotations.Thunk;

import java.util.List;

import static com.wavesplatform.wallet.v1.data.Keys.KEY_PAIR_MODEL;

public class LastTradesFragment extends Fragment implements LastTradesViewModel.DataListener {

    @Thunk
    FragmentLastTradesBinding binding;
    @Thunk LastTradesViewModel viewModel;

    private LastTradesAdapter mLastTradesAdapter;

    public static LastTradesFragment newInstance(WatchMarket pairModel) {
        Bundle args = new Bundle();
        args.putParcelable(KEY_PAIR_MODEL, pairModel);
        LastTradesFragment fragment = new LastTradesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_last_trades, container, false);
        viewModel = new LastTradesViewModel(getActivity(), this);

        viewModel.compositeDisposable.add(viewModel.mRxEventBus
                .filteredObservable(Events.NeedUpdateDataAfterPlaceOrder.class)
                .subscribe(o -> viewModel.onViewReady()));

        initArgs(getArguments());
        setUpAdapter();
        viewModel.onViewReady();
        return binding.getRoot();
    }

    private void initArgs(Bundle arguments) {
        if (arguments == null) return;
        if (arguments.containsKey(KEY_PAIR_MODEL))
            viewModel.lastTradeModel.setPairModel(arguments.getParcelable(KEY_PAIR_MODEL));
    }

    private void setUpAdapter(){
        mLastTradesAdapter = new LastTradesAdapter();
        binding.recycleLastTrades.setLayoutManager(new LinearLayoutManager(getActivity()));

        binding.recycleLastTrades.setAdapter(mLastTradesAdapter);
        mLastTradesAdapter.setPriceDecimal(((DexDetailsActivity) getActivity()).viewModel.dexDetailsModel.getWatchMarket().market.getPriceAssetInfo().decimals);

        mLastTradesAdapter.bindToRecyclerView(binding.recycleLastTrades);
        mLastTradesAdapter.setEmptyView(R.layout.dex_empty_view);

        binding.swipeContainer.setOnRefreshListener(() -> viewModel.onViewReady());
    }

    @Override
    public void successfullyGetLastTrades(List<TradesMarket> tradesMarket) {
        binding.swipeContainer.setRefreshing(false);
        mLastTradesAdapter.setNewData(tradesMarket);
    }
}
