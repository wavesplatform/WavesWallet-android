package com.wavesplatform.wallet.util;

interface PersistentPrefs {
    String GLOBAL_CURRENT_ENVIRONMENT = "global_current_environment";
    String GLOBAL_LOGGED_IN_GUID = "global_logged_in_wallet_guid";
    String GLOBAL_SCHEME_URL = "scheme_url";
    String LIST_WALLET_GUIDS = "list_wallet_guid";

    String KEY_WALLET_NAME = "wallet_name";
    String KEY_PUB_KEY = "wallet_public_key";
    String KEY_ENCRYPTED_WALLET = "encrypted_wallet";
    String KEY_ENCRYPTED_PASSWORD = "encrypted_password";
    String KEY_PIN_FAILS = "pin_fails";

    String KEY_AB_NAMES = "address_book_names";
    String KEY_AB_ADDRESSES = "address_book_addresses";

    String KEY_DISABLE_ROOT_WARNING = "disable_root_warning";
    String KEY_BACKUP_DATE_KEY = "backup_date_key";
    String KEY_LAST_BACKUP_PROMPT = "last_backup_prompt";
    String KEY_SECURITY_BACKUP_NEVER = "security_backup_never";
    String KEY_ENCRYPTED_PIN_CODE = "encrypted_pin_code";
    String KEY_SEND_USAGE_STATS = "send_usage_stats";

    String KEY_FINGERPRINT_ENABLED = "fingerprint_enabled";
    String KEY_SHARED_KEY = "sharedKey";
    String KEY_BALANCE_DISPLAY_STATE = "balance_display_state";
    String KEY_NEWLY_CREATED_WALLET = "newly_created_wallet";
    String LOGGED_OUT = "logged_out";
    String KEY_EVENT_2ND_PW = "event_2nd_pw";
    String KEY_RECEIVE_SHORTCUTS_ENABLED = "receive_shortcuts_enabled";
    String KEY_SWIPE_TO_RECEIVE_ENABLED = "swipe_to_receive_enabled";
    String KEY_DONT_ASK_AGAIN_ORDER = "dont_ask_again_order";

    String KEY_DISABLE_SPAM_FILTER = "disable_spam_filter";

}
