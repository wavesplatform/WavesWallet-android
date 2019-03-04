package com.wavesplatform.wallet.v2.ui.auth.fingerprint

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.util.RootUtil
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPassCodeActivity
import com.wavesplatform.wallet.v2.util.notNull
import com.wei.android.lib.fingerprintidentify.FingerprintIdentify
import com.wei.android.lib.fingerprintidentify.base.BaseFingerprint
import de.adorsys.android.securestoragelibrary.SecurePreferences
import de.adorsys.android.securestoragelibrary.SecureStorageException
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fingerprint_dialog.*
import kotlinx.android.synthetic.main.fingerprint_dialog.view.*
import pers.victor.ext.click
import pers.victor.ext.findColor
import timber.log.Timber

class FingerprintAuthDialogFragment : DialogFragment() {

    private var fingerprintState = FingerprintState.DEFAULT
    private var fingerPrintDialogListener: FingerPrintDialogListener? = null
    private var subscriptions = CompositeDisposable()
    private var mode: Int? = ENCRYPT
    private var handler = Handler()
    private var guid: String = ""
    private lateinit var fingerprintIdentify: FingerprintIdentify

    init {
        setFingerPrintDialogListener(object : FingerPrintDialogListener {})
    }

    override fun onStart() {
        super.onStart()
        dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fingerprint_dialog, container, false)
        view.text_cancel.click { cancelDialog() }
        mode = arguments?.getInt(KEY_INTENT_MODE, ENCRYPT)
        return view
    }

    private fun cancelDialog() {
        fingerPrintDialogListener?.onCancelButtonClicked(this.dialog)
        fingerprintIdentify.cancelIdentify()
    }

    override fun onResume() {
        super.onResume()
        dialog.setOnKeyListener(DialogInterface.OnKeyListener { _, keyCode, _ ->
            if ((keyCode == android.view.KeyEvent.KEYCODE_BACK)) {
                cancelDialog()
                return@OnKeyListener true
            }
            return@OnKeyListener false
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startIdentify()
    }

    private fun startIdentify() {
        onDefaultState()
        fingerprintIdentify = FingerprintIdentify(context)
        fingerprintIdentify.startIdentify(AVAILABLE_TIMES, object : BaseFingerprint.FingerprintIdentifyListener {

            override fun onSucceed() {
                when (mode) {
                    ENCRYPT -> encrypt()
                    DECRYPT -> decrypt()
                    else -> showHelpMessage(null)
                }
            }

            override fun onFailed(isDeviceLocked: Boolean) {
                App.getAccessManager().setUseFingerPrint(guid, false)
                onFingerprintLocked()
            }

            override fun onNotMatch(availableTimes: Int) {
                fingerprintIdentify.cancelIdentify()
                onFingerprintDoNotMatchTryAgain()
                handler.postDelayed({
                    if (fingerprintState == FingerprintState.NOT_RECOGNIZED ||
                            fingerprintState == FingerprintState.DEFAULT) {
                        onDefaultState()
                        fingerprintIdentify.resumeIdentify()
                    }
                }, DELAY_TO_CHANGE_STATE)
            }

            override fun onStartFailedByDeviceLocked() {
                fingerprintIdentify.cancelIdentify()
                onFingerprintLocked()
            }
        })
    }

    private fun encrypt() {
        guid = arguments?.getString(KEY_INTENT_GUID, "") ?: ""
        val passCode = arguments?.getString(KEY_INTENT_PASS_CODE, "") ?: ""
        try {
            SecurePreferences.setValue(guid + EnterPassCodeActivity.KEY_INTENT_PASS_CODE, passCode)
            fingerprintIdentify.cancelIdentify()
            onSuccessRecognizedFingerprint()
            handler.postDelayed({
                fingerPrintDialogListener?.onSuccessRecognizedFingerprint()
                dismissAllowingStateLoss()
            }, DELAY_TO_CHANGE_STATE)
        } catch (throwable: SecureStorageException) {
            onErrorGetKey(throwable, "crypt")
        }
    }

    private fun decrypt() {
        guid = arguments?.getString(KEY_INTENT_GUID, "") ?: ""
        try {
            val decryptedMessage = SecurePreferences.getStringValue(
                    guid + EnterPassCodeActivity.KEY_INTENT_PASS_CODE, "")!!
            fingerprintIdentify.cancelIdentify()
            onSuccessRecognizedFingerprint()
            handler.postDelayed({
                fingerPrintDialogListener?.onSuccessRecognizedFingerprint(decryptedMessage)
                dismissAllowingStateLoss()
            }, DELAY_TO_CHANGE_STATE)
        } catch (throwable: SecureStorageException) {
            onErrorGetKey(throwable, "decrypt")
        }
    }

    private fun onErrorGetKey(throwable: SecureStorageException, errMessage: String) {
        App.getAccessManager().setUseFingerPrint(guid, false)
        showErrorMessage(throwable.message, errMessage, throwable)
    }

    private fun showHelpMessage(message: String?) {
        fingerPrintDialogListener.notNull {
            it.onShowErrorMessage(message ?: getString(R.string.common_server_error))
        }
    }

    private fun showErrorMessage(message: String?, errMessage: String?, tr: Throwable) {
        Timber.e(tr, errMessage)
        fingerPrintDialogListener.notNull {
            it.onShowErrorMessage(message ?: getString(R.string.common_server_error))
        }
    }

    override fun onDestroyView() {
        subscriptions.clear()
        handler.removeCallbacksAndMessages(null)
        super.onDestroyView()
    }

    private fun onSuccessRecognizedFingerprint() {
        fingerprintState = FingerprintState.SUCCESS

        image_fingerprint_state?.setImageResource(R.drawable.ic_fingerprint_recognized_48_submit_300)
        text_fingerprint_state?.setTextColor(findColor(R.color.black))
        text_fingerprint_state?.setText(R.string.fingerprint_dialog_recognized)
    }

    private fun onDefaultState() {
        fingerprintState = FingerprintState.DEFAULT

        image_fingerprint_state?.setImageResource(R.drawable.ic_fingerprint_sensor_48_submit_300)
        text_fingerprint_state?.setTextColor(findColor(R.color.disabled500))
        text_fingerprint_state?.setText(R.string.fingerprint_dialog_hint)
    }

    private fun onFingerprintLocked() {
        fingerprintState = FingerprintState.LOCKED

        image_fingerprint_state?.setImageResource(R.drawable.ic_fingerprint_lock_48_submit_300)
        text_fingerprint_state?.setTextColor(findColor(R.color.error500))
        text_fingerprint_state?.setText(R.string.fingerprint_dialog_too_many_attempts)
        fingerPrintDialogListener.notNull {
            it.onFingerprintLocked(getString(R.string.fingerprint_dialog_too_many_attempts))
        }
    }

    private fun onFingerprintDoNotMatchTryAgain() {
        fingerprintState = FingerprintState.NOT_RECOGNIZED

        image_fingerprint_state?.setImageResource(R.drawable.ic_fingerprint_sensor_48_submit_300)
        text_fingerprint_state?.setTextColor(findColor(R.color.error500))
        text_fingerprint_state?.setText(R.string.fingerprint_dialog_not_recognized)
    }

    fun setFingerPrintDialogListener(fingerPrintDialogListener: FingerPrintDialogListener) {
        this.fingerPrintDialogListener = fingerPrintDialogListener
    }

    private enum class FingerprintState {
        LOCKED, NOT_RECOGNIZED, SUCCESS, DEFAULT
    }

    interface FingerPrintDialogListener {
        fun onCancelButtonClicked(dialog: Dialog) {
            dialog.dismiss()
        }

        fun onSuccessRecognizedFingerprint() {
        }

        fun onSuccessRecognizedFingerprint(passCode: String) {
        }

        fun onFingerprintLocked(message: String) {
        }

        fun onShowErrorMessage(message: String) {
        }

        fun onShowMessage(message: String) {
        }
    }

    companion object {
        const val AVAILABLE_TIMES = 5

        private const val DELAY_TO_CHANGE_STATE = 1500L
        private const val KEY_INTENT_MODE = "intent_mode"
        private const val KEY_INTENT_PASS_CODE = "intent_pass_code"
        private const val KEY_INTENT_GUID = "intent_guid"
        private const val ENCRYPT = 0
        private const val DECRYPT = 1

        fun newInstance(guid: String, passCode: String): FingerprintAuthDialogFragment {
            val args = Bundle()
            args.putInt(KEY_INTENT_MODE, ENCRYPT)
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
            val mFingerprintIdentify = FingerprintIdentify(context)
            return !RootUtil.isDeviceRooted() && mFingerprintIdentify.isFingerprintEnable
        }
    }
}