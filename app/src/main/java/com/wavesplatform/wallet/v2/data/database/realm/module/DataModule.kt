/*
 * Created by Eduard Zaydel on 3/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.database.realm.module

import com.wavesplatform.wallet.v2.data.model.db.*
import io.realm.annotations.RealmModule

@RealmModule(classes = [AssetBalanceDb::class, IssueTransactionDb::class, TransactionDb::class, DataDb::class,
    TransferDb::class, AssetPairDb::class, OrderDb::class, LeaseDb::class, AliasDb::class, SpamAssetDb::class,
    AssetInfoDb::class, Payment::class]) // todo check imports
class DataModule