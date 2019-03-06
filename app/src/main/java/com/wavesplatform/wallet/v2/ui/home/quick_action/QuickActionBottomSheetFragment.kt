package com.wavesplatform.wallet.v2.ui.home.quick_action

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseBottomSheetDialogFragment
import com.wavesplatform.wallet.v2.ui.home.quick_action.receive.ReceiveActivity
import com.wavesplatform.wallet.v2.ui.home.quick_action.send.SendActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.quick_action_bottom_sheet_dialog_layout.view.*
import pers.victor.ext.click

class QuickActionBottomSheetFragment : BaseBottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val rootView = inflater.inflate(R.layout.quick_action_bottom_sheet_dialog_layout, container, false)

        rootView.image_close.click {
            dismiss()
        }

        rootView.relative_send.click {
            dismiss()
            launchActivity<SendActivity> { }
        }
        rootView.relative_receive.click {
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