/*
 * Created by Eduard Zaydel on 2/10/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.base.view

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.BottomSheetDialog

import com.arellomobile.mvp.MvpAppCompatDialogFragment

open class BaseMvpBottomDialogFragment : MvpAppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme)
    }

}