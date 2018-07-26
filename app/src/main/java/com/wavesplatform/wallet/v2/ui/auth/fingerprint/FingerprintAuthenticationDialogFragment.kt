package com.wavesplatform.wallet.v2.ui.auth.fingerprint


import android.app.Dialog
import android.app.DialogFragment
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatTextView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager

import com.wavesplatform.wallet.R
import kotlinx.android.synthetic.main.fingerprint_dialog.*
import kotlinx.android.synthetic.main.fingerprint_dialog.view.*
import pers.victor.ext.click


class FingerprintAuthenticationDialogFragment : DialogFragment() {
    private var fingerprintState = FingerprintState.DEFAULT
    private var numberOfAttempts = 0
    private var fingerPrintDialogListener: FingerPrintDialogListener? = null

    init {
        setFingerPrintDialogListener(object : FingerPrintDialogListener{})
    }

    override fun onStart() {
        super.onStart()
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fingerprint_dialog, container, false)
        view.text_enter_passcode.click {
            fingerPrintDialogListener?.onPinCodeButtonClicked(this.dialog, it)
        }
        view.text_cancel.click {
            fingerPrintDialogListener?.onCancelButtonClicked(this.dialog, it)
        }
        return view
    }

    fun onSuccessRecognizedFingerprint() {
        fingerprintState = FingerprintState.SUCCESS

        image_fingerprint_state.setImageResource(R.drawable.ic_fingerprint_recognized_48_submit_300)
        text_fingerprint_state.setTextColor(ContextCompat.getColor(activity, R.color.black))
        text_fingerprint_state.setText(R.string.fingerprint_dialog_recognized)
    }

    fun onDefaultState() {
        fingerprintState = FingerprintState.DEFAULT

        image_fingerprint_state.setImageResource(R.drawable.ic_fingerprint_sensor_48_submit_300);
        text_fingerprint_state.setTextColor(ContextCompat.getColor(activity, R.color.disabled500));
        text_fingerprint_state.setText(R.string.fingerprint_dialog_hint);
    }

    fun onFingerprintLocked() {
        fingerprintState = FingerprintState.LOCKED

        image_fingerprint_state.setImageResource(R.drawable.ic_fingerprint_lock_48_submit_300);
        text_fingerprint_state.setTextColor(ContextCompat.getColor(activity, R.color.error500));
        text_fingerprint_state.setText(R.string.fingerprint_dialog_too_many_attempts);
    }

    fun onFingerprintDoNotMatchTryAgain() {
        fingerprintState = FingerprintState.NOT_RECOGNIZED

        image_fingerprint_state.setImageResource(R.drawable.ic_fingerprint_sensor_48_submit_300);
        text_fingerprint_state.setTextColor(ContextCompat.getColor(activity, R.color.error500));
        text_fingerprint_state.setText(R.string.fingerprint_dialog_not_recognized);

        Handler().postDelayed({
            //            if fingerprint recognized and the handler has not started yet
            if (fingerprintState == FingerprintState.NOT_RECOGNIZED || fingerprintState == FingerprintState.DEFAULT) {
                onDefaultState()
            }
        }, 2000)

        numberOfAttempts++
        if (numberOfAttempts == AVAILABLE_TIMES) {
            onFingerprintLocked()
        }
    }

    fun setFingerPrintDialogListener(fingerPrintDialogListener: FingerPrintDialogListener){
        this.fingerPrintDialogListener = fingerPrintDialogListener
    }

    private enum class FingerprintState {
        LOCKED, NOT_RECOGNIZED, SUCCESS, DEFAULT
    }

    companion object {
        val AVAILABLE_TIMES = 5
    }

    interface FingerPrintDialogListener {
        fun onPinCodeButtonClicked(dialog: Dialog, button: AppCompatTextView) {
            dialog.dismiss()
        }
        fun onCancelButtonClicked(dialog: Dialog, button: AppCompatTextView) {
            dialog.dismiss()
        }
    }
}
