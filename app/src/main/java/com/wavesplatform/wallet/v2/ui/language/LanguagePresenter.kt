package com.wavesplatform.wallet.v2.ui.language

import com.arellomobile.mvp.InjectViewState
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.local.Language
import com.wavesplatform.wallet.v2.ui.base.presenter.BasePresenter
import javax.inject.Inject

@InjectViewState
class LanguagePresenter @Inject constructor() : BasePresenter<LanguageView>() {
    var currentLanguagePosition: Int = -1

    fun getLanguages(): MutableList<Language>? {
        return arrayListOf(Language(R.drawable.ic_flag_18_britain, R.string.choose_language_english, false),
                Language(R.drawable.ic_flag_18_rus, R.string.choose_language_russia, false),
                Language(R.drawable.ic_flag_18_china, R.string.choose_language_china, false),
                Language(R.drawable.ic_flag_18_korea, R.string.choose_language_korea, false),
                Language(R.drawable.ic_flag_18_turkey, R.string.choose_language_turkey, false),
                Language(R.drawable.ic_flag_18_hindi, R.string.choose_language_hindi, false),
                Language(R.drawable.ic_flag_18_danish, R.string.choose_language_dansk, false),
                Language(R.drawable.ic_flag_18_nederland, R.string.choose_language_nederlands, false))
    }

}
