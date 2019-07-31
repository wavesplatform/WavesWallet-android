package com.wavesplatform.wallet.v2.ui.widget

import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ethanhua.skeleton.RecyclerViewSkeletonScreen
import com.ethanhua.skeleton.Skeleton
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mindorks.editdrawabletext.EditDrawableText
import com.wavesplatform.sdk.utils.RxUtil
import com.wavesplatform.sdk.utils.WavesConstants
import com.wavesplatform.sdk.utils.isWavesId
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.data.manager.DataServiceManager
import com.wavesplatform.wallet.v2.ui.base.view.BaseBottomSheetDialogFragment
import com.wavesplatform.wallet.v2.util.EnvironmentManager
import com.wavesplatform.wallet.v2.util.showError
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_dex_markets.*
import kotlinx.android.synthetic.main.content_empty_data.view.*
import pers.victor.ext.inflate
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AssetsBottomSheetFragment : BaseBottomSheetDialogFragment() {

    private var assets = arrayListOf<String>()
    private var skeletonScreen: RecyclerViewSkeletonScreen? = null
    private lateinit var editSearch: EditDrawableText
    private lateinit var recycleAssets: RecyclerView
    private val defaultAssets: MutableList<String> = mutableListOf()
    @Inject
    lateinit var dataServiceManager: DataServiceManager
    @Inject
    lateinit var adapter: AssetsAdapter
    var onChooseListener: OnChooseListener? = null

    init {
        EnvironmentManager.defaultAssets.forEach {
            if (it.assetId.isWavesId()) {
                defaultAssets.add(WavesConstants.WAVES_ASSET_ID_FILLED)
            } else {
                defaultAssets.add(it.assetId)
            }
        }
        defaultAssets.add(Constants.VstGeneralAsset.assetId)
        defaultAssets.add(Constants.MrtGeneralAsset.assetId)
        defaultAssets.add(Constants.WctGeneralAsset.assetId)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        assets = arguments?.getStringArrayList(ASSETS) ?: arrayListOf()

        val rootView = inflater.inflate(R.layout.bottom_sheet_dialog_assets_layout, container, false)

        editSearch = rootView.findViewById(R.id.edit_search)
        recycleAssets = rootView.findViewById(R.id.recycle_assets)

        recycleAssets.layoutManager = LinearLayoutManager(baseActivity)
        adapter.bindToRecyclerView(recycleAssets)

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

        return rootView
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)
        val assetsList = arrayListOf<String>()
        adapter.chosenAssets.forEach {
            assetsList.add(it.id)
        }
        onChooseListener?.onChoose(assetsList)
    }

    private fun search(query: String) {
        skeletonScreen?.show()
        setSkeletonGradient()

        if (query.isEmpty()) {
            initLoad()
            return
        }

        eventSubscriptions.add(dataServiceManager.assets(search = query.trim())
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ result ->
                    skeletonScreen?.hide()
                    adapter.allData = ArrayList(AssetsAdapter.convert(result))
                    adapter.setNewData(AssetsAdapter.convert(result))
                    adapter.emptyView = getEmptyView()
                }, {
                    afterFailGetMarkets()
                    it.printStackTrace()
                }))
    }

    private fun initLoad() {
        val initAssets = if (assets.isEmpty()) {
            defaultAssets
        } else {
            assets
        }

        eventSubscriptions.add(dataServiceManager.assets(ids = initAssets)
                .compose(RxUtil.applyObservableDefaultSchedulers())
                .subscribe({ result ->
                    skeletonScreen?.hide()
                    adapter.allData = ArrayList(AssetsAdapter.convert(result))
                    adapter.setNewData(AssetsAdapter.convert(result))
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
        recycle_markets?.post {
            recycle_markets?.layoutManager?.findViewByPosition(1)?.alpha = 0.7f
            recycle_markets?.layoutManager?.findViewByPosition(2)?.alpha = 0.5f
            recycle_markets?.layoutManager?.findViewByPosition(3)?.alpha = 0.4f
            recycle_markets?.layoutManager?.findViewByPosition(4)?.alpha = 0.2f
        }
    }

    interface OnChooseListener {
        fun onChoose(assets: ArrayList<String>)
    }

    companion object {

        private const val ASSETS = "assets"

        fun newInstance(assets: ArrayList<String> = arrayListOf()): AssetsBottomSheetFragment {
            val fragment = AssetsBottomSheetFragment()
            val args = Bundle()
            args.putStringArrayList(ASSETS, assets)
            fragment.arguments = args
            return fragment
        }
    }
}