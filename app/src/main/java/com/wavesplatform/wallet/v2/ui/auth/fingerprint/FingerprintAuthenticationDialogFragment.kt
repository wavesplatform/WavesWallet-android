package com.wavesplatform.wallet.v2.ui.auth.fingerprint


import android.app.Dialog
import android.app.DialogFragment
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.widget.AppCompatTextView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import com.mtramin.rxfingerprint.EncryptionMethod

import com.wavesplatform.wallet.R
import kotlinx.android.synthetic.main.fingerprint_dialog.*
import kotlinx.android.synthetic.main.fingerprint_dialog.view.*
import pers.victor.ext.click
import com.mtramin.rxfingerprint.RxFingerprint
import com.mtramin.rxfingerprint.data.FingerprintResult
import com.wavesplatform.wallet.v1.data.access.AccessState
import com.wavesplatform.wallet.v1.payload.WatchMarket
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom
import com.wavesplatform.wallet.v1.ui.dex.details.chart.ChartFragment
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPasscodeActivity
import io.reactivex.disposables.Disposables


class FingerprintAuthenticationDialogFragment : DialogFragment() {

    companion object {
        const val AVAILABLE_TIMES = 5

        private const val KEY_MODE = "key_mode"
        const val AUTH = 0
        const val DECRYPT = 1

        fun newInstance(mode: Int): FingerprintAuthenticationDialogFragment {
            val args = Bundle()
            args.putInt(KEY_MODE, mode)
            val fragment = FingerprintAuthenticationDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }



    private var fingerprintState = FingerprintState.DEFAULT
    private var numberOfAttempts = 0
    private var fingerPrintDialogListener: FingerPrintDialogListener? = null
    private var fingerprintDisposable = Disposables.empty()
    private var mode = AUTH

    init {
        setFingerPrintDialogListener(object : FingerPrintDialogListener {})
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

        mode = arguments.getInt(KEY_MODE, AUTH)

        if (mode == AUTH) {
            fingerprintDisposable = RxFingerprint.authenticate(activity)
                    .subscribe({ fingerprintAuthenticationResult ->
                        when (fingerprintAuthenticationResult?.result) {
                            FingerprintResult.FAILED -> onFingerprintDoNotMatchTryAgain()
                            FingerprintResult.HELP -> ToastCustom.makeText(
                                    activity,
                                    fingerprintAuthenticationResult.message,
                                    Toast.LENGTH_SHORT,
                                    ToastCustom.TYPE_GENERAL)
                            FingerprintResult.AUTHENTICATED -> {
                                onSuccessRecognizedFingerprint()
                                fingerPrintDialogListener?.onSuccessRecognizedFingerprint()
                            }
                        }
                    }) {
                        Log.e("ERROR", "authenticate", it)
                        ToastCustom.makeText(
                                activity,
                                it.message,
                                Toast.LENGTH_SHORT,
                                ToastCustom.TYPE_GENERAL)
                    }
        } else {
            fingerprintDisposable = RxFingerprint.decrypt(EncryptionMethod.RSA, activity,
                    EnterPasscodeActivity.KEY_PIN_CODE, AccessState.getInstance().ecryptedPin)
                    .subscribe({ decryptionResult ->
                        when (decryptionResult?.result) {
                            FingerprintResult.FAILED -> onFingerprintDoNotMatchTryAgain()
                            FingerprintResult.HELP -> ToastCustom.makeText(
                                    activity,
                                    decryptionResult.message,
                                    Toast.LENGTH_SHORT,
                                    ToastCustom.TYPE_GENERAL)
                            FingerprintResult.AUTHENTICATED -> {
                                onSuccessRecognizedFingerprint()
                                fingerPrintDialogListener?.onSuccessRecognizedFingerprint(
                                        decryptionResult.decrypted)
                            }
                        }
                    }, {
                        if (RxFingerprint.keyInvalidated(it)) {
                            // The keys you wanted to use are invalidated because the user has turned off his
                            // secure lock screen or changed the fingerprints stored on the device
                            // You have to re-encrypt the data to access it
                        }
                        Log.e("ERROR", "authenticate", it)
                        ToastCustom.makeText(
                                activity,
                                it.message,
                                Toast.LENGTH_SHORT,
                                ToastCustom.TYPE_GENERAL)
                    })
        }

        return view
    }

    override fun onPause() {
        super.onPause()
        fingerprintDisposable.dispose()
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

    fun setFingerPrintDialogListener(fingerPrintDialogListener: FingerPrintDialogListener) {
        this.fingerPrintDialogListener = fingerPrintDialogListener
    }

    fun FingerPrintDialogStateChangeListener(fingerPrintDialogListener: FingerPrintDialogListener) {
        this.fingerPrintDialogListener = fingerPrintDialogListener
    }

    private enum class FingerprintState {
        LOCKED, NOT_RECOGNIZED, SUCCESS, DEFAULT
    }

    interface FingerPrintDialogListener {
        fun onPinCodeButtonClicked(dialog: Dialog, button: AppCompatTextView) {
            dialog.dismiss()
        }

        fun onCancelButtonClicked(dialog: Dialog, button: AppCompatTextView) {
            dialog.dismiss()
        }

        fun onSuccessRecognizedFingerprint() {

        }

        fun onSuccessRecognizedFingerprint(decrypted: String) {

        }
    }
}
