package com.wavesplatform.wallet.v2.ui.home.profile

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatTextView
import android.view.*
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.BuildConfig
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.NodeDataManager
import com.wavesplatform.wallet.v2.data.model.local.Language
import com.wavesplatform.wallet.v2.ui.auth.fingerprint.FingerprintAuthDialogFragment
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.create.CreatePassCodeActivity
import com.wavesplatform.wallet.v2.ui.auth.passcode.enter.EnterPassCodeActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity
import com.wavesplatform.wallet.v2.ui.home.profile.addresses.AddressesAndKeysActivity
import com.wavesplatform.wallet.v2.ui.home.profile.backup.BackupPhraseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.change_password.ChangePasswordActivity
import com.wavesplatform.wallet.v2.ui.home.profile.network.NetworkActivity
import com.wavesplatform.wallet.v2.ui.language.change_welcome.ChangeLanguageActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.makeStyled
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_profile.*
import pers.victor.ext.click
import pers.victor.ext.toast
import javax.inject.Inject

class ProfileFragment : BaseFragment(), ProfileView {

    @Inject
    @InjectPresenter
    lateinit var presenter: ProfilePresenter
    @Inject
    lateinit var nodeDataManager: NodeDataManager
    var subscriptions: CompositeDisposable = CompositeDisposable()


    @ProvidePresenter
    fun providePresenter(): ProfilePresenter = presenter

    override fun configLayoutRes(): Int = R.layout.fragment_profile

    override fun onViewReady(savedInstanceState: Bundle?) {
        card_address_book.click {
            launchActivity<AddressBookActivity> { }
        }
        card_addresses_and_keys.click {
            launchActivity<AddressesAndKeysActivity> { }
        }
        card_backup_phrase.click {
            launchActivity<BackupPhraseActivity> {
                putExtra(KEY_INTENT_SET_BACKUP, true)
            }
        }
        card_language.click {
            launchActivity<ChangeLanguageActivity> { }
        }
        card_change_password.click {
            launchActivity<ChangePasswordActivity> { }
        }
        card_change_passcode.click {
            launchActivity<EnterPassCodeActivity>(
                    requestCode = REQUEST_ENTER_PASS_CODE_FOR_CHANGE)
        }
        card_network.click {
            launchActivity<NetworkActivity> { }
        }
        button_delete_account.click {
            val alertDialog = AlertDialog.Builder(baseActivity).create()
            alertDialog.setTitle(getString(R.string.profile_general_delete_account_dialog_title))
            alertDialog.setMessage(getString(R.string.profile_general_delete_account_dialog_description))
            if (App.getAccessManager().isCurrentAccountBackupSkipped()) {
                alertDialog.setView(LayoutInflater.from(baseActivity)
                        .inflate(R.layout.delete_account_warning_layout, null))
            }
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,
                    getString(R.string.profile_general_delete_account_dialog_delete)) { dialog, _ ->
                App.getAccessManager().deleteCurrentWavesWallet()
                presenter.prefsUtil.logOut()
                presenter.appUtil.restartApp()
                dialog.dismiss()
            }
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.profile_general_delete_account_dialog_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            alertDialog.show()
            alertDialog.makeStyled()
        }

        button_logout.click {
            logout()
        }

        initFingerPrintControl()


        if (App.getAccessManager().isCurrentAccountBackupSkipped()) {
            skip_backup_indicator.setBackgroundColor(ContextCompat
                    .getColor(context!!, R.color.error500))
            skip_backup_indicator_image.setImageDrawable(ContextCompat
                    .getDrawable(context!!, R.drawable.ic_info_error_500))
        } else {
            skip_backup_indicator.setBackgroundColor(ContextCompat
                    .getColor(context!!, R.color.success400))
            skip_backup_indicator_image.setImageDrawable(ContextCompat
                    .getDrawable(context!!, R.drawable.ic_check_18_success_400))
        }

