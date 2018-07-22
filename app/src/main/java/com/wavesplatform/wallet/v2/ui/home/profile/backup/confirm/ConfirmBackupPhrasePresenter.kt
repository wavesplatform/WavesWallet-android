package com.wavesplatform.wallet.v2.ui.home.profile.backup.confirm

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class ConfirmBackupPhrasePresenter @Inject constructor() : BasePresenter<ConfirmBackupPhraseView>() {

    var originPhraseList = ArrayList<String>()
    var originPhraseString = ""

    fun getRandomPhrasePositions(originPhrase: ArrayList<String>) {
        this.originPhraseList = originPhrase

        originPhraseString = originPhrase.toString().replace("[", "").replace("]", "").replace(",", "")

        val randomList = originPhraseList
        randomList.shuffle()

        viewState.showRandomPhraseList(randomList)
    }
}
