package com.wavesplatform.wallet.v2.ui.home.wallet.leasing.start

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.zxing.integration.android.IntentIntegrator
import com.wavesplatform.wallet.R
import com.wavesplatform.wallet.v2.ui.auth.import_account.ImportAccountActivity
import com.wavesplatform.wallet.v2.ui.auth.import_account.scan.ScanSeedFragment
import com.wavesplatform.wallet.v2.ui.auth.qr_scanner.QrCodeScannerActivity
import com.wavesplatform.wallet.v2.ui.base.view.BaseActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookActivity
import com.wavesplatform.wallet.v2.ui.home.profile.address_book.AddressBookUser
import com.wavesplatform.wallet.v2.ui.home.wallet.leasing.confirmation.ConfirmationLeasingActivity
import com.wavesplatform.wallet.v2.util.launchActivity
import kotlinx.android.synthetic.main.activity_start_leasing.*
import pers.victor.ext.click
import pers.victor.ext.toast

class StartLeasingActivity : BaseActivity(), StartLeasingView {

    companion object {
        var REQUEST_CHOOSE_ADDRESS = 57
    }

    override fun configLayoutRes(): Int = R.layout.activity_start_leasing

    override fun onViewReady(savedInstanceState: Bundle?) {
        setupToolbar(toolbar_view, View.OnClickListener { onBackPressed() }, true, getString(R.string.start_leasing_toolbar), R.drawable.ic_toolbar_back_black)

        text_choose_from_address.click {
            launchActivity<AddressBookActivity>(requestCode = REQUEST_CHOOSE_ADDRESS) {
                putExtra(AddressBookActivity.BUNDLE_SCREEN_TYPE, AddressBookActivity.AddressBookScreenType.CHOOSE.type)
            }
        }
        text_use_total_balance.click {
            toast("Total balance")
        }
        text_leasing_0_100.click {
            edit_amount.setText("0.100")
        }
        text_leasing_0_100000.click {
            edit_amount.setText("0.00100000")
        }
        text_leasing_0_500000.click {
            edit_amount.setText("0.00500000")
        }
        image_view_recipient_action.click {
            launchActivity<QrCodeScannerActivity> {  }
        }

        button_continue.click {
            launchActivity<ConfirmationLeasingActivity> { }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ScanSeedFragment.REQUEST_SCAN_QR_CODE -> {
                val result = IntentIntegrator.parseActivityResult(resultCode, data)

                if (result.contents == null) {
                    Log.d("MainActivity", "Cancelled scan")
                } else {
                    Log.d("MainActivity", "Scanned")
                    Toast.makeText(this, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
                    // TODO: Change to real scanned address
//                    launchActivity<ProtectAccountActivity> {
//                        putExtra(ProtectAccountActivity.BUNDLE_ACCOUNT_ADDRESS, "MkSuckMydickmMak1593x1GrfYmFdsf83skS11")
//                    }
                }
            }
            REQUEST_CHOOSE_ADDRESS -> {
                if (resultCode == Activity.RESULT_OK){
                    val addressTestObject = data?.getParcelableExtra<AddressBookUser>(AddressBookActivity.BUNDLE_ADDRESS_ITEM)
                    edit_address.setText(addressTestObject?.address)
                }
            }
        }
    }

}
