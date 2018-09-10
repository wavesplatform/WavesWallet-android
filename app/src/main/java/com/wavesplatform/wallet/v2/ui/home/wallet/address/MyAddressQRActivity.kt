package com.wavesplatform.wallet.v2.ui.home.wallet.address

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.widget.AppCompatImageView
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.vicpin.krealmextensions.queryAllAsync
import com.wavesplatform.wallet.BlockchainApplication
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.Alias
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.Identicon
import com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias.AddressesAndKeysBottomSheetFragment
import com.wavesplatform.wallet.v2.util.copyToClipboard
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.activity_my_address_qr.*
import pers.victor.ext.click
import pers.victor.ext.findColor
import pyxis.uzuki.live.richutilskt.utils.runDelayed
import javax.inject.Inject


class MyAddressQRActivity : BaseActivity(), MyAddressQrView {

    override fun afterSuccessGenerateAvatar(bitmap: Bitmap, imageView: AppCompatImageView) {
        Glide.with(applicationContext)
                .load(bitmap)
                .apply(RequestOptions()
                        .circleCrop())
                .into(imageView)
    }

    @Inject
    @InjectPresenter
    lateinit var presenter: MyAddressQrPresenter

    @ProvidePresenter
    fun providePresenter(): MyAddressQrPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_my_address_qr

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, icon = R.drawable.ic_toolbar_back_black)

        val user = BlockchainApplication.getAccessManager().createAddressBookCurrentAccount()
        text_address.text = user!!.address
        Glide.with(image_avatar.context)
                .load(Identicon.create(
                        user.address,
                        Identicon.Options.Builder().setRandomBlankColor().create()))
                .apply(RequestOptions().circleCrop())
                .into(image_avatar)

        publicKeyAccountHelper.publicKeyAccount?.address
        frame_share.click {
            shareAddress()
        }

        queryAllAsync<Alias> { aliases ->
            val ownAliases = aliases.filter { it.own }

            text_aliases_count.text = String.format(getString(R.string.alias_dialog_you_have), ownAliases.size)

            card_aliases.click {
                val bottomSheetFragment = AddressesAndKeysBottomSheetFragment()
                if (ownAliases.isEmpty()) {
                    bottomSheetFragment.type = AddressesAndKeysBottomSheetFragment.TYPE_EMPTY
                } else {
                    bottomSheetFragment.type = AddressesAndKeysBottomSheetFragment.TYPE_CONTENT
                }
                bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
            }
        }

        frame_copy.click {
            text_copy.text = getString(R.string.common_copied)
            text_copy.setTextColor(findColor(R.color.success400))
            text_copy.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_18_success_400, 0, 0, 0);
            text_address.copyToClipboard()

            runDelayed(1500) {
                this.text_copy.notNull {
                    text_copy.text = getString(R.string.my_address_qr_copy)
                    text_copy.setTextColor(findColor(R.color.black))
                    text_copy.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_copy_18_submit_400, 0, 0, 0);
                }
            }
        }

        presenter.generateAvatars(user.address, image_avatar)
        presenter.generateQRCode(user.address, resources.getDimension(R.dimen._200sdp).toInt())
    }

    override fun showQRCode(qrCode: Bitmap?) {
        image_view_recipient_action.setImageBitmap(qrCode)
    }

    private fun shareAddress() {
        val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, text_address.text)
        startActivity(Intent.createChooser(sharingIntent, resources.getString(R.string.app_name)))
    }
}
