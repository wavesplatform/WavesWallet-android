/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local

import com.wavesplatform.wallet.R

enum class ChartTimeFrame(val timeUI: Int, val timeServer: Int, val interval: String) {
    FIVE_MINUTES(R.string.chart_time_frame_5_min, 5, "5m"),
    FIFTEEN_MINUTES(R.string.chart_time_frame_15_min, 15, "15m"),
    THIRTY_MINUTES(R.string.chart_time_frame_30_min, 30, "30m"),
    ONE_HOUR(R.string.chart_time_frame_1_hour, 60, "1h"),
    THREE_HOURS(R.string.chart_time_frame_3_hours, 180, "3h"),
    TWENTY_FOUR_HOURS(R.string.chart_time_frame_24_hours, 1440, "1d");

    companion object {
        fun findByServerTime(time: Int?): ChartTimeFrame? {
            return values().firstOrNull { it.timeServer == time }
        }

        fun findPositionByServerTime(time: Int?): Int {
            if (time == null) return 0
            return values().indexOfFirst { it.timeServer == time }
        }
    }
}
