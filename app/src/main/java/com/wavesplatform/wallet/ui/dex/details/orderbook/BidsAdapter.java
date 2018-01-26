package com.wavesplatform.wallet.ui.dex.details.orderbook;

import android.support.v4.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.payload.OrderBook;
import com.wavesplatform.wallet.payload.WatchMarket;
import com.wavesplatform.wallet.util.MoneyUtil;

public class BidsAdapter extends BaseQuickAdapter<OrderBook.Bids, BaseViewHolder> {
    private WatchMarket mTickerMarket;

    public BidsAdapter() {
        super(R.layout.order_book_item, null);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, OrderBook.Bids bids) {
        baseViewHolder.setText(R.id.text_value, MoneyUtil.getTextStripZeros(MoneyUtil.getScaledText(Long.valueOf(bids.amount), mTickerMarket.market.getAmountAssetInfo().decimals)))
                .setBackgroundColor(R.id.relative_block, getData().indexOf(bids) % 2 == 0  ? ContextCompat.getColor(mContext, R.color.dex_orderbook_left_bg) : ContextCompat.getColor(mContext, R.color.dex_orderbook_left_bg_lighter));
    }

    public void setTickerMarket(WatchMarket tickerMarket) {
        mTickerMarket = tickerMarket;
    }
}