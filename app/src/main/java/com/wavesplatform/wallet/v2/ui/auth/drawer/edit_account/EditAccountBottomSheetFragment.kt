/*
 * Created by Eduard Zaydel on 1/10/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.auth.drawer.edit_account

import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.wavesplatform.sdk.utils.Identicon
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.db.userdb.AddressBookUserDb
import com.wavesplatform.wallet.v2.data.rules.AccountNameRule
import com.wavesplatform.wallet.v2.data.rules.NotEmptyTrimRule
import com.wavesplatform.wallet.v2.ui.auth.choose_account.edit.EditAccountNamePresenter
import com.wavesplatform.wallet.v2.ui.auth.choose_account.edit.EditAccountNameView
import com.wavesplatform.wallet.v2.ui.base.view.BaseBottomSheetDialogFragment
import io.github.anderscheow.validator.Validation
import io.github.anderscheow.validator.Validator
import io.github.anderscheow.validator.constant.Mode
import io.github.anderscheow.validator.rules.common.MaxRule
import kotlinx.android.synthetic.main.bottom_sheet_dialog_edit_account_layout.view.*
import pers.victor.ext.addTextChangedListener
import pers.victor.ext.click
import javax.inject.Inject

class EditAccountBottomSheetFragment : BaseBottomSheetDialogFragment(), EditAccountNameView {

    val validator: Validator by lazy { Validator.with(requireActivity()).setMode(Mode.CONTINUOUS) }
    var listener: EditAccountListener? = null

    @Inject
    @InjectPresenter
    lateinit var presenter: EditAccountNamePresenter

    @ProvidePresenter
    fun providePresenter(): EditAccountNamePresenter = presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.EditAccountDialog)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val rootView = inflater.inflate(R.layout.bottom_sheet_dialog_edit_account_layout, container, false)

        presenter.account = arguments?.getParcelable(KEY_INTENT_ITEM_ADDRESS)

        rootView.text_name.text = presenter.account?.name
        val address = presenter.account?.address
        Glide.with(this)
                .load(Identicon().create(address))
                .apply(RequestOptions().circleCrop())
                .into(rootView.image_asset)

        rootView.button_save.click {

            if (presenter.accountNameFieldValid) {
                presenter.account?.name = rootView.edit_account_name.text.toString().trim()

                presenter.account?.let { account ->
                    listener?.onSuccess(arguments?.getInt(KEY_INTENT_ITEM_POSITION, NO_POSITION)
                            ?: NO_POSITION, account)
                    dismiss()
                }
            }
        }

        val nameValidation = Validation(rootView.til_account_name)
                .and(NotEmptyTrimRule(R.string.new_account_account_name_validation_required_error))
                .and(MaxRule(20, R.string.new_account_account_name_validation_length_error))
                .and(AccountNameRule(R.string.new_account_account_name_validation_already_exist_error))

        rootView.edit_account_name.addTextChangedListener {
            on { s, start, before, count ->
                validator
                        .validate(object : Validator.OnValidateListener {
                            override fun onValidateSuccess(values: List<String>) {
                                presenter.accountNameFieldValid = true

                                rootView.button_save.isEnabled = true
                            }

                            override fun onValidateFailed() {
                                presenter.accountNameFieldValid = false

                                rootView.button_save.isEnabled = false
                            }
                        }, nameValidation)
            }
        }

        return rootView
    }

    companion object {
        private const val NO_POSITION = -1
        private const val KEY_INTENT_ITEM_ADDRESS = "intent_item_address"
        private const val KEY_INTENT_ITEM_POSITION = "intent_item_position"

        fun newInstance(position: Int, account: AddressBookUserDb?): EditAccountBottomSheetFragment {
            val dialog = EditAccountBottomSheetFragment()

            dialog.arguments = Bundle().apply {
                putParcelable(KEY_INTENT_ITEM_ADDRESS, account)
                putInt(KEY_INTENT_ITEM_POSITION, position)
            }

            return dialog
        }
    }

    interface EditAccountListener {
        fun onSuccess(position: Int, account: AddressBookUserDb)
    }
}