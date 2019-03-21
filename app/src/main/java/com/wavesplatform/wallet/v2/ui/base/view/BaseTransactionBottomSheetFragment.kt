package com.wavesplatform.wallet.v2.ui.base.view

import android.content.ClipData
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v7.widget.AppCompatImageView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.novoda.simplechromecustomtabs.SimpleChromeCustomTabs
import com.wavesplatform.sdk.utils.notNull
import com.wavesplatform.wallet.R
import pers.victor.ext.clipboardManager
import pers.victor.ext.dp2px
import pers.victor.ext.findColor
import pyxis.uzuki.live.richutilskt.utils.runDelayed

abstract class BaseTransactionBottomSheetFragment<T> : BaseSuperBottomSheetDialogFragment() {
    var selectedItem: T? = null
    var selectedItemPosition: Int = 0
    var rootView: View? = null
    var inflater: LayoutInflater? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        this.inflater = inflater
        rootView = inflater.inflate(configLayoutRes(), container, false)

        configureView()

        return rootView
    }

    protected fun configureView() {
        val container = rootView?.findViewById<LinearLayout>(R.id.main_container)
        container?.removeAllViews()

        selectedItem?.let {
            container?.apply {
                addView(setupHeader(it))
                addView(setupBody(it))
                addView(setupInfo(it))
                addView(setupFooter(it))
            }
            configCloseButton()
        }
    }

    private fun configCloseButton() {
        val close = rootView?.findViewById<AppCompatImageView>(R.id.image_close)
        close?.post {
            val closeOriginalPos = IntArray(2)
            close.getLocationOnScreen(closeOriginalPos)

            val dialogHeight = dialog.findViewById<CoordinatorLayout>(R.id.coordinator).height
            val imageCloseBottomY = (closeOriginalPos[1] + close.height)
            val difference = dialogHeight - imageCloseBottomY

            if (imageCloseBottomY < dialogHeight && difference > 0) {
                val lp = close.layoutParams as RelativeLayout.LayoutParams
                lp.setMargins(0, dp2px(34) + difference, dp2px(24), 0)
                close.layoutParams = lp
            }
        }
    }

    protected fun copyToClipboard(textToCopy: String, view: TextView, btnText: Int) {
        clipboardManager.primaryClip = ClipData.newPlainText(getString(R.string.app_name), textToCopy)
        view.text = getString(R.string.common_copied)
        view.setTextColor(findColor(R.color.success400))
        view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_18_success_400, 0, 0, 0)
        runDelayed(1500) {
            this.context.notNull {
                view.text = getString(btnText)
                view.setTextColor(findColor(R.color.black))
                view.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_copy_18_black, 0, 0, 0)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        SimpleChromeCustomTabs.getInstance().connectTo(requireActivity())
    }

    override fun onPause() {
        SimpleChromeCustomTabs.getInstance().disconnectFrom(requireActivity())
        super.onPause()
    }

    fun configureData(data: T, selectedPosition: Int) {
        this.selectedItem = data
        this.selectedItemPosition = selectedPosition
    }


    abstract fun configLayoutRes(): Int

    abstract fun setupHeader(data: T): View?

    abstract fun setupBody(data: T): View?

    abstract fun setupInfo(data: T): View?

    abstract fun setupFooter(data: T): View?
}