package com.wavesplatform.wallet.v2.data.model.local

import com.wavesplatform.wallet.R

enum class ChartTimeFrame(val timeUI: Int, val timeServer: Int) {
    FIVE_MINUTES(R.string.chart_time_frame_5_min, 5),
    FIFTEEN_MINUTES(R.string.chart_time_frame_15_min, 15),
    THIRTY_MINUTES(R.string.chart_time_frame_30_min, 30),
    ONE_HOUR(R.string.chart_time_frame_1_hour, 60),
    FOUR_HOURS(R.string.chart_time_frame_4_hours, 240),
    TWENTY_FOUR_HOURS(R.string.chart_time_frame_24_hours, 1440);

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
