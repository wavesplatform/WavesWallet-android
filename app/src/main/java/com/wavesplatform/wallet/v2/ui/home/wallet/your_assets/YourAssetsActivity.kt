package com.wavesplatform.wallet.v2.ui.home.wallet.your_assets

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.AppCompatCheckBox
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.chad.library.adapter.base.BaseQuickAdapter
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mindorks.editdrawabletext.DrawablePosition
import com.mindorks.editdrawabletext.onDrawableClickListener
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_your_assets.*
import pyxis.uzuki.live.richutilskt.utils.runDelayed
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class YourAssetsActivity : BaseActivity(), YourAssetsView {

    @Inject
    @InjectPresenter
    lateinit var presenter: YourAssetsPresenter


    @Inject
    lateinit var adapter: YourAssetsAdapter

    @ProvidePresenter
    fun providePresenter(): YourAssetsPresenter = presenter

    override fun configLayoutRes() = R.layout.activity_your_assets

    companion object {
        var BUNDLE_ASSET_ITEM = "asset"
    }


    override fun onViewReady(savedInstanceState: Bundle?) {

        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, getString(R.string.your_assets_toolbar_title), R.drawable.ic_toolbar_back_black)

        eventSubscriptions.add(RxTextView.textChanges(edit_search)
                .skipInitialValue()
                .map({
                    if (it.isNotEmpty()) {
                        edit_search.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_24_basic_500, 0, R.drawable.ic_clear_24_basic_500, 0)
                    } else {
                        edit_search.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_24_basic_500, 0, 0, 0)
                    }
                    return@map it.toString()
                })
                .debounce(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    adapter.filter(it)
                }))


        edit_search.setDrawableClickListener(object : onDrawableClickListener {
            override fun onClick(target: DrawablePosition) {
                when (target) {
                    DrawablePosition.RIGHT -> {
                        edit_search.text = null
                    }
                }
            }
        })

        recycle_assets.layoutManager = LinearLayoutManager(this)
        recycle_assets.adapter = adapter
        adapter.bindToRecyclerView(recycle_assets)

        presenter.loadAssets()

        adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position ->
            val item = adapter.getItem(position) as AssetBalance
            val viewItem = recycle_assets.layoutManager.findViewByPosition(position)
            val checkBox = viewItem.findViewById<AppCompatCheckBox>(R.id.checkbox_choose)
            checkBox.isChecked = true

            // disable click for next items, which user click before activity will finish
            adapter.onItemClickListener = BaseQuickAdapter.OnItemClickListener { adapter, view, position -> }

            // finish activity with result and timeout
            runDelayed(500, {
                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra(BUNDLE_ASSET_ITEM, item)
                })
                finish()
            })
        }
    }

    override fun showAssets(assets: ArrayList<AssetBalance>) {
        adapter.allData = ArrayList(assets)
        adapter.setNewData(assets)
    }

}
