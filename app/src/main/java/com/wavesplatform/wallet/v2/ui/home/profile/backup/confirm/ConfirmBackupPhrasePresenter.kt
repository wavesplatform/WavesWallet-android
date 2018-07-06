package com.wavesplatform.wallet.v2.ui.home.profile.backup.confirm

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@InjectViewState
class ConfirmBackupPhrasePresenter @Inject constructor() : BasePresenter<ConfirmBackupPhraseView>() {

    var originPhraseList = ArrayList<String>()
    var originPhraseString = ""

    fun getRandomPhrasePositions(originPhrase: ArrayList<String>) {
        this.originPhraseList = originPhrase

        originPhraseString = originPhrase.toString().replace("[", "").replace("]", "").replace(",", "")

        var listRandomPhrase = arrayListOf<String>()
        var randomNumbers = HashMap<Int, Int>()

        for (i in 0 until originPhrase.size) {
            getRandomPhrasePosition(randomNumbers, listRandomPhrase)
        }

        viewState.showRandomPhraseList(listRandomPhrase)
    }

    fun getRandomPhrasePosition(randomNumbers: HashMap<Int, Int>, listRandomPhrase: ArrayList<String>) {
        val randomPosition = Random().nextInt(originPhraseList.size)
        if (randomNumbers.contains(randomPosition)) {
            getRandomPhrasePosition(randomNumbers, listRandomPhrase)
        } else {
            randomNumbers[randomPosition] = randomPosition
            listRandomPhrase.add(originPhraseList[randomPosition])
        }
    }
}
