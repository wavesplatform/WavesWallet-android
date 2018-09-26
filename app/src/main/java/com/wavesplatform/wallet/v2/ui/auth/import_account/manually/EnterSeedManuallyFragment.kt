package com.wavesplatform.wallet.v2.ui.auth.import_account.manually

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v1.data.auth.WavesWallet
import com.wavesplatform.wallet.v2.ui.auth.import_account.protect_account.ProtectAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.new_account.NewAccountActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseFragment
import com.wavesplatform.wallet.v2.ui.custom.Identicon
import com.wavesplatform.wallet.v2.util.launchActivity
import io.github.anderscheow.validator.Validation
import io.github.anderscheow.validator.Validator
import io.github.anderscheow.validator.constant.Mode
import io.github.anderscheow.validator.rules.common.NotEmptyRule
import kotlinx.android.synthetic.main.fragment_enter_seed_manually.*
import org.apache.commons.io.Charsets
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.click
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
                .and(NotEmptyRule(" "))

        val identicon = Identicon()

        edit_seed.addTextChangedListener {
            on { s, start, before, count ->
                validator
                        .validate(object : Validator.OnValidateListener {
                            override fun onValidateSuccess(values: List<String>) {
                                button_continue.isEnabled = true
                                if (values.isNotEmpty() && values[0].length > 24 ) {
                                    val wallet = WavesWallet(values[0].toByteArray(Charsets.UTF_8))
                                    Glide.with(activity)
                                            .load(identicon.create(wallet.address))
                                            .apply(RequestOptions().circleCrop())
                                            .into(image_asset!!)
                                    address_asset.text = wallet.address
                                    address_asset.visibility = View.VISIBLE
                                    skeleton_address_asset.visibility = View.GONE
                                } else {
                                    setSkeleton()
                                }
                            }

                            override fun onValidateFailed() {
                                setSkeleton()
                            }
                        }, seedValidation)
            }
        }

        edit_seed.imeOptions = EditorInfo.IME_ACTION_DONE
        edit_seed.setRawInputType(InputType.TYPE_CLASS_TEXT)
        edit_seed.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && button_continue.isEnabled) {
                launchActivity<ProtectAccountActivity> {
                    putExtra(NewAccountActivity.KEY_INTENT_SEED, edit_seed.text.toString().trim())
                }
                true
            } else {
                false
            }
        }

        button_continue.click {
            launchActivity<ProtectAccountActivity> {
                putExtra(NewAccountActivity.KEY_INTENT_SEED, edit_seed.text.toString().trim())
            }
        }
    }

    fun setSkeleton() {
        button_continue.isEnabled = false
        skeleton_address_asset.visibility = View.VISIBLE
        address_asset.visibility = View.GONE
        Glide.with(activity)
                .load(R.drawable.asset_gray_icon)
                .apply(RequestOptions().circleCrop())
                .into(image_asset)
    }
}
