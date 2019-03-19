package com.wavesplatform.wallet.v2.ui.auth.import_account.manually

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.data.auth.WavesWallet
import com.wavesplatform.wallet.v2.data.rules.NotEmptyTrimRule
import com.wavesplatform.wallet.v2.data.rules.SeedRule
import com.wavesplatform.wallet.v2.ui.auth.import_account.protect_account.ProtectAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.custom.Identicon
import com.wavesplatform.wallet.v2.util.applyFilterStartEmptySpace
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.notNull
import io.github.anderscheow.validator.Validation
import io.github.anderscheow.validator.Validator
import io.github.anderscheow.validator.constant.Mode
import kotlinx.android.synthetic.main.fragment_enter_seed_manually.*
import org.apache.commons.io.Charsets
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.click
import pers.victor.ext.isNetworkConnected
import javax.inject.Inject

class EnterSeedManuallyFragment : BaseFragment(), EnterSeedManuallyView {

    @Inject
    @InjectPresenter
    lateinit var presenter: EnterSeedManuallyPresenter
    lateinit var validator: Validator

    @ProvidePresenter
    fun providePresenter(): EnterSeedManuallyPresenter = presenter

    override fun configLayoutRes() = R.layout.fragment_enter_seed_manually

    override fun onViewReady(savedInstanceState: Bundle?) {
        setSkeleton()

        validator = Validator.with(baseActivity).setMode(Mode.CONTINUOUS)
        val seedValidation = Validation(til_seed)
                .and(NotEmptyTrimRule(" "))
                .and(SeedRule(getString(R.string.enter_seed_manually_validation_seed_exists_error)))

        val identicon = Identicon()

        edit_seed.applyFilterStartEmptySpace()

        edit_seed.addTextChangedListener {
            on { s, start, before, count ->
                validator
                        .validate(object : Validator.OnValidateListener {
                            override fun onValidateSuccess(values: List<String>) {
                                presenter.nextStepValidation = true
                                makeButtonEnableIfValid()
                                if (values.isNotEmpty() && values[0].length > 24) {
                                    val wallet = WavesWallet(values[0].trim().toByteArray(Charsets.UTF_8))
                                    activity?.let {
                                        Glide.with(it)
                                                .load(identicon.create(wallet.address))
                                                .apply(RequestOptions().circleCrop())
                                                .into(image_asset!!)
                                    }
                                    address_asset.text = wallet.address
                                    address_asset.visibility = View.VISIBLE
                                    skeleton_address_asset.visibility = View.GONE
                                } else {
                                    setSkeleton()
                                }
                            }

                            override fun onValidateFailed() {
                                setSkeleton()
                                if (App.getAccessManager().isAccountWithSeedExist(edit_seed.text.toString().trim())) {
                                    val wallet = WavesWallet(edit_seed.text.toString().trim().toByteArray(Charsets.UTF_8))
                                    Glide.with(baseActivity)
                                            .load(identicon.create(wallet.address))
                                            .apply(RequestOptions().circleCrop())
                                            .into(image_asset!!)
                                    address_asset.text = wallet.address
                                    address_asset.visibility = View.VISIBLE
                                    skeleton_address_asset.visibility = View.GONE
                                }
                            }
                        }, seedValidation)
            }
        }

        edit_seed.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && button_continue.isEnabled) {
                launchActivity<ProtectAccountActivity> {
                    putExtra(NewAccountActivity.KEY_INTENT_SEED, edit_seed.text.toString().trim())
                    putExtra(NewAccountActivity.KEY_INTENT_PROCESS_ACCOUNT_IMPORT, true)
                }
                true
            } else {
                false
            }
        }

        button_continue.click {
            launchActivity<ProtectAccountActivity> {
                putExtra(NewAccountActivity.KEY_INTENT_PROCESS_ACCOUNT_IMPORT, true)
                putExtra(NewAccountActivity.KEY_INTENT_SEED, edit_seed.text.toString().trim())
            }
        }
    }

    private fun makeButtonEnableIfValid() {
        button_continue.isEnabled = presenter.nextStepValidation && isNetworkConnected()
    }

    fun setSkeleton() {
        presenter.nextStepValidation = false
        makeButtonEnableIfValid()
        skeleton_address_asset.visibility = View.VISIBLE
        address_asset.visibility = View.GONE
        activity.notNull {
            Glide.with(it)
                    .load(R.drawable.asset_gray_icon)
                    .apply(RequestOptions().circleCrop())
                    .into(image_asset)
        }
    }

    override fun onNetworkConnectionChanged(networkConnected: Boolean) {
        super.onNetworkConnectionChanged(networkConnected)
        button_continue.isEnabled = presenter.nextStepValidation && networkConnected
    }
}
