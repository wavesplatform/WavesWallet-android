/*
 * Created by Eduard Zaydel on 18/7/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.ui.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.PopupMenu
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import kotlinx.android.synthetic.main.market_widget_configure.*
import com.wavesplatform.wallet.v2.ui.widget.model.MarketWidgetStyle
import pers.victor.ext.click


/**
 * The configuration screen for the [MarketWidget] AppWidget.
 */
class MarketWidgetConfigureActivity : BaseActivity() {

    private val widgetId: Int by lazy {
        intent.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                ?: AppWidgetManager.INVALID_APPWIDGET_ID
    }

    private val menu: PopupMenu by lazy {
        PopupMenu(this, edit_theme)
                .apply {
                    setOnMenuItemClickListener { item ->
                        val themeName = item.title.toString()

                        edit_theme.setText(themeName)
                        MarketWidgetStyle.setTheme(this@MarketWidgetConfigureActivity, widgetId, themeName)

                        true
                    }

                    MarketWidgetStyle.values().forEach { theme ->
                        menu.add(theme.name)
                    }
                }

    }

    override fun configLayoutRes(): Int = R.layout.market_widget_configure

    override fun askPassCode() = false

    override fun onViewReady(savedInstanceState: Bundle?) {
        checkWidgetId()

        add_button.click {
            // It is the responsibility of the configuration activity to update the app widget
            val appWidgetManager = AppWidgetManager.getInstance(this)
            MarketWidget.updateWidget(this, appWidgetManager, widgetId)

            // Make sure we pass back the original appWidgetId
            val resultValue = Intent()
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }

        edit_theme.setText(MarketWidgetStyle.getTheme(this, widgetId).name)
        edit_theme.click {
            menu.show()
        }
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
}