        textView_version.text = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})"
        subscriptions.add(nodeDataManager.currentBlocksHeight()
                .subscribe { textView_height.text = it.height.toString() })
    }

    private fun initFingerPrintControl() {
        if (FingerprintAuthDialogFragment.isAvailable(context!!)) {
            fingerprint_switch.setOnCheckedChangeListener(null)
            fingerprint_switch.isChecked = App.getAccessManager().isUseFingerPrint()
            fingerprint_switch.setOnCheckedChangeListener { _, _ ->
                launchActivity<EnterPassCodeActivity>(
                        requestCode = REQUEST_ENTER_PASS_CODE_FOR_FINGERPRINT) {
                    putExtra(EnterPassCodeActivity.KEY_INTENT_PROCESS_SET_FINGERPRINT, true)
                }
            }
        } else {
            card_fingerprint.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        setCurrentLangFlag()
    }

    override fun onPause() {
        super.onPause()
        subscriptions.clear()
    }

    private fun setCurrentLangFlag() {
        val languageItemByCode = Language.getLanguageItemByCode(presenter.preferenceHelper.getLanguage())
        image_language_flag.setImageResource(languageItemByCode.language.image)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_profile, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_logout -> {
                logout()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logout() {
        App.getAccessManager().setLastLoggedInGuid("")
        activity?.finish()
        presenter.appUtil.restartApp()
        toast(getString(R.string.profile_general_logout))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

            REQUEST_ENTER_PASS_CODE_FOR_FINGERPRINT -> {
                if (resultCode == Constants.RESULT_OK) {
                    setFingerprint(App.getAccessManager().getLoggedInGuid(),
                            data!!.extras.getString(EnterPassCodeActivity.KEY_INTENT_PASS_CODE))
                } else {
                    initFingerPrintControl()
                }
            }

            REQUEST_ENTER_PASS_CODE_FOR_CHANGE -> {
                if (resultCode == Constants.RESULT_OK) {
                    val passCode = data!!.extras.getString(EnterPassCodeActivity.KEY_INTENT_PASS_CODE)
                    val password = data.extras.getString(NewAccountActivity.KEY_INTENT_PASSWORD)
                    launchActivity<CreatePassCodeActivity> {
                        putExtra(CreatePassCodeActivity.KEY_INTENT_PROCESS_CHANGE_PASS_CODE, true)
                        putExtra(EnterPassCodeActivity.KEY_INTENT_GUID,
                                App.getAccessManager().getLoggedInGuid())
                        putExtra(NewAccountActivity.KEY_INTENT_PASSWORD, password)
                        putExtra(EnterPassCodeActivity.KEY_INTENT_PASS_CODE, passCode)
                    }
                }
            }
        }
    }

    private fun setFingerprint(guid: String, passCode: String) {
        if (App.getAccessManager().isUseFingerPrint()) {
            App.getAccessManager().setUseFingerPrint(false)
        } else {
            val fingerprintDialog = FingerprintAuthDialogFragment.newInstance(guid, passCode)
            fingerprintDialog.isCancelable = false
            fingerprintDialog.show(activity!!.fragmentManager, "fingerprintDialog")
            fingerprintDialog.setFingerPrintDialogListener(
                    object : FingerprintAuthDialogFragment.FingerPrintDialogListener {
                        override fun onSuccessRecognizedFingerprint() {
                            App.getAccessManager().setUseFingerPrint(
                                    !App.getAccessManager().isUseFingerPrint())
                            initFingerPrintControl()
                        }

                        override fun onCancelButtonClicked(dialog: Dialog, button: AppCompatTextView) {
                            dialog.dismiss()
                            initFingerPrintControl()
                        }
                    })
        }
    }

    companion object {

        /**
         * @return ProfileFragment instance
         * */
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }

        const val KEY_INTENT_SET_BACKUP = "intent_set_backup"
        const val REQUEST_ENTER_PASS_CODE_FOR_CHANGE = 5551
        const val REQUEST_ENTER_PASS_CODE_FOR_FINGERPRINT = 5552
    }
}
