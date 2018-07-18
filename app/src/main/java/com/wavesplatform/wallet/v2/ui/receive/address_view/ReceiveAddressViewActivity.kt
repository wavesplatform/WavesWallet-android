package com.wavesplatform.wallet.v2.ui.receive.address_view

import android.os.Bundle
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity

class ReceiveAddressViewActivity : BaseActivity(), ReceiveAddressView {

    override fun onViewReady(savedInstanceState: Bundle?) {

    }

    override fun configLayoutRes(): Int = R.layout.activity_receive_loading
}
