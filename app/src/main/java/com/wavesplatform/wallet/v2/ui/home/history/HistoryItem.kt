package com.wavesplatform.wallet.v2.ui.home.history

import com.chad.library.adapter.base.entity.SectionEntity
import com.wavesplatform.wallet.v2.data.model.remote.response.Transaction
import com.wavesplatform.wallet.v2.ui.home.history.TestObject

class HistoryItem : SectionEntity<Transaction> {
    private val isMore: Boolean = false

    constructor(isHeader: Boolean, header: String) : super(isHeader, header) {}

    constructor(t: Transaction) : super(t) {}
}