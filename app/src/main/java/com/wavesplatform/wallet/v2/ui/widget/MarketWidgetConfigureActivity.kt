/*
 * Created by Eduard Zaydel on 18/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.widget.AppCompatTextView
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import kotlinx.android.synthetic.main.market_widget_configure.*
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetStyle
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetUpdateInterval
import kotlinx.android.synthetic.main.market_widget_configure.tab_navigation
import pers.victor.ext.click


/**
 * The configuration screen for the [MarketWidget] AppWidget.
 */
class MarketWidgetConfigureActivity : BaseActivity(), TabLayout.OnTabSelectedListener {

    private var themeName = MarketWidgetStyle.CLASSIC
    private var intervalUpdate = MarketWidgetUpdateInterval.MIN_10
    private var assets = arrayListOf<String>()

    private val widgetId: Int by lazy {
        intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    override fun configLayoutRes(): Int = R.layout.market_widget_configure

    override fun askPassCode() = false

    override fun onViewReady(savedInstanceState: Bundle?) {
        setStatusBarColor(R.color.basic50)

        checkWidgetId()

        tab_navigation.addTab(tab_navigation.newTab().setCustomView(
                getCustomView(R.drawable.ic_widget_interval_22,
                        R.string.market_widget_config_interval)).setTag("set_interval"))
        tab_navigation.addTab(tab_navigation.newTab().setCustomView(
                getCustomView(R.drawable.ic_widget_addtoken_22,
                        R.string.market_widget_config_add_token)).setTag("add_token"))
        tab_navigation.addTab(tab_navigation.newTab().setCustomView(
                getCustomView(R.drawable.ic_widget_style_22,
                        R.string.market_widget_config_style)).setTag("set_style"))

        tab_navigation.addOnTabSelectedListener(this)

        toolbar_close.click {
            // It is the responsibility of the configuration activity to update the app widget
            val appWidgetManager = AppWidgetManager.getInstance(this)
            MarketWidget.updateWidget(this, appWidgetManager, widgetId)

            // Make sure we pass back the original appWidgetId
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        onTabSelected(tab)
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        // do nothing
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        when (tab?.position) {
            INTERVAL_TAB -> showIntervalDialog()
            ADD_TAB -> showAssetsDialog()
            THEME_TAB -> showThemeDialog()
        }
    }

    private fun getCustomView(tabIcon: Int, tabText: Int): View? {
        val customTab = LayoutInflater.from(this)
                .inflate(R.layout.content_widget_configure_navigation_tab, null)
        val imageTab = customTab.findViewById<ImageView>(R.id.image_tab)
        val textTab = customTab.findViewById<AppCompatTextView>(R.id.text_tab)

        imageTab.setImageResource(tabIcon)
        textTab.text = getString(tabText)

        return customTab
    }

    private fun checkWidgetId() {
        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(Activity.RESULT_CANCELED)

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
    }

    private fun showIntervalDialog() {
        val position = when(intervalUpdate) {
            MarketWidgetUpdateInterval.MIN_1 -> 0
            MarketWidgetUpdateInterval.MIN_5 -> 1
            MarketWidgetUpdateInterval.MIN_10 -> 2
            MarketWidgetUpdateInterval.MANUALLY -> 3
        }

        val optionDialog = OptionBottomSheetFragment.newInstance(
                arrayListOf(
                        getString(R.string.market_widget_config_interval_1_min),
                        getString(R.string.market_widget_config_interval_5_min),
                        getString(R.string.market_widget_config_interval_10_min),
                        getString(R.string.market_widget_config_interval_manually)),
                getString(R.string.market_widget_config_update_interval),
                position
        )
        optionDialog.onChangeListener = object : OptionBottomSheetFragment.OnChangeListener{
            override fun onChange(optionPosition: Int) {
                intervalUpdate = when(optionPosition) {
                    0 -> MarketWidgetUpdateInterval.MIN_1
                    1 -> MarketWidgetUpdateInterval.MIN_5
                    2 -> MarketWidgetUpdateInterval.MIN_10
                    3 -> MarketWidgetUpdateInterval.MANUALLY
                    else -> MarketWidgetUpdateInterval.MIN_10
                }
                MarketWidgetUpdateInterval.setInterval(
                        this@MarketWidgetConfigureActivity, widgetId, intervalUpdate)
            }
        }
        val ft = supportFragmentManager.beginTransaction()
        ft.add(optionDialog, optionDialog::class.java.simpleName)
        ft.commitAllowingStateLoss()
    }

    private fun showThemeDialog() {
        val position = when(themeName) {
            MarketWidgetStyle.CLASSIC -> 0
            MarketWidgetStyle.DARK -> 1
        }

        val optionDialog = OptionBottomSheetFragment.newInstance(
                arrayListOf(
                        getString(R.string.market_widget_config_classic),
                        getString(R.string.market_widget_config_dark)),
                getString(R.string.market_widget_config_widget_style),
                position
        )
        optionDialog.onChangeListener = object : OptionBottomSheetFragment.OnChangeListener{
            override fun onChange(optionPosition: Int) {
                themeName = when(optionPosition) {
                    0 -> MarketWidgetStyle.CLASSIC
                    1 -> MarketWidgetStyle.DARK
                    else -> MarketWidgetStyle.CLASSIC
                }
                MarketWidgetStyle.setTheme(
                        this@MarketWidgetConfigureActivity, widgetId, themeName)
            }
        }
        val ft = supportFragmentManager.beginTransaction()
        ft.add(optionDialog, optionDialog::class.java.simpleName)
        ft.commitAllowingStateLoss()
    }

    private fun showAssetsDialog() {
        val assetsDialog = AssetsBottomSheetFragment.newInstance(assets)
        val ft = supportFragmentManager.beginTransaction()
        ft.add(assetsDialog, assetsDialog::class.java.simpleName)
        ft.commitAllowingStateLoss()
        assetsDialog.onChooseListener = object : AssetsBottomSheetFragment.OnChooseListener {
            override fun onChoose(assets: ArrayList<String>) {
                this@MarketWidgetConfigureActivity.assets = assets

            }
        }
    }

    companion object {
        const val INTERVAL_TAB = 0
        const val ADD_TAB = 1
        const val THEME_TAB = 2
    }
}

