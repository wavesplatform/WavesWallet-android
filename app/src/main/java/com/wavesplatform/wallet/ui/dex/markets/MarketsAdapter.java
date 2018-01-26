package com.wavesplatform.wallet.ui.dex.markets;

import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.payload.Market;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MarketsAdapter extends BaseQuickAdapter<Market, BaseViewHolder> {

    ArrayList<Market> allData = new ArrayList();
    public ArrayList<Market> currentData = new ArrayList();
    Map<String, String> verifiedAssets = new HashMap<>();
    String WAVES = "WAVES";
    private Handler mHandler = new Handler();
    private boolean showUnVerifiedAssets = false;

    public MarketsAdapter() {
        super(R.layout.markets_item, null);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, Market market) {
        baseViewHolder
                .setText(R.id.text_amount_name, market.amountAssetName)
                .setText(R.id.text_amount, market.amountAsset)
                .setText(R.id.text_price, market.priceAsset)
                .setText(R.id.text_price_name, market.priceAssetName)
                .setOnCheckedChangeListener(R.id.checkbox, null)
                .setChecked(R.id.checkbox, market.checked)
                .setOnCheckedChangeListener(R.id.checkbox, (buttonView, isChecked) -> {
                    market.checked = isChecked;
                    mHandler.post(() -> {
                        setData(getData().indexOf(market), market);
                        allData.set(allData.indexOf(market), market);
                    });
                });

        ImageView imageAmount = baseViewHolder.getView(R.id.image_amount);
        ImageView imagePrice = baseViewHolder.getView(R.id.image_price);

        imageAmount.setImageResource(0);
        imagePrice.setImageResource(0);

        if (assetIsVerified(market.amountAsset) != null) imageAmount.setImageResource(R.drawable.ic_verified);
        if (assetIsVerified(market.priceAsset) != null) imagePrice.setImageResource(R.drawable.ic_verified);

        if (market.priceAssetName.equals(WAVES)) imagePrice.setImageResource(R.drawable.ic_color_logo);
        if (market.amountAssetName.equals(WAVES)) imageAmount.setImageResource(R.drawable.ic_color_logo);

        if (imageAmount.getDrawable() == null) imageAmount.setVisibility(View.GONE);
        else imageAmount.setVisibility(View.VISIBLE);
        if (imagePrice.getDrawable() == null) imagePrice.setVisibility(View.GONE);
        else imagePrice.setVisibility(View.VISIBLE);
    }

    public void filter(String text) {
        getData().clear();
        if(text.trim().isEmpty()){
            setNewData(new ArrayList<>(currentData));
        } else{
            text = text.toLowerCase();
            for(Market item: currentData){
                StringBuilder stringBuilder = new StringBuilder().append(item.amountAssetName.toLowerCase()).append("/").append(item.priceAssetName.toLowerCase());
                if(stringBuilder.toString().contains(text)){
                    getData().add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public void setAllData(ArrayList<Market> allData) {
        this.allData = allData;
    }

    public ArrayList<Market> getAllData() {
        return allData;
    }

    public Map<String, String> getVerifiedAssets() {
        return verifiedAssets;
    }

    public void setVerifiedAssets(Map<String, String> verifiedAssets) {
        this.verifiedAssets = verifiedAssets;
    }

    public String assetIsVerified(String assetId){
        return verifiedAssets.get(assetId);
    }

    public void setShowUnVerifiedAssets(boolean showUnVerifiedAssets) {
        this.showUnVerifiedAssets = showUnVerifiedAssets;
    }

    public boolean isShowUnVerifiedAssets() {
        return showUnVerifiedAssets;
    }
}