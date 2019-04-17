/*
 * Created by Eduard Zaydel on 5/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.database.realm.module


import com.wavesplatform.wallet.v2.data.model.db.userdb.AddressBookUserDb
import com.wavesplatform.wallet.v2.data.model.db.userdb.AssetBalanceStoreDb
import com.wavesplatform.wallet.v2.data.model.db.userdb.MarketResponseDb
import io.realm.annotations.RealmModule

@RealmModule(classes = [AddressBookUserDb::class, AssetBalanceStoreDb::class, MarketResponseDb::class])
class UserDataModule