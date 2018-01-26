package com.wavesplatform.wallet.ui.dex.markets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.util.Pair;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.api.mather.MatherManager;
import com.wavesplatform.wallet.data.rxjava.RxUtil;
import com.wavesplatform.wallet.data.services.VerifiedAssetsService;
import com.wavesplatform.wallet.db.DBHelper;
import com.wavesplatform.wallet.injection.Injector;
import com.wavesplatform.wallet.payload.Market;
import com.wavesplatform.wallet.ui.base.BaseViewModel;
import com.wavesplatform.wallet.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.util.SSLVerifyUtil;
import com.wavesplatform.wallet.util.annotations.Thunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;

@SuppressWarnings("WeakerAccess")
public class MarketsViewModel extends BaseViewModel {

    @Thunk
    DataListener dataListener;
    private Context context;
    ArrayList<Market> currentCheckedMarkets;
    VerifiedAssetsService mVerifiedAssetsService;
    private List<Market> assetsToRemove = new ArrayList<>();

    @Inject
    SSLVerifyUtil sslVerifyUtil;

    MarketsViewModel(Context context, DataListener dataListener) {

        Injector.getInstance().getDataManagerComponent().inject(this);

        this.context = context;
        this.dataListener = dataListener;

        mVerifiedAssetsService = new VerifiedAssetsService();

        sslVerifyUtil.validateSSL();
    }

    @Override
    public void onViewReady() {
        // No-op
    }

    @Override
    public void destroy() {
        super.destroy();
        context = null;
        dataListener = null;
    }

    public interface DataListener {

        void onShowToast(@StringRes int message, @ToastCustom.ToastType String toastType);

        void successfullyGetAllMarkets(Map<String, String> verifiedAssets, List<Market> markets);

        void showProgressDialog(@StringRes int messageId, @Nullable String suffix);

        void dismissProgressDialog();

        void exitFromActivityWithData(List<Market> list);
    }

    public void getAllMarkets() {
        dataListener.showProgressDialog(R.string.dex_fetching_markets, null);

        compositeDisposable.add(MatherManager.get().getAllMarkets()
                .compose(RxUtil.applySchedulersToObservable())
                .flatMap(markets -> Observable.fromIterable(markets.markets))
                .flatMap(market -> {
                    for (Market o : getCurrentWatchlistMarkets()) {
                        if (o.equals(market)) {
                            market.checked = true;
                        }
                    }
                    return Observable.just(market);
                })
                .toList().toObservable()
                .flatMap(markets -> Observable.zip(mVerifiedAssetsService.getAllVerifiedAssets(), Observable.just(markets), (BiFunction<Map<String, String>, List<Market>, Pair>) Pair::new))
                .flatMap(pair -> Observable.fromIterable((List<Market>) pair.second)
                        .map(market -> {
                            String verifiedAmountAsset = ((Map<String, String>) pair.first).get(market.amountAsset);
                            String verifiedPriceAsset = ((Map<String, String>) pair.first).get(market.priceAsset);

                            if (verifiedAmountAsset != null) market.setAmountAssetName(verifiedAmountAsset);
                            if (verifiedPriceAsset != null) market.setPriceAssetName(verifiedPriceAsset);

                            if (verifiedAmountAsset != null && verifiedPriceAsset != null) market.verified = true;

//                            if (!market.verified) assetsToRemove.add(market);

                            return market;
                        })
                        .toList()
//                        .map(markets -> ((List<Market>) pair.second).removeAll(assetsToRemove))
                        .toObservable()
                        .map(market -> pair))
                .subscribe(pair -> {
                    if (dataListener != null) {
                        dataListener.successfullyGetAllMarkets((Map<String, String>) pair.first, (List<Market>) pair.second);
                        dataListener.dismissProgressDialog();
                    }
                }, err -> {
                    if (dataListener != null) {
                        dataListener.onShowToast(R.string.unexpected_error, ToastCustom.TYPE_ERROR);
                        dataListener.dismissProgressDialog();
                    }
                }));

    }

    public void filterOnlyCheckedMarkets(List<Market> markets) {
        compositeDisposable.add(Observable.fromIterable(markets)
                .filter(market -> market.checked)
                .toList()
                .subscribe(list -> {
                    if (dataListener != null)
                        dataListener.exitFromActivityWithData(list);
                }, Throwable::printStackTrace));
    }

    public ArrayList<Market> getCurrentWatchlistMarkets() {
        if (currentCheckedMarkets == null)
            currentCheckedMarkets = new ArrayList<>(DBHelper.getInstance().getRealm().where(Market.class).findAll());
        return currentCheckedMarkets;
    }

    public ArrayList<Market> filteredData(ArrayList<Market> marketArrayList) {
        ArrayList<Market> markets = new ArrayList<>();
        for (Market market : marketArrayList) {
            if (market.verified) markets.add(market);
        }
        return markets;
    }
}
