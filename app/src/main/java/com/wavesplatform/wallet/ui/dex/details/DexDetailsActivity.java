package com.wavesplatform.wallet.ui.dex.details;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.databinding.ActivityDexDetailsBinding;
import com.wavesplatform.wallet.payload.WatchMarket;
import com.wavesplatform.wallet.ui.base.BaseAuthActivity;
import com.wavesplatform.wallet.ui.dex.details.adapter.ViewPagerAdapter;
import com.wavesplatform.wallet.ui.dex.details.chart.ChartFragment;
import com.wavesplatform.wallet.ui.dex.details.last_trades.LastTradesFragment;
import com.wavesplatform.wallet.ui.dex.details.my_orders.MyOrdersFragment;
import com.wavesplatform.wallet.ui.dex.details.order.PlaceOrderActivity;
import com.wavesplatform.wallet.ui.dex.details.orderbook.OrderBookFragment;
import com.wavesplatform.wallet.util.annotations.Thunk;

import static com.wavesplatform.wallet.data.Keys.KEY_PAIR_MODEL;

public class DexDetailsActivity extends BaseAuthActivity implements DexDetailsViewModel.DataListener, TabLayout.OnTabSelectedListener {

    @Thunk
    public DexDetailsViewModel viewModel;

    public ActivityDexDetailsBinding binding;
    private Menu menu;
    private ChartFragment chartFragment;

    public static Intent newInstance(Context context, WatchMarket pairModel) {
        Intent intent = new Intent(context, DexDetailsActivity.class);
        intent.putExtra(KEY_PAIR_MODEL, pairModel);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dex_details);
        viewModel = new DexDetailsViewModel(this, this);

        initArgs(getIntent());
        initUI();
        setupViewPager();
    }

    private void initArgs(Intent intent) {
        if (intent != null)
            if (intent.getParcelableExtra(KEY_PAIR_MODEL) != null)
                viewModel.dexDetailsModel.setWatchMarket(intent.getParcelableExtra(KEY_PAIR_MODEL));
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new OrderBookFragment(), getString(R.string.dex_tab_orderbook));
        chartFragment = ChartFragment.newInstance(viewModel.dexDetailsModel.getWatchMarket());
        adapter.addFragment(chartFragment, getString(R.string.dex_tab_chart));
        adapter.addFragment(LastTradesFragment.newInstance(viewModel.dexDetailsModel.getWatchMarket()), getString(R.string.dex_tab_last_trades));
        adapter.addFragment(MyOrdersFragment.newInstance(viewModel.dexDetailsModel.getWatchMarket()), getString(R.string.dex_tab_my_orders));
        binding.viewpager.setAdapter(adapter);
//        binding.viewpager.setOffscreenPageLimit(4);
        binding.tabs.setupWithViewPager(binding.viewpager);
    }

    private void initUI() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_general);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.dex_bg_toolbar));
        toolbar.setTitle(getString(R.string.dex_details_toolbar_title, viewModel.dexDetailsModel.getWatchMarket().market.amountAssetName, viewModel.dexDetailsModel.getWatchMarket().market.priceAssetName));
        setSupportActionBar(toolbar);

        binding.tabs.addOnTabSelectedListener(this);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (menu != null){
            if (tab.getPosition() == 1) {
                menu.findItem(R.id.action_timeframe).setVisible(true);
                menu.findItem(R.id.action_timeframe).setTitle(returnTitleFromMin(viewModel.dexDetailsModel.getWatchMarket().market.currentTimeFrame));
            }
            else menu.findItem(R.id.action_timeframe).setVisible(false);
        }
        if (tab.getPosition() == 3) {
            MyOrdersFragment myOrdersFragment = (MyOrdersFragment) binding.viewpager.getAdapter().instantiateItem(binding.viewpager, binding.viewpager.getCurrentItem());
            myOrdersFragment.sendRequest();
        }
    }

    public String returnTitleFromMin(Integer currentTimeFrame){
        if (currentTimeFrame == null) currentTimeFrame = 30;

        if (currentTimeFrame == (5)) return "M5";
        if (currentTimeFrame == (15)) return "M15";
        if (currentTimeFrame == (30)) return "M30";
        if (currentTimeFrame == (60)) return "1H";
        if (currentTimeFrame == (240)) return "4H";
        if (currentTimeFrame == (1440)) return "1D";
        return "M30";
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_create_order) {
            startActivity(PlaceOrderActivity.newInstance(this, viewModel.dexDetailsModel.getWatchMarket(), null));
            return true;
        } else if (item.getItemId() == R.id.action_timeframe) {
            chartFragment.menu = menu;
            PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.action_timeframe));
            popupMenu.inflate(R.menu.menu_time_frame);
            popupMenu.setOnMenuItemClickListener(chartFragment);
            popupMenu.show();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_dex_details_chart, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
