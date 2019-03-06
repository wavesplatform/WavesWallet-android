package com.wavesplatform.wallet.v2.ui.home.wallet.address

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.widget.AppCompatImageView
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.jakewharton.rxbinding2.view.RxView
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.Alias
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.custom.Identicon
import com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias.AliasBottomSheetFragment
import com.wavesplatform.wallet.v2.util.copyToClipboard
import com.wavesplatform.wallet.v2.util.notNull
import com.wavesplatform.wallet.v2.util.showSuccess
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_my_address_qr.*
import pers.victor.ext.click
import pers.victor.ext.findColor
import pyxis.uzuki.live.richutilskt.utils.runDelayed
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MyAddressQRActivity : BaseActivity(), MyAddressQrView {

    @Inject
    @InjectPresenter
    lateinit var presenter: MyAddressQrPresenter

    @ProvidePresenter
    fun providePresenter(): MyAddressQrPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_my_address_qr

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(R.anim.slide_in_right, R.anim.null_animation)
        super.onCreate(savedInstanceState)
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        setNavigationBarColor(R.color.basic50)
        setupToolbar(toolbar_view, true, icon = R.drawable.ic_toolbar_back_black)

        val address = App.getAccessManager().getWallet()?.address
        text_address.text = address
        Glide.with(image_avatar.context)
                .load(Identicon().create(address))
                .apply(RequestOptions().circleCrop())
                .into(image_avatar)

        eventSubscriptions.add(RxView.clicks(frame_share)
                .throttleFirst(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    shareAddress()
                })

        eventSubscriptions.add(RxView.clicks(frame_copy)
                .throttleFirst(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    text_copy.text = getString(R.string.common_copied)
                    text_copy.setTextColor(findColor(R.color.success400))
                    text_copy.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_18_success_400, 0, 0, 0)
                    text_address.copyToClipboard()

                    runDelayed(1500) {
                        this.text_copy.notNull {
                            text_copy.text = getString(R.string.my_address_qr_copy)
                            text_copy.setTextColor(findColor(R.color.submit400))
                            text_copy.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_copy_18_submit_400, 0, 0, 0)
                        }
                    }
                })

        presenter.generateAvatars(App.getAccessManager().getWallet()?.address, image_avatar)
        presenter.generateQRCode(App.getAccessManager().getWallet()?.address, resources.getDimension(R.dimen._200sdp).toInt())

        presenter.loadAliases()
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

    override fun afterSuccessLoadAliases(ownAliases: List<Alias>) {
        if (ownAliases.isEmpty()) {
            text_aliases_count.text = getString(R.string.addresses_and_keys_you_do_not_have)
        } else {
            text_aliases_count.text = String.format(getString(R.string.alias_dialog_you_have), ownAliases.size)
        }
        card_aliases.click {
            val bottomSheetFragment = AliasBottomSheetFragment()
            if (ownAliases.isEmpty()) {
                bottomSheetFragment.type = AliasBottomSheetFragment.TYPE_EMPTY
            } else {
                bottomSheetFragment.type = AliasBottomSheetFragment.TYPE_CONTENT
            }
            bottomSheetFragment.show(supportFragmentManager, bottomSheetFragment.tag)
            bottomSheetFragment.onCreateAliasListener = object : AliasBottomSheetFragment.OnCreateAliasListener {
                override fun onSuccess() {
                    bottomSheetFragment.dismiss()
                    showSuccess(getString(R.string.new_alias_success_create), R.id.root)
                }
            }
        }
    }

    override fun afterSuccessGenerateAvatar(bitmap: Bitmap, imageView: AppCompatImageView) {
        Glide.with(applicationContext)
                .load(bitmap)
                .apply(RequestOptions()
                        .circleCrop())
                .into(imageView)
    }

    override fun onBackPressed() {
        finish()
        overridePendingTransition(R.anim.null_animation, R.anim.slide_out_right)
    }
}
