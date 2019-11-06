/*
 * Created by Eduard Zaydel on 8/10/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.service.configs

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
open class SpamAssetResponse(@SerializedName("assetId") var assetId: String? = "") : Parcelable