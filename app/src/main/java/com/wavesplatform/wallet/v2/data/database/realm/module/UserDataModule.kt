/*
 * Created by Eduard Zaydel on 3/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.database.realm.module


import com.wavesplatform.wallet.v2.data.model.userdb.AddressBookUser
import com.wavesplatform.wallet.v2.data.model.userdb.AssetBalanceStore
import com.wavesplatform.wallet.v2.data.model.userdb.MarketResponseDb
import io.realm.annotations.RealmModule

@RealmModule(classes = [AddressBookUser::class, AssetBalanceStore::class, MarketResponseDb::class])
class UserDataModule