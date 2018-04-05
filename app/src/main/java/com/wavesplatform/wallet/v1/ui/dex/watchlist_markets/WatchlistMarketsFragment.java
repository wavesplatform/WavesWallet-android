package com.wavesplatform.wallet.v1.ui.dex.watchlist_markets;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.github.rubensousa.bottomsheetbuilder.BottomSheetBuilder;
import com.github.rubensousa.bottomsheetbuilder.BottomSheetMenuDialog;
import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.databinding.FragmentDexBinding;
import com.wavesplatform.wallet.v1.payload.Market;
import com.wavesplatform.wallet.v1.payload.TickerMarket;
import com.wavesplatform.wallet.v1.payload.TradesMarket;
import com.wavesplatform.wallet.v1.payload.WatchMarket;
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.v1.ui.dex.DividerItemDecoration;
import com.wavesplatform.wallet.v1.ui.dex.details.DexDetailsActivity;
import com.wavesplatform.wallet.v1.ui.dex.markets.MarketsActivity;
import com.wavesplatform.wallet.v1.util.AndroidUtils;
import com.wavesplatform.wallet.v1.util.ViewUtils;
import com.wavesplatform.wallet.v1.util.annotations.Thunk;

import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

public class WatchlistMarketsFragment extends Fragment implements WatchlistMarketsViewModel.DataListener {

    private static final String ARG_SELECTED_ACCOUNT_POSITION = "selected_account_position";
    private static final int COOL_DOWN_MILLIS = 2 * 1000;
    public static final int GET_NEW_MARKETS = 99;

    @Thunk
    WatchlistMarketsViewModel viewModel;
    @Thunk
    FragmentDexBinding binding;
    private WatchlistMarketsAdapter mWatchlistMarketsAdapter;

    private OnDexFragmentInteractionListener listener;
    private long backPressed;

    public WatchlistMarketsFragment() {
        // Required empty public constructor
    }

    public static WatchlistMarketsFragment newInstance(int selectedAccountPosition) {
        WatchlistMarketsFragment fragment = new WatchlistMarketsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SELECTED_ACCOUNT_POSITION, selectedAccountPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_dex, container, false);
        viewModel = new WatchlistMarketsViewModel(getActivity(), this);

        setupToolbar();

        viewModel.onViewReady();

        setupAdapter();

        setupViews();

        setHasOptionsMenu(true);

        return binding.getRoot();
    }

    private void setupViews() {
        binding.fabAdd.setOnClickListener(v -> {
            startActivityForResult(MarketsActivity.getStartIntent(getActivity()), GET_NEW_MARKETS);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_NEW_MARKETS) {
            if (resultCode == RESULT_OK) {
                viewModel.updateCurrentWatchlistMarkets(data.getParcelableArrayListExtra("list"));
                fillAdapter();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        fillAdapter();
    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.compositeDisposable.clear();
    }

    private void fillAdapter() {
        viewModel.compositeDisposable.clear();
        ArrayList<WatchMarket> watchMarkets = new ArrayList<>();
        ArrayList<Market> markets = new ArrayList<>(viewModel.getCurrentWatchlistMarkets());
        for (Market market : markets) {
            if (market.amountAsset.equals(viewModel.defaultAmount) && market.priceAsset.equals(viewModel.defaultPrice)) watchMarkets.add(0, new WatchMarket(market, null, null));
            else watchMarkets.add(new WatchMarket(market, null, null));
        }
        mWatchlistMarketsAdapter.setNewData(watchMarkets);
        for (int i = 0; i < mWatchlistMarketsAdapter.getData().size(); i++) {
            viewModel.getTickerByPair(i, mWatchlistMarketsAdapter.getData().get(i));
            viewModel.getTradesByPair(i, mWatchlistMarketsAdapter.getData().get(i));
        }
        binding.swipeContainer.setRefreshing(false);
    }

    private void setupToolbar() {
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(R.string.dex_nav);

            ViewUtils.setElevation(
                    getActivity().findViewById(R.id.appbar_layout),
                    ViewUtils.convertDpToPixel(5F, getContext()));
        } else {
            finishPage();
        }
    }

    private void setupAdapter() {
        mWatchlistMarketsAdapter = new WatchlistMarketsAdapter();
        binding.recycleAssets.setLayoutManager(new LinearLayoutManager(getActivity()));
        //binding.recycleAssets.addItemDecoration(new DividerItemDecoration(getActivity()));

        mWatchlistMarketsAdapter.bindToRecyclerView(binding.recycleAssets);
        mWatchlistMarketsAdapter.setEmptyView(R.layout.dex_empty_view);

        binding.swipeContainer.setOnRefreshListener(this::fillAdapter);

        binding.recycleAssets.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if (dy > 0)
                    binding.fabAdd.hide();
                else if (dy < 0)
                    binding.fabAdd.show();
            }
        });

        mWatchlistMarketsAdapter.setOnItemClickListener((adapter, view, position) -> {
            WatchMarket market = mWatchlistMarketsAdapter.getData().get(position);
            startActivity(DexDetailsActivity.newInstance(getActivity(), market));
        });

        mWatchlistMarketsAdapter.setOnItemLongClickListener((adapter, view, position) -> {
            BottomSheetMenuDialog dialog = new BottomSheetBuilder(getActivity())
                    .setMode(BottomSheetBuilder.MODE_LIST)
                    .setMenu(R.menu.menu_watchlist_markets)
                    .setItemClickListener(item -> {
                        WatchMarket watchMarket = mWatchlistMarketsAdapter.getItem(position);
                        viewModel.deleteMarketFromWathclist(watchMarket.market);
                        viewModel.compositeDisposable.clear();
                        fillAdapter();
                    })
                    .createDialog();

            dialog.show();
            return true;
        });

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.menu_receive, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem menuItem = menu.findItem(R.id.action_qr_main);
        menuItem.setVisible(false);
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
    public void onDestroy() {
        super.onDestroy();
        viewModel.destroy();
    }

    @Override
    public void onShowToast(@StringRes int message, @ToastCustom.ToastType String toastType) {
        ToastCustom.makeText(getActivity(), getString(message), ToastCustom.LENGTH_SHORT, toastType);
    }

    @Override
    public void successfullyGetTickerByPair(int position, TickerMarket markets) {
        WatchMarket watchMarket = mWatchlistMarketsAdapter.getData().get(position);
        watchMarket.tickerMarket = markets;
        mWatchlistMarketsAdapter.setData(position, watchMarket);
    }

    @Override
    public void successfullyGetTradesByPair(int position, TradesMarket markets) {
        WatchMarket watchMarket = mWatchlistMarketsAdapter.getData().get(position);
        watchMarket.tradesMarket = markets;
        mWatchlistMarketsAdapter.setData(position, watchMarket);
    }

    @Override
    public void finishPage() {
        if (listener != null) {
            listener.onDexFragmentClose();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnDexFragmentInteractionListener) {
            listener = (OnDexFragmentInteractionListener
                    ) context;
        } else {
            throw new RuntimeException(context + " must implement OnReceiveFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnDexFragmentInteractionListener {

        void onDexFragmentClose();

    }
}
