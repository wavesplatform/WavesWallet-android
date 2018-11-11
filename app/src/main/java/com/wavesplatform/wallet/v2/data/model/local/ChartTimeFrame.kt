package com.wavesplatform.wallet.v2.data.model.local

import com.wavesplatform.wallet.R
import pers.victor.ext.app


enum class ChartTimeFrame(val timeUI: String, val timeServer: Int) {
    FIVE_MINUTES(app.getString(R.string.chart_time_frame_5_min), 5),
    FIFTEEN_MINUTES(app.getString(R.string.chart_time_frame_15_min), 15),
    THIRTY_MINUTES(app.getString(R.string.chart_time_frame_30_min), 30),
    ONE_HOUR(app.getString(R.string.chart_time_frame_1_hour), 60),
    FOUR_HOURS(app.getString(R.string.chart_time_frame_4_hours), 240),
    TWENTY_FOUR_HOURS(app.getString(R.string.chart_time_frame_24_hours), 1440);

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
