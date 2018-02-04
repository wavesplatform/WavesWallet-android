package com.wavesplatform.wallet.ui.dex.details.last_trades;

import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.payload.TradesMarket;
import com.wavesplatform.wallet.util.DateUtil;
import java.text.DecimalFormat;

public class LastTradesAdapter extends BaseQuickAdapter<TradesMarket, BaseViewHolder> {
    private DateUtil mDateUtil;
    private int priceDecimal;

    public LastTradesAdapter() {
        super(R.layout.last_trade_item, null);
        mDateUtil = new DateUtil(mContext);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, TradesMarket market) {

        View container = baseViewHolder.getView(R.id.last_trade_container);
        if (getData().indexOf(market) % 2 == 0) container.setBackgroundColor(ContextCompat.getColor(mContext, R.color.dex_details_bg));
        else container.setBackgroundColor(ContextCompat.getColor(mContext, R.color.dex_details_bg_lighter));

        TextView textPrice = baseViewHolder.getView(R.id.text_price);
        if (market.type.equals("sell"))
            textPrice.setTextColor(ContextCompat.getColor(mContext, R.color.dex_watchlist_markets_down));
        else
            textPrice.setTextColor(ContextCompat.getColor(mContext, R.color.dex_watchlist_markets_up));

        double sum = Double.valueOf(market.price) * Double.valueOf(market.amount);

        baseViewHolder.setText(R.id.text_time, mDateUtil.timestampToDateTime(Long.parseLong(market.timestamp)))
                .setText(R.id.text_price, market.price)
                .setText(R.id.text_amount, market.amount)
                .setText(R.id.text_sum, sumToString(sum));
    }


    private String sumToString(Double sum){
        DecimalFormat df = new DecimalFormat("0");
        df.setMaximumFractionDigits(priceDecimal);
        return df.format(sum);
    }

    public void setPriceDecimal(int priceDecimal) {
        this.priceDecimal = priceDecimal;
    }
}