package com.wavesplatform.wallet.v2.ui.home

import com.wavesplatform.wallet.v2.data.model.remote.response.News
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface MainView : BaseMvpView {
    fun showNews(news: News)
}
