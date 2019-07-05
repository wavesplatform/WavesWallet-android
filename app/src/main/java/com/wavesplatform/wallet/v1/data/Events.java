/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v1.data;

public class Events {
    public static class NeedUpdateDataAfterPlaceOrder {

    }

    public static class ChangeTimeFrame{
        private String timeFrame;

        public ChangeTimeFrame(String timeFrame) {
            this.timeFrame = timeFrame;
        }

        public String getTimeFrame() {
            return timeFrame;
        }
    }
}
