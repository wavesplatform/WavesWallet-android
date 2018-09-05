package com.wavesplatform.wallet.v2.ui.auth.fingerprint


import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
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
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom
import com.wavesplatform.wallet.v1.util.RootUtil
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPasscodeActivity
import io.reactivex.disposables.Disposables


class FingerprintAuthDialogFragment : DialogFragment() {

    companion object {
        const val AVAILABLE_TIMES = 5

        private const val KEY_MODE = "key_mode"
        private const val KEY_PASS_CODE = "key_pass_code"
        private const val CRYPT = 0
        private const val DECRYPT = 1

        fun newInstance(passCode: String): FingerprintAuthDialogFragment {
            val args = Bundle()
            args.putInt(KEY_MODE, CRYPT)
            args.putString(KEY_PASS_CODE, passCode)
            val fragment = FingerprintAuthDialogFragment()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(): FingerprintAuthDialogFragment {
            val args = Bundle()
            args.putInt(KEY_MODE, DECRYPT)
            val fragment = FingerprintAuthDialogFragment()
            fragment.arguments = args
            return fragment
        }

        fun isAvailable(context: Context): Boolean {
            return RxFingerprint.isAvailable(context)
        }
    }


    private var fingerprintState = FingerprintState.DEFAULT
    private var numberOfAttempts = 0
    private var fingerPrintDialogListener: FingerPrintDialogListener? = null
    private var authFingerprintDisposable = Disposables.empty()
    private var fingerprintDisposable = Disposables.empty()
    private var mode = CRYPT

    init {
        setFingerPrintDialogListener(object : FingerPrintDialogListener {})
    }

    override fun onStart() {
        super.onStart()
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fingerprint_dialog, container, false)
        view.text_cancel.click {
            fingerPrintDialogListener?.onCancelButtonClicked(this.dialog, it)
        }
        mode = arguments.getInt(KEY_MODE, CRYPT)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onDefaultState()
    }

    private fun createDisposable() {
        if (RootUtil.isDeviceRooted()) {
            val message = getString(R.string.fingerprint_fatal_error_root)
            showErrorMessage(message,
                    "Fingerprint auth Error rooted Device", Throwable(message))
            return
        }

        when (mode) {
            CRYPT -> authFingerprintDisposable = RxFingerprint.authenticate(activity)
                    .subscribe(
                            { result ->
                                when (result?.result) {
                                    FingerprintResult.FAILED -> onFingerprintDoNotMatchTryAgain()
                                    FingerprintResult.HELP -> showHelpMessage(result.message)
                                    FingerprintResult.AUTHENTICATED -> {
                                        crypt()
                                    }
                                }
                            },
                            { showErrorMessage(it.message, "authenticate", it) }
                    )
            DECRYPT -> decrypt()
        }
    }

    private fun dispose() {
        authFingerprintDisposable.dispose()
        fingerprintDisposable.dispose()
    }

    private fun crypt() {
        val passCode = arguments.getString(KEY_PASS_CODE, "")
        fingerprintDisposable = RxFingerprint.encrypt(
                EncryptionMethod.RSA,
                activity,
                EnterPasscodeActivity.KEY_INTENT_PASS_CODE, passCode)
                .subscribe(
                        { encryptionResult ->
                            when (encryptionResult?.result) {
                                FingerprintResult.FAILED -> onFingerprintDoNotMatchTryAgain()
                                FingerprintResult.HELP -> showHelpMessage(encryptionResult.message)
                                FingerprintResult.AUTHENTICATED -> {
                                    AccessState.getInstance().encryptedPin =
                                            encryptionResult.encrypted
                                    onSuccessRecognizedFingerprint()
                                    fingerPrintDialogListener?.onSuccessRecognizedFingerprint()
                                    dismiss()
                                }
                            }
                        },
                        { showErrorMessage(it.message, "crypt", it) })
    }

    private fun decrypt() {
        fingerprintDisposable = RxFingerprint.decrypt(EncryptionMethod.RSA, activity,
                EnterPasscodeActivity.KEY_INTENT_PASS_CODE, AccessState.getInstance().encryptedPin)
                .subscribe(
                        { result ->
                            when (result?.result) {
                                FingerprintResult.FAILED -> onFingerprintDoNotMatchTryAgain()
                                FingerprintResult.HELP -> showHelpMessage(result.message)
                                FingerprintResult.AUTHENTICATED -> {
                                    onSuccessRecognizedFingerprint()
                                    fingerPrintDialogListener?.onSuccessRecognizedFingerprint(
                                            result.decrypted)
                                    dismiss()
                                }
                            }
                        },
                        {
                            if (RxFingerprint.keyInvalidated(it)) {
                                showErrorMessage(activity.getString(
                                        R.string.fingerprint_fatal_error_key_invalidate),
                                        "decypt", it)
                            } else {
                                showErrorMessage(it.message, "decypt", it)
                            }
                        })
    }

    private fun showHelpMessage(message: String?) {
        ToastCustom.makeText(
                activity,
                message,
                Toast.LENGTH_LONG,
                ToastCustom.TYPE_GENERAL)
    }

    private fun showErrorMessage(message: String?, errMessage: String?, tr: Throwable) {
        Log.e("ERROR", errMessage, tr)
        ToastCustom.makeText(
                activity,
                message,
                Toast.LENGTH_LONG,
                ToastCustom.TYPE_ERROR)
    }

    override fun onStop() {
        super.onStop()
        dispose()
    }

    fun onSuccessRecognizedFingerprint() {
        fingerprintState = FingerprintState.SUCCESS

        image_fingerprint_state.setImageResource(R.drawable.ic_fingerprint_recognized_48_submit_300)
        text_fingerprint_state.setTextColor(ContextCompat.getColor(activity, R.color.black))
        text_fingerprint_state.setText(R.string.fingerprint_dialog_recognized)
    }

    private fun onDefaultState() {
        fingerprintState = FingerprintState.DEFAULT

        image_fingerprint_state.setImageResource(R.drawable.ic_fingerprint_sensor_48_submit_300);
        text_fingerprint_state.setTextColor(ContextCompat.getColor(activity, R.color.disabled500));
        text_fingerprint_state.setText(R.string.fingerprint_dialog_hint);

        createDisposable()
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

        dispose()

        Handler().postDelayed({
            if (fingerprintState == FingerprintState.NOT_RECOGNIZED
                    || fingerprintState == FingerprintState.DEFAULT) {
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

    private enum class FingerprintState {
        LOCKED, NOT_RECOGNIZED, SUCCESS, DEFAULT
    }

    interface FingerPrintDialogListener {
        fun onCancelButtonClicked(dialog: Dialog, button: AppCompatTextView) {
            dialog.dismiss()
        }

        fun onSuccessRecognizedFingerprint() {

        }

        fun onSuccessRecognizedFingerprint(passCode: String) {

        }
    }
}
