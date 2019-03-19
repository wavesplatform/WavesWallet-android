package com.wavesplatform.wallet.v2.ui.custom

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import kotlinx.android.synthetic.main.pass_code_entry_keyboard_layout.view.*
import pers.victor.ext.invisiable
import pers.victor.ext.vibrator
import pers.victor.ext.visiable
import pers.victor.ext.visiableIf
import pyxis.uzuki.live.richutilskt.utils.runDelayed

class PassCodeEntryKeypad : LinearLayout, View.OnClickListener {

    private var listener: OnPinEntryPadClickedListener? = null
    private var dots: PinDotsLayout? = null
    private var passCode = ""
    private var isFingerprintAvailable: Boolean = false

    private var allButtons = arrayListOf<View>()

    constructor(context: Context) : this(context, null) {
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0) {
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val attr = context.obtainStyledAttributes(attrs, R.styleable.PassCodeEntryKeypad, defStyleAttr, 0)
        isFingerprintAvailable = attr.getBoolean(R.styleable.PassCodeEntryKeypad_is_fingerprint_available, false)
        attr.recycle()

        init()
    }

    private fun init() {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.BOTTOM

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.pass_code_entry_keyboard_layout, this, true)

        button_fingerprint.visiableIf { isFingerprintAvailable }

        button0.setOnClickListener(this)
        button1.setOnClickListener(this)
        button2.setOnClickListener(this)
        button3.setOnClickListener(this)
        button4.setOnClickListener(this)
        button5.setOnClickListener(this)
        button6.setOnClickListener(this)
        button7.setOnClickListener(this)
        button8.setOnClickListener(this)
        button9.setOnClickListener(this)
        button_fingerprint.setOnClickListener(this)
        button_delete.setOnClickListener(this)

        allButtons = arrayListOf(button0, button1, button2, button3, button4, button5,
                button6, button7, button8, button9, button_fingerprint, button_delete)
    }

    fun attachDots(dotsLayout: PinDotsLayout) {
        dots = dotsLayout
    }

    fun setPadClickedListener(listener: OnPinEntryPadClickedListener) {
        this.listener = listener
    }

    private fun padClicked(view: View) {
        val number = view.tag.toString().substring(0, 1)
        listener?.onNumberClicked(number)
        if (passCode.length != 4) {
            if (passCode.length == 3) {
                passCode += number
                dots?.fillDot(passCode.length - 1)

                listener?.onPassCodeEntered(passCode)
            } else {
                passCode += number
                dots?.fillDot(passCode.length - 1)
            }
        }
    }

    fun passCodesNotMatches() {
        dots?.passCodesNotMatches()

        val animation = AnimationUtils.loadAnimation(context, R.anim.shake_error)
        dots?.startAnimation(animation)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(500)
        }
        runDelayed(500) {
            dots?.clearDots()
            passCode = ""
        }
    }

    fun clearPassCode() {
        dots?.clearDots()
        passCode = ""
    }

    private fun deleteClicked() {
        listener?.onDeleteClicked()
        if (passCode.isNotEmpty()) {
            passCode = passCode.substring(0, passCode.length - 1)
            dots?.passCodeRemoved(passCode.length)
        }
    }

    private fun fingerprintClicked() {
        listener?.onFingerprintClicked()
    }

    fun isFingerprintAvailable(enable: Boolean) {
        isFingerprintAvailable = enable
        if (isFingerprintAvailable) {
            button_fingerprint.visiable()
        } else {
            button_fingerprint.invisiable()
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.button0, R.id.button1, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9 -> padClicked(v)
            R.id.button_delete -> deleteClicked()
            R.id.button_fingerprint -> fingerprintClicked()
            else -> {
            }
        }
    }

    fun setEnable(isEnable: Boolean) {
        if (isEnable) {
            allButtons.forEach { button ->
                button.isClickable = true
                button.alpha = Constants.ENABLE_VIEW
            }
        } else {
            allButtons.forEach { button ->
                button.isClickable = false
                button.alpha = Constants.DISABLE_VIEW
            }
        }
    }

    interface OnPinEntryPadClickedListener {

        fun onNumberClicked(number: String) {}

        fun onPassCodeEntered(passCode: String) {}

        fun onDeleteClicked() {}

        fun onFingerprintClicked() {}
    }
}