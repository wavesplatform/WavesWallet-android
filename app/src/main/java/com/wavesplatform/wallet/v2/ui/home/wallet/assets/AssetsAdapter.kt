package com.wavesplatform.wallet.v2.ui.home.wallet.assets

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wavesplatform.wallet.App
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.data.model.remote.response.AssetBalance
import com.wavesplatform.wallet.v2.util.makeTextHalfBold
import com.wavesplatform.wallet.v2.util.notNull
import kotlinx.android.synthetic.main.wallet_asset_item.view.*
import javax.inject.Inject

class AssetsAdapter @Inject constructor() :
        RecyclerView.Adapter<AssetsAdapter.AssetsBaseViewHolder>() {

    var data = arrayListOf<AssetBalance>()
    var onClickListener: OnItemClick? = null

    fun update(data: List<AssetBalance>) {
        this.data.clear()
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetsBaseViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.wallet_asset_item, parent, false)
        return AssetsBaseViewHolder(view)
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: AssetsBaseViewHolder, position: Int) {
        val item = data[position]
        holder.itemView.text_asset_name.text = item.getName()
        holder.itemView.text_asset_value.text = item.getDisplayBalance()
        holder.itemView.image_favourite.visibility = if (item.isFavorite) {
            View.VISIBLE
        } else {
            View.GONE
        }
        holder.itemView.text_my_asset.visibility = if (item.issueTransaction?.sender
                == App.getAccessManager().getWallet()?.address) {
            View.VISIBLE
        } else {
            View.GONE
        }
        holder.itemView.image_down_arrow.visibility = if (item.isGateway) {
            View.VISIBLE
        } else {
            View.GONE
        }
        holder.itemView.text_tag_spam.visibility = if (item.isSpam) {
            View.VISIBLE
        } else {
            View.GONE
        }
        holder.itemView.image_asset_icon.isOval = true
        holder.itemView.image_asset_icon.setAsset(item)
        holder.itemView.text_asset_value.makeTextHalfBold()

        onClickListener.notNull { onClick ->
            holder.itemView.setOnClickListener {onClick.onClick(item, position)}
        }
    }

    class AssetsBaseViewHolder(view: View) : RecyclerView.ViewHolder(view)

    interface OnItemClick {
        fun onClick(assetBalance: AssetBalance, position: Int)
    }
}
