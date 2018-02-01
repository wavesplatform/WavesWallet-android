package com.wavesplatform.wallet.ui.dex.details.my_orders;

import android.support.v4.content.ContextCompat;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.data.enums.OrderStatus;
import com.wavesplatform.wallet.payload.MyOrder;
import com.wavesplatform.wallet.payload.WatchMarket;
import com.wavesplatform.wallet.util.DateUtil;
import com.wavesplatform.wallet.util.MoneyUtil;
import com.wavesplatform.wallet.util.StringUtils;

import java.math.BigInteger;

public class MyOrdersAdapter extends BaseQuickAdapter<MyOrder, BaseViewHolder> {
    WatchMarket mWatchMarket;
    DateUtil mDateUtil;

    public MyOrdersAdapter() {
        super(R.layout.my_order_item, null);
        mDateUtil = new DateUtil(mContext);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, MyOrder myOrder) {
        int color = myOrder.type.equals("buy") ? ContextCompat.getColor(mContext, R.color.dex_orderbook_left_bg) : ContextCompat.getColor(mContext, R.color.dex_orderbook_right_bg);
        baseViewHolder
                .setText(R.id.text_side, StringUtils.capitalize(myOrder.type))
                .setText(R.id.text_price, MoneyUtil.getScaledPrice(myOrder.price, mWatchMarket.market.getAmountAssetInfo().decimals, mWatchMarket.market.getPriceAssetInfo().decimals))
                .setText(R.id.text_date, mDateUtil.timestampToDate(myOrder.timestamp))
                .setText(R.id.text_time, mDateUtil.timestampToDateTime(myOrder.timestamp))
                .setText(R.id.text_amount, MoneyUtil.getTextStripZeros(MoneyUtil.getScaledText(myOrder.amount, mWatchMarket.market.getAmountAssetInfo().decimals)))
                .setText(R.id.text_sum, MoneyUtil.getTextStripZeros(MoneyUtil.getTextStripZeros(BigInteger.valueOf(myOrder.amount).multiply(BigInteger.valueOf(myOrder.price)).divide(BigInteger.valueOf(100000000)).longValue(), mWatchMarket.market.getPriceAssetInfo().decimals)))
                .setText(R.id.text_status, myOrder.getStatus().getStatus())
                .setText(R.id.text_failed_value, MoneyUtil.getTextStripZeros(MoneyUtil.getTextStripZeros(myOrder.filled, mWatchMarket.market.getAmountAssetInfo().decimals)))
                .setVisible(R.id.text_failed_value, myOrder.getStatus() == OrderStatus.Filled || myOrder.getStatus() == OrderStatus.PartiallyFilled)
                .setTextColor(R.id.text_failed_value, ContextCompat.getColor(mContext,myOrder.getStatus().getColor()))
                .setTextColor(R.id.text_status, ContextCompat.getColor(mContext,myOrder.getStatus().getColor()))
                .setTextColor(R.id.text_side, color)
                .addOnClickListener(R.id.image_delete)
                .setTextColor(R.id.text_price, color)
                .setBackgroundColor(R.id.my_order_container, getData().indexOf(myOrder) % 2 == 0 ? ContextCompat.getColor(mContext, R.color.dex_details_bg) : ContextCompat.getColor(mContext, R.color.dex_details_bg_lighter));
    }

    public void setWatchMarket(WatchMarket watchMarket) {
        mWatchMarket = watchMarket;
    }
}
