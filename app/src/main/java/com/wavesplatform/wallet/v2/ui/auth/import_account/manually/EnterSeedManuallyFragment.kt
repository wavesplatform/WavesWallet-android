/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.import_account.manually

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.wavesplatform.wallet.v2.util.WavesWallet
import com.jakewharton.rxbinding2.widget.RxTextView
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.analytics.AnalyticEvents
import com.wavesplatform.wallet.v2.data.analytics.analytics
import com.wavesplatform.wallet.v2.data.rules.NotEmptyTrimRule
import com.wavesplatform.wallet.v2.data.rules.SeedRule
import com.wavesplatform.wallet.v2.ui.auth.import_account.protect_account.ProtectAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.sdk.utils.Identicon
import com.wavesplatform.wallet.v2.util.applyFilterStartEmptySpace
import com.wavesplatform.wallet.v2.util.launchActivity
import com.wavesplatform.wallet.v2.util.onAction
import com.wavesplatform.sdk.utils.notNull
import io.github.anderscheow.validator.Validation
import io.github.anderscheow.validator.Validator
import io.github.anderscheow.validator.constant.Mode
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_enter_seed_manually.*
import org.apache.commons.io.Charsets
import pers.victor.ext.*
import java.util.concurrent.TimeUnit
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

        eventSubscriptions.add(RxTextView.textChanges(edit_seed)
                .skipInitialValue()
                .map { it.toString() }
                .debounce(500, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    presenter.simpleValidationAlertShown = false
                    relative_validation_error.gone()
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
                                                    .into(image_asset)
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
                                    if (App.accessManager.isAccountWithSeedExist(edit_seed.text.toString().trim())) {
                                        val wallet = WavesWallet(edit_seed.text.toString().trim().toByteArray(Charsets.UTF_8))
                                        Glide.with(baseActivity)
                                                .load(identicon.create(wallet.address))
                                                .apply(RequestOptions().circleCrop())
                                                .into(image_asset)
                                        address_asset.text = wallet.address
                                        address_asset.visibility = View.VISIBLE
                                        skeleton_address_asset.visibility = View.GONE
                                    }
                                }
                            }, seedValidation)
                })

        edit_seed.onAction(EditorInfo.IME_ACTION_DONE) {
            if (button_continue.isEnabled) {
                launchActivity<ProtectAccountActivity> {
                    putExtra(NewAccountActivity.KEY_INTENT_SEED, edit_seed.text.toString().trim())
                    putExtra(NewAccountActivity.KEY_INTENT_PROCESS_ACCOUNT_IMPORT, true)
                }
            }
        }

        button_continue.click {
            if (presenter.simpleValidationAlertShown || isSimpleValidationPassed()) {
                analytics.trackEvent(AnalyticEvents.StartImportManuallyEvent)

                launchActivity<ProtectAccountActivity> {
                    putExtra(NewAccountActivity.KEY_INTENT_PROCESS_ACCOUNT_IMPORT, true)
                    putExtra(NewAccountActivity.KEY_INTENT_SEED, edit_seed.text.toString().trim())
                }
            }
        }
    }

    private fun isSimpleValidationPassed(): Boolean {
        presenter.simpleValidationAlertShown = true
        return when {
            edit_seed.text.toString().length < 59 -> {
                hideInputMethod()
                text_validation_title.text = getString(R.string.enter_seed_manually_validation_seed_is_simple_title)
                text_validation_description.text = getString(R.string.enter_seed_manually_validation_seed_is_simple_description)
                relative_validation_error.visiable()
                false
            }
            else -> {
                true
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
