/*
 * Created by Eduard Zaydel on 8/8/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget.configuration.assets

import android.content.Context.INPUT_METHOD_SERVICE
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen
import com.ethanhua.skeleton.Skeleton
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mindorks.editdrawabletext.DrawablePosition
import com.mindorks.editdrawabletext.EditDrawableText
import com.mindorks.editdrawabletext.OnDrawableClickListener
import com.wavesplatform.sdk.model.response.data.AssetInfoResponse
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.sdk.utils.WavesConstants
import com.wavesplatform.sdk.utils.isWavesId
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.DataServiceManager
import com.wavesplatform.wallet.v2.data.manager.GithubServiceManager
import com.wavesplatform.wallet.v2.data.manager.NodeServiceManager
import com.wavesplatform.wallet.v2.data.model.service.configs.SpamAssetResponse
import com.wavesplatform.wallet.v2.ui.base.view.BaseBottomSheetDialogFragment
import com.wavesplatform.wallet.v2.util.isFiat
import com.wavesplatform.wallet.v2.util.showError
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import kotlinx.android.synthetic.main.content_empty_data.view.*
import pers.victor.ext.inflate
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class MarketWidgetConfigurationAssetsBottomSheetFragment : BaseBottomSheetDialogFragment() {

    private var skeletonScreen: RecyclerViewSkeletonScreen? = null
    private lateinit var editSearch: EditDrawableText
    private lateinit var recycleAssets: RecyclerView
    private val defaultAssets: MutableList<String> = mutableListOf()
    @Inject
    lateinit var dataServiceManager: DataServiceManager
    @Inject
    lateinit var githubServiceManager: GithubServiceManager
    @Inject
    lateinit var nodeDataManager: NodeServiceManager
    @Inject
    lateinit var adapter: MarketWidgetConfigurationAssetsAdapter
    var onChooseListener: OnChooseListener? = null
    private var inputMethodManager: InputMethodManager? = null

    init {
        Constants.defaultCrypto().forEach {
            if (it.isWavesId()) {
                defaultAssets.add(WavesConstants.WAVES_ASSET_ID_FILLED)
            } else {
                defaultAssets.add(it)
            }
        }
        defaultAssets.add(Constants.MrtGeneralAsset.assetId)
        defaultAssets.add(Constants.WctGeneralAsset.assetId)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        adapter.chosenAssets = arguments?.getStringArrayList(ASSETS) ?: arrayListOf()
        inputMethodManager = baseActivity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        val rootView = inflater.inflate(R.layout.bottom_sheet_dialog_assets_layout,
                container, false)

        editSearch = rootView.findViewById(R.id.edit_search)
        recycleAssets = rootView.findViewById(R.id.recycle_assets)

        recycleAssets.layoutManager = LinearLayoutManager(baseActivity)
        adapter.bindToRecyclerView(recycleAssets)

        adapter.onItemChildClickListener =
                BaseQuickAdapter.OnItemChildClickListener { _, view, position ->
                    when (view.id) {
                        R.id.asset_root -> {
                            val item = this.adapter.getItem(position)
                            if (item != null) {
                                onChooseListener?.onChoose(item)
                            }
                            dismiss()
                        }
                    }
                }

        editSearch.setDrawableClickListener(object : OnDrawableClickListener {
            override fun onClick(target: DrawablePosition) {
                if (target == DrawablePosition.RIGHT) {
                    editSearch.text = null
                }
            }
        })

        eventSubscriptions.add(RxTextView.textChanges(editSearch)
                .skipInitialValue()
                .map {
                    if (it.isNotEmpty()) {
                        editSearch.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_search_24_basic_500, 0,
                                R.drawable.ic_clear_24_basic_500, 0)
                    } else {
                        editSearch.setCompoundDrawablesWithIntrinsicBounds(
                                R.drawable.ic_search_24_basic_500, 0,
                                0, 0)
                    }
                    return@map it.toString()
                }
                .debounce(500, TimeUnit.MILLISECONDS)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { query ->
                    search(query.trim())
                })

        skeletonScreen = Skeleton.bind(recycleAssets)
                .adapter(adapter)
                .shimmer(true)
                .count(5)
                .color(R.color.basic50)
                .load(R.layout.item_skeleton_assets)
                .frozen(false)
                .show()
        setSkeletonGradient()
        initLoad()

        editSearch.postDelayed({ inputMethodManager?.showSoftInput(editSearch, 0) }, 50)
        editSearch.requestFocus()

        return rootView
    }

    override fun onCancel(dialog: DialogInterface?) {
        eventSubscriptions.clear()
        inputMethodManager?.hideSoftInputFromWindow(editSearch.windowToken, 0)
    }

    private fun search(query: String) {
        skeletonScreen?.show()
        setSkeletonGradient()

        if (query.trim().isEmpty()) {
            initLoad()
            return
        }

        eventSubscriptions.add(dataServiceManager.assets(search = query.trim())
                .flatMap {
                    Observable.fromArray(it.filter { !spams.containsKey(it.id) && !isFiat(it.id) })
                }
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ result ->
                    skeletonScreen?.hide()
                    adapter.setNewData(result)
                    adapter.emptyView = getEmptyView()
                }, {
                    afterFailGetMarkets()
                    it.printStackTrace()
                }))
    }

    private fun initLoad() {
        eventSubscriptions.add(
                Observable.zip(dataServiceManager.assets(ids = defaultAssets),
                        if (spams.isEmpty()) {
                            githubServiceManager.loadSpamAssets()
                        } else {
                            Observable.fromArray(spams.values.toList())
                        },
                        BiFunction { assets: List<AssetInfoResponse>, spams: List<SpamAssetResponse> ->
                            return@BiFunction Pair(assets, spams)
                        })
                        .flatMap { (assets, spams) ->
                            if (spams.isNotEmpty()) {
                                MarketWidgetConfigurationAssetsBottomSheetFragment.spams = spams.associateBy {
                                    it.assetId
                                }
                            }
                            Observable.fromArray(assets)
                        }
                        .compose(RxUtil.applyObservableDefaultSchedulers())
                        .subscribe({ result ->
                            adapter.setNewData(result)
                            skeletonScreen?.hide()
                            adapter.emptyView = getEmptyView()
                        }, {
                            afterFailGetMarkets()
                            it.printStackTrace()
                        }))
    }

    private fun afterFailGetMarkets() {
        skeletonScreen?.hide()
        showError(R.string.common_server_error, R.id.root)
    }

    private fun getEmptyView(): View {
        val view = inflate(R.layout.content_address_book_empty_state)
        view.text_empty.text = getString(R.string.dex_market_empty)
        return view
    }

    private fun setSkeletonGradient() {
        recycleAssets.post {
            recycleAssets.layoutManager?.findViewByPosition(1)?.alpha = 0.7f
            recycleAssets.layoutManager?.findViewByPosition(2)?.alpha = 0.5f
            recycleAssets.layoutManager?.findViewByPosition(3)?.alpha = 0.4f
            recycleAssets.layoutManager?.findViewByPosition(4)?.alpha = 0.2f
        }
    }

    interface OnChooseListener {
        fun onChoose(asset: AssetInfoResponse)
    }

    companion object {

        private var spams = mapOf<String?, SpamAssetResponse>()

        private const val ASSETS = "assets"

        fun newInstance(assets: ArrayList<String>): MarketWidgetConfigurationAssetsBottomSheetFragment {
            val fragment = MarketWidgetConfigurationAssetsBottomSheetFragment()
            val args = Bundle()
            args.putStringArrayList(ASSETS, assets)
            fragment.arguments = args
            return fragment
        }
    }
}