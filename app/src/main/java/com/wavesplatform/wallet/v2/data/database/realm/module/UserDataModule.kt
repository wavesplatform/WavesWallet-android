/*
 * Created by Eduard Zaydel on 5/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.database.realm.module

import com.wavesplatform.wallet.v2.data.model.remote.response.MarketResponse
import com.wavesplatform.wallet.v2.data.model.userdb.AddressBookUser
import com.wavesplatform.wallet.v2.data.model.userdb.AssetBalanceStore
import io.realm.annotations.RealmModule

@RealmModule(classes = [AddressBookUser::class, AssetBalanceStore::class, MarketResponse::class])
class UserDataModule