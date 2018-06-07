package com.wavesplatform.wallet.v2.ui.home.history.adapter

import com.chad.library.adapter.base.entity.SectionEntity
import com.wavesplatform.wallet.v2.ui.home.history.TestObject

class HistoryItem : SectionEntity<TestObject> {
    private val isMore: Boolean = false

    constructor(isHeader: Boolean, header: String) : super(isHeader, header) {}

    constructor(t: TestObject) : super(t) {}
}