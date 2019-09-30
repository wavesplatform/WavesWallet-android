/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.migration.new_security_level

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.auth.new_account.secret_phrase.SecretPhraseView
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class NewLevelOfSecurityPresenter @Inject constructor() : BasePresenter<SecretPhraseView>()
