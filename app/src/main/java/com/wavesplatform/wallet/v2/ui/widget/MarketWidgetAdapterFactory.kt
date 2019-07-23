/*
 * Created by Eduard Zaydel on 22/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.wavesplatform.wallet.v2.data.Constants
import com.wavesplatform.wallet.v2.ui.custom.AssetAvatarView
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetTheme
import pers.victor.ext.dp
import pers.victor.ext.sp


class MarketWidgetAdapterFactory constructor(var context: Context, intent: Intent) : RemoteViewsService.RemoteViewsFactory {

    var data: MutableList<String> = mutableListOf()
    private var widgetID: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    init {
        widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID)
    }

    override fun onCreate() {
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewAt(position: Int): RemoteViews {
        val theme = MarketWidgetTheme.getTheme(context, widgetID)
        val marketViewRv = RemoteViews(context.packageName, theme.marketItemLayout)
        val data = data[position]

        marketViewRv.setImageViewBitmap(com.wavesplatform.wallet.R.id.image_asset_icon, createAvatarViewBitmap(data))
        marketViewRv.setTextViewText(com.wavesplatform.wallet.R.id.text_asset_name, data)

        return marketViewRv
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun hasStableIds(): Boolean {
        return true
    }

    override fun onDataSetChanged() {
        data.clear()
        data.add("btc")
        data.add("btc1")
        data.add("btc2")
        data.add("btc3")
        data.add("btc4")
    }

    override fun onDestroy() {

    }

    private fun createAvatarViewBitmap(data: String): Bitmap? {
        val assetAvatarView = AssetAvatarView(context)

        assetAvatarView.configureForWidget()
        // TODO: Change from waves to 'data'
        assetAvatarView.setAsset(Constants.wavesAssetInfo)
        assetAvatarView.textSize = 11.sp.toFloat()

        return loadBitmapFromView(assetAvatarView, 20.dp)
    }

    private fun loadBitmapFromView(v: View, size: Int): Bitmap {
        v.measure(View.MeasureSpec.makeMeasureSpec(size,
                View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(size,
                        View.MeasureSpec.EXACTLY))
        v.layout(0, 0, v.measuredWidth, v.measuredHeight)
        val returnedBitmap = Bitmap.createBitmap(v.measuredWidth,
                v.measuredHeight, Bitmap.Config.ARGB_8888)
        val c = Canvas(returnedBitmap)
        v.draw(c)

        return returnedBitmap
    }

}
