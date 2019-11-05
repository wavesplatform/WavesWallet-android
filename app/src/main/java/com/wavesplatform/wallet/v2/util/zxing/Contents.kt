/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.util.zxing

object Contents {

    object Type {

        // Plain text. Use Intent.putExtra(DATA, string). This can be used for URLs too, but string
        // must include "http://" or "https://".
        const val TEXT = "TEXT_TYPE"

        // An email type. Use Intent.putExtra(DATA, string) where string is the email address.
        const val EMAIL = "EMAIL_TYPE"

        // Use Intent.putExtra(DATA, string) where string is the phone number to call.
        const val PHONE = "PHONE_TYPE"

        // An SMS type. Use Intent.putExtra(DATA, string) where string is the number to SMS.
        const val SMS = "SMS_TYPE"

        //public static final String CONTACT = "CONTACT_TYPE";

        const val LOCATION = "LOCATION_TYPE"
    }
}
