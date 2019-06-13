/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.sdk.model.response

import com.google.gson.annotations.SerializedName

data class HeightResponse(@SerializedName("height") var height: Int = 0)