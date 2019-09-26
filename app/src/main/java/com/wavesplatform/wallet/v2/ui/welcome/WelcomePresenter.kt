/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.welcome

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.WelcomeItem
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class WelcomePresenter @Inject constructor() : BasePresenter<WelcomeView>() {

    fun saveLanguage(lang: String) {
        preferenceHelper.setLanguage(lang)
    }

    fun getTutorialSliderData(): MutableList<WelcomeItem> {
        return mutableListOf(
                WelcomeItem(R.drawable.userimg_blockchain_80,
                        R.string.welcome_blockchain_title,
                        R.string.welcome_blockchain_description),
                WelcomeItem(R.drawable.userimg_wallet_80,
                        R.string.welcome_wallet_title,
                        R.string.welcome_wallet_description),
                WelcomeItem(R.drawable.userimg_dex_80,
                        R.string.welcome_dex_title,
                        R.string.welcome_dex_description)
        )
    }
}
