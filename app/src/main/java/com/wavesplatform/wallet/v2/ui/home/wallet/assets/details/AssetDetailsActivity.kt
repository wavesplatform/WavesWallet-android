package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details

import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.Menu
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.vicpin.krealmextensions.queryFirst
import com.vicpin.krealmextensions.save
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.wallet.assets.AssetsFragment.Companion.RESULT_NEED_UPDATE
import com.wavesplatform.wallet.v2.ui.welcome.AlphaScalePageTransformer
import kotlinx.android.synthetic.main.activity_asset_details.*
import pers.victor.ext.click
import pers.victor.ext.dp2px
import pers.victor.ext.gone
import pers.victor.ext.visiable
import pyxis.uzuki.live.richutilskt.utils.runAsync
import javax.inject.Inject


class AssetDetailsActivity : BaseActivity(), AssetDetailsView {

    @Inject
    @InjectPresenter
    lateinit var presenter: AssetDetailsPresenter

    @ProvidePresenter
    fun providePresenter(): AssetDetailsPresenter = presenter

    @Inject
    lateinit var adapterAvatar: AssetDetailsAvatarPagerAdapter

    lateinit var menu: Menu

    override fun configLayoutRes() = R.layout.activity_asset_details

    companion object {
        var BUNDLE_ASSET_BALANCE_ITEM = "assetBalance"
        var BUNDLE_ASSET_POSITION = "position"
    }

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.basic50)
        setupToolbar(toolbar_view, true, icon = R.drawable.ic_toolbar_back_black)

        view_pager.adapter = adapterAvatar
        view_pager.offscreenPageLimit = 10
        view_pager.clipToPadding = false
        view_pager.setPadding(dp2px(160) / 2 - dp2px(24), 0, dp2px(160) / 2 - dp2px(24), 0)
        view_pager.setPageTransformer(false, AlphaScalePageTransformer(0.58f))
        view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                configureTitleForAssets(position)
                showFavorite(view_pager.currentItem)
                view_pager_content.setCurrentItem(position, true)
            }
        })

        view_pager_content.offscreenPageLimit = 3
        view_pager_content.clipToPadding = false
        view_pager_content.setPagingEnabled(false)

        image_favorite.click {
            presenter.needToUpdate = true
            changeFavorite()
        }

        runAsync({
            presenter.loadAssets()
        })
    }

    private fun configureTitleForAssets(position: Int) {
        val item = adapterAvatar.items[position]
        text_asset_name.text = item.getName()

        text_asset_description.visiable()
        spam_tag.gone()

        when {
            item.isWaves() -> text_asset_description.setText(R.string.asset_details_waves_token)
            item.isGateway -> text_asset_description.setText(R.string.asset_details_cryptocurrency)
            item.isFiatMoney -> text_asset_description.setText(R.string.asset_details_flat_money)
            item.isSpam -> {
                text_asset_description.gone()
                spam_tag.visiable()
            }
            else -> text_asset_description.text = ""
        }
    }

    override fun afterSuccessLoadAssets(sortedToFirstFavoriteList: MutableList<AssetBalance>) {
        // configure top avatars pager
        adapterAvatar.items = sortedToFirstFavoriteList
        adapterAvatar.notifyDataSetChanged()
        view_pager.setCurrentItem(intent.getIntExtra(BUNDLE_ASSET_POSITION, 0), false)
        view_pager.post {
            if (view_pager.beginFakeDrag()) {
                view_pager.fakeDragBy(0f)
                view_pager.endFakeDrag()
            }
        }
        configureTitleForAssets(view_pager.currentItem)
        showFavorite(view_pager.currentItem)

        // configure contents pager
        view_pager_content.adapter = AssetDetailsContentPageAdapter(supportFragmentManager, ArrayList(sortedToFirstFavoriteList))
        view_pager_content.setCurrentItem(intent.getIntExtra(BUNDLE_ASSET_POSITION, 0), false)
    }

    fun showFavorite(currentItem: Int) {
        if (adapterAvatar.items[currentItem].isSpam) image_favorite.gone()
        else image_favorite.visiable()

        if (adapterAvatar.items[view_pager.currentItem].isFavorite) {
            markAsFavorite()
        } else {
            unmarkAsFavorite()
        }
    }

    private fun changeFavorite() {
        if (!adapterAvatar.items[view_pager.currentItem].isWaves()) {
            if (adapterAvatar.items[view_pager.currentItem].isFavorite) {
                unmarkAsFavorite()
            } else {
                markAsFavorite()
            }
        }
    }

    private fun unmarkAsFavorite() {
        adapterAvatar.items[view_pager.currentItem].isFavorite = false
        runAsync {
            val assetBalance = queryFirst<AssetBalance>({ equalTo("assetId", adapterAvatar.items[view_pager.currentItem].assetId) })
            assetBalance?.isFavorite = false
            assetBalance?.save()
        }
        image_favorite.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_toolbar_favorite_off))
    }

    private fun markAsFavorite() {
        adapterAvatar.items[view_pager.currentItem].isFavorite = true
        runAsync {
            var assetBalance = queryFirst<AssetBalance>({ equalTo("assetId", adapterAvatar.items[view_pager.currentItem].assetId) })
            assetBalance?.isFavorite = true
            assetBalance?.save()
        }
        image_favorite.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_toolbar_favorite_on))
    }

    override fun onBackPressed() {
        setResult(Constants.RESULT_OK, Intent().apply {
            putExtra(RESULT_NEED_UPDATE, presenter.needToUpdate)
        })
        finish()
    }
}
