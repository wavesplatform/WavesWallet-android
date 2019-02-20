package com.wavesplatform.wallet.v1.ui.auth

class EnvironmentConstants {

    companion object {

        const val MAIN_NET_JSON = "{\n" +
                "  \"name\": \"Mainnet\",\n" +
                "  \"servers\": {\n" +
                "    \"nodeUrl\": \"https://nodes.wavesnodes.com\",\n" +
                "    \"dataUrl\": \"https://api.wavesplatform.com\",\n" +
                "    \"spamUrl\": \"https://github-proxy.wvservices.com/wavesplatform/waves-community/master/Scam%20tokens%20according%20to%20the%20opinion%20of%20Waves%20Community.csv\",\n" +
                "    \"matcherUrl\": \"https://matcher.wavesplatform.com\"\n" +
                "  },\n" +
                "  \"scheme\": \"W\",\n" +
                "  \"generalAssetIds\": [\n" +
                "    {\n" +
                "      \"iconUrls\": {\n" +
                "        \"default\": \"https://d1jh0rcszsaxik.cloudfront.net/assset_icons/logo_waves_48.png\"\n" +
                "      },\n" +
                "      \"assetId\": \"WAVES\",\n" +
                "      \"displayName\": \"Waves\",\n" +
                "      \"isFiat\": false,\n" +
                "      \"isGateway\": false,\n" +
                "      \"wavesId\": \"WAVES\",\n" +
                "      \"gatewayId\": \"WAVES\",\n" +
                "      \"addressRegEx\": \"\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"iconUrls\": {\n" +
                "        \"default\": \"https://d1jh0rcszsaxik.cloudfront.net/assset_icons/logo_bitcoin_48.png\"\n" +
                "      },\n" +
                "      \"assetId\": \"8LQW8f7P5d5PZM7GtZEBgaqRPGSzS3DfPuiXrURJ4AJS\",\n" +
                "      \"displayName\": \"Bitcoin\",\n" +
                "      \"isFiat\": false,\n" +
                "      \"isGateway\": true,\n" +
                "      \"wavesId\": \"WBTC\",\n" +
                "      \"gatewayId\": \"BTC\",\n" +
                "      \"addressRegEx\": \"^[13][a-km-zA-HJ-NP-Z1-9]{33}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"iconUrls\": {\n" +
                "        \"default\": \"https://d1jh0rcszsaxik.cloudfront.net/assset_icons/logo_ethereum_48.png\"\n" +
                "      },\n" +
                "      \"assetId\": \"474jTeYx2r2Va35794tCScAXWJG9hU2HcgxzMowaZUnu\",\n" +
                "      \"displayName\": \"Ethereum\",\n" +
                "      \"isFiat\": false,\n" +
                "      \"isGateway\": true,\n" +
                "      \"wavesId\": \"WETH\",\n" +
                "      \"gatewayId\": \"ETH\",\n" +
                "      \"addressRegEx\": \"^0x[a-fA-F0-9]{40}\$\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"iconUrls\": {\n" +
                "        \"default\": \"https://d1jh0rcszsaxik.cloudfront.net/assset_icons/logo_usd_48.png\"\n" +
                "      },\n" +
                "      \"assetId\": \"Ft8X1v1LTa1ABafufpaCWyVj8KkaxUWE6xBhW6sNFJck\",\n" +
                "      \"displayName\": \"US Dollar\",\n" +
                "      \"isFiat\": true,\n" +
                "      \"isGateway\": true,\n" +
                "      \"wavesId\": \"WUSD\",\n" +
                "      \"gatewayId\": \"USD\",\n" +
                "      \"addressRegEx\": \"\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"iconUrls\": {\n" +
                "        \"default\": \"https://d1jh0rcszsaxik.cloudfront.net/assset_icons/logo_euro_48.png\"\n" +
                "      },\n" +
                "      \"assetId\": \"Gtb1WRznfchDnTh37ezoDTJ4wcoKaRsKqKjJjy7nm2zU\",\n" +
                "      \"displayName\": \"Euro\",\n" +
                "      \"isFiat\": true,\n" +
                "      \"isGateway\": true,\n" +
                "      \"wavesId\": \"WEUR\",\n" +
                "      \"gatewayId\": \"EUR\",\n" +
                "      \"addressRegEx\": \"\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"iconUrls\": {\n" +
                "        \"default\": \"https://d1jh0rcszsaxik.cloudfront.net/assset_icons/logo_ltc_48.png\"\n" +
                "      },\n" +
                "      \"assetId\": \"HZk1mbfuJpmxU1Fs4AX5MWLVYtctsNcg6e2C6VKqK8zk\",\n" +
                "      \"displayName\": \"Litecoin\",\n" +
                "      \"isFiat\": false,\n" +
                "      \"isGateway\": true,\n" +
                "      \"wavesId\": \"WLTC\",\n" +
                "      \"gatewayId\": \"LTC\",\n" +
                "      \"addressRegEx\": \"^[LM3][a-km-zA-HJ-NP-Z1-9]{26,33}\$\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"iconUrls\": {\n" +
                "        \"default\": \"https://d1jh0rcszsaxik.cloudfront.net/assset_icons/logo_zec_48.png\"\n" +
                "      },\n" +
                "      \"assetId\": \"BrjUWjndUanm5VsJkbUip8VRYy6LWJePtxya3FNv4TQa\",\n" +
                "      \"displayName\": \"Zcash\",\n" +
                "      \"isFiat\": false,\n" +
                "      \"isGateway\": true,\n" +
                "      \"wavesId\": \"WZEC\",\n" +
                "      \"gatewayId\": \"ZEC\",\n" +
                "      \"addressRegEx\": \"^t1[a-zA-Z0-9]{33}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"iconUrls\": {\n" +
                "        \"default\": \"https://d1jh0rcszsaxik.cloudfront.net/assset_icons/logo_bitcoincash_48.png\"\n" +
                "      },\n" +
                "      \"assetId\": \"zMFqXuoyrn5w17PFurTqxB7GsS71fp9dfk6XFwxbPCy\",\n" +
                "      \"displayName\": \"Bitcoin Cash\",\n" +
                "      \"isFiat\": false,\n" +
                "      \"isGateway\": true,\n" +
                "      \"wavesId\": \"WBCH\",\n" +
                "      \"gatewayId\": \"BCH\",\n" +
                "      \"addressRegEx\": \"^[13][a-km-zA-HJ-NP-Z1-9]{33}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"iconUrls\": {\n" +
                "        \"default\": \"https://d1jh0rcszsaxik.cloudfront.net/assset_icons/logo_bitcoinsv_48.png\"\n" +
                "      },\n" +
                "      \"assetId\": \"62LyMjcr2DtiyF5yVXFhoQ2q414VPPJXjsNYp72SuDCH\",\n" +
                "      \"displayName\": \"Bitcoin SV\",\n" +
                "      \"isFiat\": false,\n" +
                "      \"isGateway\": true,\n" +
                "      \"wavesId\": \"WBSV\",\n" +
                "      \"gatewayId\": \"BSV\",\n" +
                "      \"addressRegEx\": \"^[13][a-km-zA-HJ-NP-Z1-9]{33}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"iconUrls\": {\n" +
                "        \"default\": \"https://d1jh0rcszsaxik.cloudfront.net/assset_icons/logo_lira_48.png\"\n" +
                "      },\n" +
                "      \"assetId\": \"2mX5DzVKWrAJw8iwdJnV2qtoeVG9h5nTDpTqC1wb1WEN\",\n" +
                "      \"displayName\": \"TRY\",\n" +
                "      \"isFiat\": true,\n" +
                "      \"isGateway\": true,\n" +
                "      \"wavesId\": \"WTRY\",\n" +
                "      \"gatewayId\": \"TRY\",\n" +
                "      \"addressRegEx\": \"\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"iconUrls\": {\n" +
                "        \"default\": \"https://d1jh0rcszsaxik.cloudfront.net/assset_icons/logo_dash_48.png\"\n" +
                "      },\n" +
                "      \"assetId\": \"B3uGHFRpSUuGEDWjqB9LWWxafQj8VTvpMucEyoxzws5H\",\n" +
                "      \"displayName\": \"DASH\",\n" +
                "      \"isFiat\": false,\n" +
                "      \"isGateway\": true,\n" +
                "      \"wavesId\": \"WDASH\",\n" +
                "      \"gatewayId\": \"DASH\",\n" +
                "      \"addressRegEx\": \"^X[1-9A-HJ-NP-Za-km-z]{33}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"iconUrls\": {\n" +
                "        \"default\": \"https://d1jh0rcszsaxik.cloudfront.net/assset_icons/logo_monero_48.png\"\n" +
                "      },\n" +
                "      \"assetId\": \"5WvPKSJXzVE2orvbkJ8wsQmmQKqTv9sGBPksV4adViw3\",\n" +
                "      \"displayName\": \"Monero\",\n" +
                "      \"isFiat\": false,\n" +
                "      \"isGateway\": true,\n" +
                "      \"wavesId\": \"WXMR\",\n" +
                "      \"gatewayId\": \"XMR\",\n" +
                "      \"addressRegEx\": \"^4[0-9AB][1-9A-HJ-NP-Za-km-z]{93}\"\n" +
                "    }\n" +
                "  ]\n" +
                "}"

        const val TEST_NET_JSON = "{\n" +
                "  \"name\": \"Testnet\",\n" +
                "  \"servers\": {\n" +
                "    \"nodeUrl\": \"https://testnet1.wavesnodes.com\",\n" +
                "    \"dataUrl\": \"https://api.testnet.wavesplatform.com\",\n" +
                "    \"spamUrl\": \"https://github-proxy.wvservices.com/wavesplatform/waves-community/master/Scam%20tokens%20according%20to%20the%20opinion%20of%20Waves%20Community.csv\",\n" +
                "    \"matcherUrl\": \"https://matcher.testnet.wavesnodes.com\"\n" +
                "  },\n" +
                "  \"scheme\": \"T\",\n" +
                "  \"generalAssetIds\": [\n" +
                "    {\n" +
                "      \"assetId\": \"WAVES\",\n" +
                "      \"displayName\": \"Waves\",\n" +
                "      \"isFiat\": false,\n" +
                "      \"isGateway\": false,\n" +
                "      \"wavesId\": \"WAVES\",\n" +
                "      \"gatewayId\": \"WAVES\",\n" +
                "      \"addressRegEx\": \"\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"assetId\": \"DWgwcZTMhSvnyYCoWLRUXXSH1RSkzThXLJhww9gwkqdn\",\n" +
                "      \"displayName\": \"Bitcoin\",\n" +
                "      \"isFiat\": false,\n" +
                "      \"isGateway\": true,\n" +
                "      \"wavesId\": \"WBTC\",\n" +
                "      \"gatewayId\": \"BTC\",\n" +
                "      \"addressRegEx\": \"^[13][a-km-zA-HJ-NP-Z1-9]{33}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"assetId\": \"BrmjyAWT5jjr3Wpsiyivyvg5vDuzoX2s93WgiexXetB3\",\n" +
                "      \"displayName\": \"Ethereum\",\n" +
                "      \"isFiat\": false,\n" +
                "      \"isGateway\": true,\n" +
                "      \"wavesId\": \"WETH\",\n" +
                "      \"gatewayId\": \"ETH\",\n" +
                "      \"addressRegEx\": \"^0x[a-fA-F0-9]{40}\$\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"assetId\": \"D6N2rAqWN6ZCWnCeNFWLGqqjS6nJLeK4m19XiuhdDenr\",\n" +
                "      \"displayName\": \"US Dollar\",\n" +
                "      \"isFiat\": true,\n" +
                "      \"isGateway\": true,\n" +
                "      \"wavesId\": \"WUSD\",\n" +
                "      \"gatewayId\": \"USD\",\n" +
                "      \"addressRegEx\": \"\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"assetId\": \"AsuWyM9MUUsMmWkK7jS48L3ky6gA1pxx7QtEYPbfLjAJ\",\n" +
                "      \"displayName\": \"Euro\",\n" +
                "      \"isFiat\": true,\n" +
                "      \"isGateway\": true,\n" +
                "      \"wavesId\": \"WEUR\",\n" +
                "      \"gatewayId\": \"EUR\",\n" +
                "      \"addressRegEx\": \"\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"assetId\": \"BNdAstuFogzSyN2rY3beJbnBYwYcu7RzTHFjW88g8roK\",\n" +
                "      \"displayName\": \"Litecoin\",\n" +
                "      \"isFiat\": false,\n" +
                "      \"isGateway\": true,\n" +
                "      \"wavesId\": \"WLTC\",\n" +
                "      \"gatewayId\": \"LTC\",\n" +
                "      \"addressRegEx\": \"^[LM3][a-km-zA-HJ-NP-Z1-9]{26,33}\$\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"assetId\": \"CFg2KQfkUgUVM2jFCMC5Xh8T8zrebvPc4HjHPfAugU1S\",\n" +
                "      \"displayName\": \"Zcash\",\n" +
                "      \"isFiat\": false,\n" +
                "      \"isGateway\": true,\n" +
                "      \"wavesId\": \"WZEC\",\n" +
                "      \"gatewayId\": \"ZEC\",\n" +
                "      \"addressRegEx\": \"^t1[a-zA-Z0-9]{33}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"assetId\": \"8HT8tXwrXAYqwm8XrZ2hywWWTUAXxobHB5DakVC1y6jn\",\n" +
                "      \"displayName\": \"Bitcoin Cash\",\n" +
                "      \"isFiat\": false,\n" +
                "      \"isGateway\": true,\n" +
                "      \"wavesId\": \"WBCH\",\n" +
                "      \"gatewayId\": \"BCH\",\n" +
                "      \"addressRegEx\": \"^[13][a-km-zA-HJ-NP-Z1-9]{33}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"assetId\": \"7itsmgdmomeTXvZzaaxqF3346h4FhciRoWceEw9asNV3\",\n" +
                "      \"displayName\": \"TRY\",\n" +
                "      \"isFiat\": true,\n" +
                "      \"isGateway\": true,\n" +
                "      \"wavesId\": \"WTRY\",\n" +
                "      \"gatewayId\": \"TRY\",\n" +
                "      \"addressRegEx\": \"\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"assetId\": \"DGgBtwVoXKAKKvV2ayUpSoPzTJxt7jo9KiXMJRzTH2ET\",\n" +
                "      \"displayName\": \"DASH\",\n" +
                "      \"isFiat\": false,\n" +
                "      \"isGateway\": true,\n" +
                "      \"wavesId\": \"WDASH\",\n" +
                "      \"gatewayId\": \"DASH\",\n" +
                "      \"addressRegEx\": \"^X[1-9A-HJ-NP-Za-km-z]{33}\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"assetId\": \"8oPbSCKFHkXBy1hCGSg9pJkSARE7zhTQTLpc8KZwdtr7\",\n" +
                "      \"displayName\": \"Monero\",\n" +
                "      \"isFiat\": false,\n" +
                "      \"isGateway\": true,\n" +
                "      \"wavesId\": \"WXMR\",\n" +
                "      \"gatewayId\": \"XMR\",\n" +
                "      \"addressRegEx\": \"^4[0-9AB][1-9A-HJ-NP-Za-km-z]{93}\"\n" +
                "    }\n" +
                "  ]\n" +
                "}"
    }
}