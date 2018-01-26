package com.wavesplatform.wallet.ui.dex.watchlist_markets;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.payload.WatchMarket;
import com.wavesplatform.wallet.util.DateUtil;
import com.wavesplatform.wallet.util.MoneyUtil;

import java.math.BigDecimal;

public class WatchlistMarketsAdapter extends BaseQuickAdapter<WatchMarket, BaseViewHolder> {
    private DateUtil mDateUtil;

    public WatchlistMarketsAdapter() {
        super(R.layout.watchlist_markets_item, null);
        mDateUtil = new DateUtil(mContext);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, WatchMarket market) {

        RecyclerView.LayoutParams param = (RecyclerView.LayoutParams)baseViewHolder.itemView.getLayoutParams();
        if (market.tickerMarket == null || market.tradesMarket == null) {
            param.height = 0;
            param.width = 0;
            baseViewHolder.itemView.setLayoutParams(param);
            baseViewHolder.itemView.setVisibility(View.GONE);
            return;
        } else {
            param.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            param.width = LinearLayout.LayoutParams.MATCH_PARENT;
            baseViewHolder.itemView.setLayoutParams(param);
            baseViewHolder.itemView.setVisibility(View.VISIBLE);
        }

        baseViewHolder
                .setText(R.id.text_markets_pair, String.format("%s/%s", market.market.amountAssetName,  market.market.priceAssetName))
                .setText(R.id.text_high, String.format(mContext.getString(R.string.dex_watchlist_markets_high, market.tickerMarket.high24h)))
                .setText(R.id.text_low, String.format(mContext.getString(R.string.dex_watchlist_markets_low, market.tickerMarket.low24h)))
                .setText(R.id.text_price_diff, MoneyUtil.getTextStripZeros(new BigDecimal(market.tradesMarket.price).subtract(new BigDecimal(market.tickerMarket.open24h)).toPlainString()))
                .setText(R.id.text_percent, Double.isInfinite((((Double.valueOf(market.tradesMarket.price) - Double.valueOf(market.tickerMarket.open24h))  * 100) / Double.valueOf(market.tickerMarket.open24h))) ? "0%" : String.valueOf(MoneyUtil.round((((Double.valueOf(market.tradesMarket.price) - Double.valueOf(market.tickerMarket.open24h))  * 100) / Double.valueOf(market.tickerMarket.open24h)), 2) + "%"))
                .setText(R.id.text_price_value, MoneyUtil.getTextStripZeros(String.valueOf(market.tradesMarket.price)))
                .setText(R.id.text_time, mDateUtil.timestampToDateTime(Long.parseLong(market.tradesMarket.timestamp)))
                .setTextColor(R.id.text_price_value, Double.valueOf(market.tradesMarket.price) >= Double.valueOf(market.tickerMarket.open24h) ? ContextCompat.getColor(mContext, R.color.dex_watchlist_markets_up) : ContextCompat.getColor(mContext, R.color.dex_watchlist_markets_down))
                .setTextColor(R.id.text_percent, Double.valueOf(market.tradesMarket.price) >= Double.valueOf(market.tickerMarket.open24h) ? ContextCompat.getColor(mContext, R.color.dex_watchlist_markets_up) : ContextCompat.getColor(mContext, R.color.dex_watchlist_markets_down))
                .setTextColor(R.id.text_price_diff, Double.valueOf(market.tradesMarket.price) >= Double.valueOf(market.tickerMarket.open24h) ? ContextCompat.getColor(mContext, R.color.dex_watchlist_markets_up) : ContextCompat.getColor(mContext, R.color.dex_watchlist_markets_down))
                .setImageResource(R.id.image_arrow, Double.valueOf(market.tradesMarket.price) >= Double.valueOf(market.tickerMarket.open24h) ?  R.drawable.ic_arrow_up_green : R.drawable.ic_arrow_down_red);
    }
}