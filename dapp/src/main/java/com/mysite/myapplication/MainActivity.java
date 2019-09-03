package com.mysite.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.wavesplatform.sdk.WavesSdk;
import com.wavesplatform.sdk.keeper.interfaces.KeeperCallback;
import com.wavesplatform.sdk.model.response.node.transaction.DataTransactionResponse;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton browser = findViewById(R.id.browser);
        browser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*WavesSdk
                        .keeper()
                        .send(this,
                                DataTransaction(mutableListOf()),
                                object :KeeperCallback<DataTransactionResponse> {
                    override fun onSuccess(result: KeeperResult.Success<DataTransactionResponse>) {
                        Timber.tag("KEEPERTEST").i(result.toString())
                    }

                    override fun onFailed(result: KeeperResult.Error) {
                        Timber.tag("KEEPERTEST").i(result.toString())
                    }
                })*/
            }
        });
    }
}
