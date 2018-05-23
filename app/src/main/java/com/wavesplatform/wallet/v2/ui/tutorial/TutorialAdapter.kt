package com.wavesplatform.wallet.v2.ui.tutorial

import android.content.Context
import android.support.v7.widget.AppCompatButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.local.PreferencesHelper
import com.wavesplatform.wallet.v2.injection.qualifier.ApplicationContext
import kotlinx.android.synthetic.main.item_tutorial_1_card.view.*
import pers.victor.ext.click
import javax.inject.Inject


class TutorialAdapter @Inject constructor(@ApplicationContext context: Context, var preferencesHelper: PreferencesHelper) : ArrayAdapter<Int>(context, 0) {
    lateinit var listener: OnNextButtonClicked

    override fun getView(position: Int, contentView: View?, parent: ViewGroup): View {
        // fix library bug with cached views
        parent.removeAllViews()

        val inflater = LayoutInflater.from(context)
        val view = when (position) {
            0 -> inflater.inflate(R.layout.item_tutorial_1_card, null, false)
            1 -> inflater.inflate(R.layout.item_tutorial_2_card, null, false)
            2 -> inflater.inflate(R.layout.item_tutorial_3_card, null, false)
            3 -> inflater.inflate(R.layout.item_tutorial_4_card, null, false)
            4 -> inflater.inflate(R.layout.item_tutorial_5_card, null, false)
            5 -> inflater.inflate(R.layout.item_tutorial_6_card, null, false)
            6 -> inflater.inflate(R.layout.item_tutorial_7_card, null, false)
            else -> inflater.inflate(R.layout.item_tutorial_1_card, null, false)
        }

        view.button_continue.click {
            listener.onButtonClicked(view.button_continue)
        }
        view.button_continue.isClickable = false

        return view
    }

    interface OnNextButtonClicked {
        fun onButtonClicked(button: AppCompatButton)
    }

}
