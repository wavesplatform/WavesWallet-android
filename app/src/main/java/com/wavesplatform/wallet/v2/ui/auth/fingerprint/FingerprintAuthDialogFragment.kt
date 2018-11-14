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
import com.mtramin.rxfingerprint.RxFingerprint
import com.mtramin.rxfingerprint.data.FingerprintResult
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.ui.customviews.ToastCustom
import com.wavesplatform.wallet.v1.util.RootUtil
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPassCodeActivity
import io.reactivex.disposables.Disposables
import kotlinx.android.synthetic.main.fingerprint_dialog.*
import kotlinx.android.synthetic.main.fingerprint_dialog.view.*
import pers.victor.ext.click


class FingerprintAuthDialogFragment : DialogFragment() {

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
        mode = arguments.getInt(KEY_INTENT_MODE, CRYPT)
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
        val guid = arguments.getString(KEY_INTENT_GUID, "")
        val passCode = arguments.getString(KEY_INTENT_PASS_CODE, "")
        fingerprintDisposable = RxFingerprint.encrypt(
                EncryptionMethod.RSA,
                activity,
                guid + EnterPassCodeActivity.KEY_INTENT_PASS_CODE,
                passCode)
                .subscribe(
                        { encryptionResult ->
                            when (encryptionResult?.result) {
                                FingerprintResult.FAILED -> onFingerprintDoNotMatchTryAgain()
                                FingerprintResult.HELP -> showHelpMessage(encryptionResult.message)
                                FingerprintResult.AUTHENTICATED -> {
                                    App.getAccessManager()
                                            .setEncryptedPassCode(guid, encryptionResult.encrypted)
                                    onSuccessRecognizedFingerprint()
                                    fingerPrintDialogListener?.onSuccessRecognizedFingerprint()
                                    dismiss()
                                }
                            }
                        },
                        { showErrorMessage(it.message, "crypt", it) })
    }

    private fun decrypt() {
        val guid = arguments.getString(KEY_INTENT_GUID, "")
        fingerprintDisposable = RxFingerprint.decrypt(EncryptionMethod.RSA, activity,
                guid + EnterPassCodeActivity.KEY_INTENT_PASS_CODE,
                App.getAccessManager().getEncryptedPassCode(guid))
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

    private fun onSuccessRecognizedFingerprint() {
        fingerprintState = FingerprintState.SUCCESS

        image_fingerprint_state.setImageResource(R.drawable.ic_fingerprint_recognized_48_submit_300)
        text_fingerprint_state.setTextColor(ContextCompat.getColor(activity, R.color.black))
        text_fingerprint_state.setText(R.string.fingerprint_dialog_recognized)
    }

    private fun onDefaultState() {
        fingerprintState = FingerprintState.DEFAULT

        image_fingerprint_state.setImageResource(R.drawable.ic_fingerprint_sensor_48_submit_300)
        text_fingerprint_state.setTextColor(ContextCompat.getColor(activity, R.color.disabled500))
        text_fingerprint_state.setText(R.string.fingerprint_dialog_hint)

        createDisposable()
    }

    private fun onFingerprintLocked() {
        fingerprintState = FingerprintState.LOCKED

        image_fingerprint_state.setImageResource(R.drawable.ic_fingerprint_lock_48_submit_300)
        text_fingerprint_state.setTextColor(ContextCompat.getColor(activity, R.color.error500))
        text_fingerprint_state.setText(R.string.fingerprint_dialog_too_many_attempts)
    }

    private fun onFingerprintDoNotMatchTryAgain() {
        fingerprintState = FingerprintState.NOT_RECOGNIZED

        image_fingerprint_state.setImageResource(R.drawable.ic_fingerprint_sensor_48_submit_300)
        text_fingerprint_state.setTextColor(ContextCompat.getColor(activity, R.color.error500))
        text_fingerprint_state.setText(R.string.fingerprint_dialog_not_recognized)

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

    companion object {
        const val AVAILABLE_TIMES = 5

        private const val KEY_INTENT_MODE = "intent_mode"
        private const val KEY_INTENT_PASS_CODE = "intent_pass_code"
        private const val KEY_INTENT_GUID = "intent_guid"
        private const val CRYPT = 0
        private const val DECRYPT = 1

        fun newInstance(guid: String, passCode: String): FingerprintAuthDialogFragment {
            val args = Bundle()
            args.putInt(KEY_INTENT_MODE, CRYPT)
            args.putString(KEY_INTENT_PASS_CODE, passCode)
            args.putString(KEY_INTENT_GUID, guid)
            val fragment = FingerprintAuthDialogFragment()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(guid: String): FingerprintAuthDialogFragment {
            val args = Bundle()
            args.putInt(KEY_INTENT_MODE, DECRYPT)
            args.putString(KEY_INTENT_GUID, guid)
            val fragment = FingerprintAuthDialogFragment()
            fragment.arguments = args
            return fragment
        }

        fun isAvailable(context: Context): Boolean {
            return !RootUtil.isDeviceRooted() && RxFingerprint.isAvailable(context)
        }
    }
}
