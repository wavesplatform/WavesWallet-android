/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.data.model.local

import java.util.concurrent.atomic.AtomicInteger

object NotificationID {
    private val c = AtomicInteger(0)

    val id: Int
        get() = c.incrementAndGet()
}
