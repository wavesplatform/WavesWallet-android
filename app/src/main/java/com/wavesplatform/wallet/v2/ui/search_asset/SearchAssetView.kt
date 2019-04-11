/*
 * Created by Ershov Aleksandr on 9/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.search_asset

import com.chad.library.adapter.base.entity.MultiItemEntity
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseMvpView

interface SearchAssetView : BaseMvpView {
    fun setSearchResult(list: List<MultiItemEntity>)
}
