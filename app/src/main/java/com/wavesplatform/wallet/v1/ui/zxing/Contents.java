package com.wavesplatform.wallet.v1.ui.zxing;

//
// * Copyright (C) 2008 ZXing authors
// * 
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * 
// * http://www.apache.org/licenses/LICENSE-2.0
// * 
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// 


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
