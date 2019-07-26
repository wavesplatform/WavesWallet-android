/*
 * Created by Eduard Zaydel on 1/4/2019
 * Copyright © 2019 Waves Platform. All rights reserved.
 */

package com.wavesplatform.wallet.v2.util.zxing;

import android.provider.ContactsContract;

public final class Contents {
    private Contents() {
    }

    public static final class Type {

     // Plain text. Use Intent.putExtra(DATA, string). This can be used for URLs too, but string
     // must include "http://" or "https://".
        public static final String TEXT = "TEXT_TYPE";

        // An email type. Use Intent.putExtra(DATA, string) where string is the email address.
        public static final String EMAIL = "EMAIL_TYPE";

        // Use Intent.putExtra(DATA, string) where string is the phone number to call.
        public static final String PHONE = "PHONE_TYPE";

        // An SMS type. Use Intent.putExtra(DATA, string) where string is the number to SMS.
        public static final String SMS = "SMS_TYPE";

        //public static final String CONTACT = "CONTACT_TYPE";

        public static final String LOCATION = "LOCATION_TYPE";

        private Type() {
        }
    }
}
