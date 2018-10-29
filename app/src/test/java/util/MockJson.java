package util;

import com.google.gson.reflect.TypeToken;
import com.wavesplatform.wallet.payload.AssetBalances;
import com.wavesplatform.wallet.payload.Transaction;
import com.wavesplatform.wallet.payload.TransactionsInfo;
import com.wavesplatform.wallet.payload.WavesBalance;

import java.lang.reflect.Type;
import java.util.List;

public enum MockJson {

    SCAM_TOKENS("/scam/scam_tokens.txt", new TypeToken<String>(){}.getType()),
    ASSETS_BALANCE("/matcher/assets_balance.json", new TypeToken<AssetBalances>(){}.getType()),
    WAVES_BALANCE("/matcher/waves_balance.json", new TypeToken<WavesBalance>(){}.getType()),
    UNCONFIRMED_TRANSACTIONS("/matcher/unconfirmed_transactions.json", new TypeToken<List<Transaction>>(){}.getType()),
    TRANSACTIONS_INFO("/matcher/transactions_info.json", new TypeToken<TransactionsInfo>(){}.getType()),
    TRANSACTIONS("/matcher/transactions.json", new TypeToken<List<List<Transaction>>>(){}.getType());

    private String path;
    private Type type;

    MockJson(String path, Type type) {
        this.path = path;
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public Type getType() {
        return type;
    }
}
