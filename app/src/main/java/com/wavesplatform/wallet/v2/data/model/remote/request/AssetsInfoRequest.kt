/*
 * Created by Eduard Zaydel on 20/6/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.remote.request

import com.google.gson.annotations.SerializedName

data class AssetsInfoRequest(@SerializedName("ids") var ids: List<String?>)