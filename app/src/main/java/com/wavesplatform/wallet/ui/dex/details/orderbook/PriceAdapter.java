package com.wavesplatform.wallet.ui.dex.details.orderbook;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.payload.Price;
import com.wavesplatform.wallet.payload.WatchMarket;
import com.wavesplatform.wallet.util.MoneyUtil;

public class PriceAdapter extends BaseQuickAdapter<Price, BaseViewHolder> {
    private WatchMarket mTickerMarket;

    public PriceAdapter() {
        super(R.layout.order_book_item, null);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, Price price) {
        baseViewHolder.setTextColor(R.id.text_value, Color.parseColor("#808080"));
        baseViewHolder.setText(R.id.text_value, price.asks == null ?
                MoneyUtil.getScaledPrice(price.bids.price, mTickerMarket.market.getAmountAssetInfo().decimals, mTickerMarket.market.getPriceAssetInfo().decimals) :
                MoneyUtil.getScaledPrice(price.asks.price, mTickerMarket.market.getAmountAssetInfo().decimals, mTickerMarket.market.getPriceAssetInfo().decimals))
                .setBackgroundColor(R.id.relative_block, getData().indexOf(price) % 2 == 0  ? ContextCompat.getColor(mContext, R.color.dex_details_bg_lighter) : ContextCompat.getColor(mContext, R.color.dex_details_bg));
    }

    public void setTickerMarket(WatchMarket tickerMarket) {
        mTickerMarket = tickerMarket;
    }
}