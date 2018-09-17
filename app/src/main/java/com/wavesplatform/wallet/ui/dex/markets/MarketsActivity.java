package com.wavesplatform.wallet.ui.dex.markets;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;

import com.jakewharton.rxbinding2.support.v7.widget.RxSearchView;
import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.databinding.ActivityMarketsBinding;
import com.wavesplatform.wallet.payload.Market;
import com.wavesplatform.wallet.ui.base.BaseAuthActivity;
import com.wavesplatform.wallet.ui.customviews.MaterialProgressDialog;
import com.wavesplatform.wallet.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.ui.dex.DividerItemDecoration;
import com.wavesplatform.wallet.ui.dex.markets.add.AddMarketActivity;
import com.wavesplatform.wallet.util.annotations.Thunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;

public class MarketsActivity extends BaseAuthActivity implements MarketsViewModel.DataListener {

    public static final int REQUEST_CODE_ADD = 9;
    private MarketsViewModel mViewModel;
    private ActivityMarketsBinding binding;
    private MarketsAdapter mMarketsAdapter;
    private MaterialProgressDialog materialProgressDialog;
    private SearchView searchView;
    private Handler mHandler = new Handler();


    public static Intent getStartIntent(Context context) {
        return new Intent(context, MarketsActivity.class);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD) {
            if (resultCode == RESULT_OK) finish();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_markets);

        setSupportActionBar(binding.toolbar);
        binding.toolbar.setNavigationOnClickListener(v -> {
            filterMarketsAndExit();
        });


        getSupportActionBar().setTitle(getString(R.string.dex_markets_toolbar_title));

        mViewModel = new MarketsViewModel(this, this);
        mViewModel.onViewReady();

        mMarketsAdapter = new MarketsAdapter();
        binding.recycleMarkets.setLayoutManager(new LinearLayoutManager(this));
        binding.recycleMarkets.addItemDecoration(new DividerItemDecoration(this));
        mMarketsAdapter.bindToRecyclerView(binding.recycleMarkets);
        mMarketsAdapter.setEmptyView(R.layout.dex_empty_view);

        binding.swipeContainer.setOnRefreshListener(() ->  mViewModel.getAllMarkets());

        mMarketsAdapter.setOnItemClickListener((adapter, view, position) -> {
            CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox);
            Market market = mMarketsAdapter.getData().get(position);
            market.checked = !market.checked;
            checkbox.setChecked(market.checked);
        });

        mViewModel.getAllMarkets();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.markets_actions, menu);

        MenuItem myActionMenuItem = menu.findItem(R.id.action_search);

        MenuItem add = menu.findItem(R.id.action_add);
        MenuItem show = menu.findItem(R.id.action_show);
        MenuItem hide = menu.findItem(R.id.action_hide);
        add.setOnMenuItemClickListener(item -> {
            startActivityForResult(AddMarketActivity.getStartIntent(this), REQUEST_CODE_ADD);
            return true;
        });
        show.setOnMenuItemClickListener(item -> {
            new Handler().postDelayed(() -> {
                show.setVisible(false);
                hide.setVisible(true);
                mMarketsAdapter.setShowUnVerifiedAssets(true);
                mMarketsAdapter.setNewData(new ArrayList<>(mMarketsAdapter.allData));
                mMarketsAdapter.currentData = mMarketsAdapter.allData;
            }, 150);
            return true;
        });
        hide.setVisible(false);
        hide.setOnMenuItemClickListener(item -> {
            new Handler().postDelayed(() -> {
                show.setVisible(true);
                hide.setVisible(false);
                mMarketsAdapter.setShowUnVerifiedAssets(false);
                mMarketsAdapter.setNewData(mViewModel.filteredData(mMarketsAdapter.allData));
                mMarketsAdapter.currentData = mViewModel.filteredData(mMarketsAdapter.allData);
            }, 150);
            return true;
        });

        searchView = (SearchView) MenuItemCompat.getActionView(myActionMenuItem);

        mViewModel.compositeDisposable.add(RxSearchView.queryTextChangeEvents(searchView)
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(searchViewQueryTextEvent -> {
                    if (searchViewQueryTextEvent.isSubmitted()) {
                        if (!searchView.isIconified()) {
                            searchView.setIconified(true);
                        }
                        searchView.onActionViewCollapsed();
                        searchView.clearFocus();
                        MenuItemCompat.collapseActionView(myActionMenuItem);
                    }
                    mMarketsAdapter.filter(searchViewQueryTextEvent.queryText().toString());
                }));

        return true;
    }

    @Thunk
    void onViewReady() {
        mViewModel.onViewReady();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewModel.destroy();
    }

    @Override
    public void onBackPressed() {
        filterMarketsAndExit();
    }

    private void filterMarketsAndExit() {
        showProgressDialog(R.string.dex_markets_saving, null);
        mViewModel.filterOnlyCheckedMarkets(mMarketsAdapter.getAllData());
    }

    @Override
    public void exitFromActivityWithData(List<Market> list) {
        dismissProgressDialog();
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra("list",new ArrayList<>(list));
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void showProgressDialog(@StringRes int messageId, @Nullable String suffix) {
        dismissProgressDialog();
        materialProgressDialog = new MaterialProgressDialog(this);
        materialProgressDialog.setCancelable(false);
        if (suffix != null) {
            materialProgressDialog.setMessage(getString(messageId) + suffix);
        } else {
            materialProgressDialog.setMessage(getString(messageId));
        }

        if (!this.isFinishing()) materialProgressDialog.show();
    }

    @Override
    public void dismissProgressDialog() {
        if (materialProgressDialog != null && materialProgressDialog.isShowing()) {
            materialProgressDialog.dismiss();
            materialProgressDialog = null;
        }
    }

    @Override
    public void onShowToast(@StringRes int message, @ToastCustom.ToastType String toastType) {
        binding.swipeContainer.setRefreshing(false);
        ToastCustom.makeText(this, getString(message), ToastCustom.LENGTH_SHORT, toastType);
    }

    @Override
    public void successfullyGetAllMarkets(Map<String, String> verifiedAssets, List<Market> markets) {
        binding.swipeContainer.setRefreshing(false);
        mMarketsAdapter.setVerifiedAssets(verifiedAssets);
        mMarketsAdapter.setAllData(new ArrayList<>(markets));
        if (mMarketsAdapter.isShowUnVerifiedAssets()) {
            mMarketsAdapter.setNewData(markets);
            mMarketsAdapter.currentData = new ArrayList<>(markets);
        }
        else {
            mMarketsAdapter.setNewData(mViewModel.filteredData(mMarketsAdapter.allData));
            mMarketsAdapter.currentData = mViewModel.filteredData(mMarketsAdapter.allData);
        }
    }
}
