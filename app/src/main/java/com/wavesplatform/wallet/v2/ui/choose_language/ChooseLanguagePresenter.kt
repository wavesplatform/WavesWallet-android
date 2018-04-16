package com.wavesplatform.wallet.v2.ui.choose_language

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.Language
import com.wavesplatform.wallet.v2.ui.choose_language.ChooseLanguageView
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class ChooseLanguagePresenter @Inject constructor() : BasePresenter<ChooseLanguageView>() {
    var currentLanguagePosition: Int = -1

    fun getLanguages(): MutableList<Language>? {
        return arrayListOf(Language(R.drawable.ic_flag_18_britain, R.string.choose_language_english, false),
                Language(R.drawable.ic_flag_18_rus, R.string.choose_language_russia, false),
                Language(R.drawable.ic_flag_18_china, R.string.choose_language_china, false),
                Language(R.drawable.ic_flag_18_korea, R.string.choose_language_korea, false),
                Language(R.drawable.ic_flag_18_turkey, R.string.choose_language_turkey, false))
    }

}
