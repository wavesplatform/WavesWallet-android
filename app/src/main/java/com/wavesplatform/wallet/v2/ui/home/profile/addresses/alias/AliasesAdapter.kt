package com.wavesplatform.wallet.v2.ui.home.profile.addresses.alias

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.jakewharton.rxbinding2.view.RxView
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.Alias
import com.wavesplatform.wallet.v2.util.copyToClipboard
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.aliases_layout.view.*
import pers.victor.ext.gone
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AliasesAdapter @Inject constructor() : BaseQuickAdapter<Alias, BaseViewHolder>(R.layout.aliases_layout) {

    var subscriptions: CompositeDisposable? = null

    override fun convert(helper: BaseViewHolder, item: Alias) {
        helper.itemView.text_alias_name.text = item.alias

        subscriptions?.add(RxView.clicks(helper.itemView.image_copy)
                .throttleFirst(1500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    helper.itemView.image_copy.copyToClipboard(helper.itemView.text_alias_name.text.toString(), R.drawable.ic_copy_18_submit_400)
                })
        if (data.indexOf(item) == data.size - 1) {
            helper.itemView.view_dashed.gone()
        }
    }
}
