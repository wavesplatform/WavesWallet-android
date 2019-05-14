/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.home.quick_action

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.analytics.AnalyticEvents
import com.wavesplatform.wallet.v2.data.analytics.analytics
import com.wavesplatform.wallet.v2.ui.base.view.BaseBottomSheetDialogFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.ReceiveActivity
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.SendActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.bottom_sheet_dialog_quick_action_layout.view.*
import pers.victor.ext.click

class QuickActionBottomSheetFragment : BaseBottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val rootView = inflater.inflate(R.layout.bottom_sheet_dialog_quick_action_layout, container, false)

        rootView.image_close.click {
            dismiss()
        }

        rootView.relative_send.click {
            analytics.trackEvent(AnalyticEvents.WavesActionSendEvent)
            dismiss()
            launchActivity<SendActivity> { }
        }
        rootView.relative_receive.click {
            analytics.trackEvent(AnalyticEvents.WavesActionReceiveEvent)
            dismiss()
            launchActivity<ReceiveActivity> { }
        }
        rootView.relative_exchange.click {
        }

        return rootView
    }

    companion object {
        fun newInstance(): QuickActionBottomSheetFragment {
            return QuickActionBottomSheetFragment()
        }
    }
}