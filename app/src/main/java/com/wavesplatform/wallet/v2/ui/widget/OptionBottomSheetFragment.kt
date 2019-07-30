package com.wavesplatform.wallet.v2.ui.widget

import android.os.Bundle
import android.support.v7.widget.AppCompatCheckBox
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseBottomSheetDialogFragment
import pers.victor.ext.click

class OptionBottomSheetFragment : BaseBottomSheetDialogFragment() {

    var onChangeListener: OnChangeListener? = null
    private var options = arrayListOf<String>()
    private var title = ""
    private var defaultPosition = 0

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        options = arguments?.getStringArrayList(OPTIONS) ?: arrayListOf()
        title = arguments?.getString(TITLE) ?: ""
        defaultPosition = arguments?.getInt(DEFAULT_POSITION) ?: 0

        val rootView = inflater.inflate(R.layout.bottom_sheet_dialog_options_layout,
                container, false)

        rootView.findViewById<TextView>(R.id.title).text = title
        val optionsContainer = rootView.findViewById<LinearLayout>(R.id.optionsContainer)

        for (index in 0 until options.size) {
            val optionView = inflater.inflate(R.layout.bottom_sheet_dialog_options_item,
                    container, false)
            optionView.findViewById<TextView>(R.id.option_title).text = options[index]
            if (index == defaultPosition) {
                optionView.findViewById<AppCompatCheckBox>(R.id.option_checkbox).isChecked = true
            }
            optionView.click {
                onChangeListener?.onChange(index)
                this.dismiss()
            }
            optionsContainer.addView(optionView)
        }

        return rootView
    }

    interface OnChangeListener {
        fun onChange(optionPosition: Int)
    }

    companion object {

        private const val OPTIONS = "options"
        private const val TITLE = "title"
        private const val DEFAULT_POSITION = "default_position"

        fun newInstance(options: ArrayList<String>,
                        title: String,
                        defaultPosition: Int = 0
        ): OptionBottomSheetFragment {
            val fragment = OptionBottomSheetFragment()
            val args = Bundle()
            args.putStringArrayList(OPTIONS, options)
            args.putString(TITLE, title)
            args.putInt(DEFAULT_POSITION, defaultPosition)
            fragment.arguments = args
            return fragment
        }
    }
}