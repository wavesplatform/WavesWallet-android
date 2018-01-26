package com.wavesplatform.wallet.ui.auth;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.widget.ArrayAdapter;

import com.wavesplatform.wallet.R;
import com.wavesplatform.wallet.ui.customviews.ToastCustom;
import com.wavesplatform.wallet.util.AppRate;

import java.util.Arrays;
import java.util.List;

class EnvironmentSwitcher {

    private Context context;

    EnvironmentSwitcher(Context context) {
        this.context = context;
    }

    void showEnvironmentSelectionDialog() {
        List<String> itemsList = Arrays.asList("Production", "TestNet");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context, R.layout.item_environment_list, itemsList);

        EnvironmentManager.Environment environment = EnvironmentManager.get().current();
        int selection;
        switch (environment) {
            case TESTNET:
                selection = 1;
                break;
            default:
                selection = 0;
                break;
        }

        final EnvironmentManager.Environment[] selectedEnvironment = new EnvironmentManager.Environment[1];

        new AlertDialog.Builder(context, R.style.AlertDialogStyle)
                .setTitle("Choose Environment")
                .setSingleChoiceItems(adapter, selection, (dialogInterface, i) -> {
                    switch (i) {
                        case 1:
                            selectedEnvironment[0] = EnvironmentManager.Environment.TESTNET;
                            break;
                        default:
                            selectedEnvironment[0] = EnvironmentManager.Environment.PRODUCTION;
                            break;
                    }
                })
                .setPositiveButton("Select", (dialog, id) -> {
                    EnvironmentManager.get().setCurrent(
                            selectedEnvironment[0] != null ? selectedEnvironment[0] : EnvironmentManager.Environment.PRODUCTION);

                    ToastCustom.makeText(
                            context,
                            "Environment set to " + EnvironmentManager.get().current().getName(),
                            ToastCustom.LENGTH_SHORT,
                            ToastCustom.TYPE_OK);
                })
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }

    private void resetAllTimers() {

        AppRate.reset(context);

        ToastCustom.makeText(context, "Timers reset", ToastCustom.LENGTH_SHORT, ToastCustom.TYPE_OK);
    }

}
