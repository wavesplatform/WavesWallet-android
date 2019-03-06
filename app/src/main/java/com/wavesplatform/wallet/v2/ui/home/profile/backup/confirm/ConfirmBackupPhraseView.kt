package com.wavesplatform.wallet.v2.ui.home.profile.backup.confirm

import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface ConfirmBackupPhraseView : BaseMvpView {
    fun showRandomPhraseList(listRandomPhrase: ArrayList<String>)
}
