/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.wallet.assets

class TestObject(
    val assetName: String,
    val isFavourite: Boolean,
    val isOut: Boolean,
    val assetValue: Double,
    val bitcoinValue: Double,
    val isSpam: Boolean = false
)
