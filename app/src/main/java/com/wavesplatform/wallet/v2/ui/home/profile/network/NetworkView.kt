/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.profile.network

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface NetworkView : BaseMvpView {
    fun afterSuccessCheckSpamUrl(isValid: Boolean)
}
