package com.wavesplatform.wallet.v2.ui.home.wallet.assets.details

import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.vicpin.krealmextensions.queryFirst
import com.vicpin.krealmextensions.save
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.welcome.AlphaScalePageTransformer
import kotlinx.android.synthetic.main.activity_asset_details.*
import pers.victor.ext.click
import pers.victor.ext.dp2px
import pers.victor.ext.visiable
import pyxis.uzuki.live.richutilskt.utils.runAsync
import java.util.*
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
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, icon = R.drawable.ic_toolbar_back_black)

        view_pager.adapter = adapterAvatar
        view_pager.offscreenPageLimit = 3
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
                showFavorite()
                view_pager_content.setCurrentItem(position, true)
            }
        })

        view_pager_content.offscreenPageLimit = 3
        view_pager_content.clipToPadding = false
        view_pager_content.setPagingEnabled(false)

        image_favorite.click {
            changeFavorite()
        }

        runAsync({
            presenter.loadAssets()
        })
    }

    private fun configureTitleForAssets(position: Int) {
        val item = adapterAvatar.items[position]
        text_asset_name.text = item.getName()

        val avatar = Constants.defaultAssetsAvatar[item.assetId]
        if (avatar == null) {
            text_asset_description.text = ""
        } else {
            when {
                item.isAssetId("") -> text_asset_description.setText(R.string.asset_details_waves_token)
                item.isFlatMoney -> text_asset_description.setText(R.string.asset_details_flat_money)
                else -> text_asset_description.setText(R.string.asset_details_cryptocurrency)
            }
        }
    }

    override fun afterSuccessLoadAssets(sortedToFirstFavoriteList: ArrayList<AssetBalance>) {
        // configure top avatars pager
        adapterAvatar.items = sortedToFirstFavoriteList
        adapterAvatar.notifyDataSetChanged()
        view_pager.setCurrentItem(intent.getIntExtra(BUNDLE_ASSET_POSITION, 0), false)
        view_pager.post({
            if (view_pager.beginFakeDrag()){
                view_pager.fakeDragBy(0f)
                view_pager.endFakeDrag()
            }
        })
        configureTitleForAssets(view_pager.currentItem)
        showFavorite()

        // configure contents pager
        view_pager_content.adapter = AssetDetailsContentPageAdapter(supportFragmentManager, sortedToFirstFavoriteList)
        view_pager_content.setCurrentItem(intent.getIntExtra(BUNDLE_ASSET_POSITION, 0), false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_favorite -> {
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun showFavorite() {
        image_favorite.visiable()
        if (adapterAvatar.items[view_pager.currentItem].isFavorite) {
            markAsFavorite()
        } else {
            unmarkAsFavorite()
        }
    }

    fun changeFavorite() {
        if (!adapterAvatar.items[view_pager.currentItem].isWaves()){
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
}
