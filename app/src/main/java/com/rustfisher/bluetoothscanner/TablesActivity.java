package com.rustfisher.bluetoothscanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.rustfisher.btscanner.BtDeviceItem;

public class TablesActivity extends Activity {

    Button mBtnScanner;
    TextView mTvName;
    TextView mTvAdrMac;
    TextView mTvRSI;

    String[] namesTables;
    String[] adrTables;
    String[] rssiTables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tables);

        Bundle bundle = getIntent().getExtras();
        namesTables = bundle.getStringArray("namestables");
        adrTables = bundle.getStringArray("adrtables");
        rssiTables = bundle.getStringArray("rssitables");

        mTvName = (TextView) findViewById(R.id.textViewNameTables);
        mTvAdrMac = (TextView) findViewById(R.id.textViewAdrMacTables);
        mTvRSI = (TextView) findViewById(R.id.textViewRSITables);
        mBtnScanner = (Button) findViewById(R.id.scannerBtn);

        mBtnScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainActivity();
            }
        });

        updateData();
    }

    private void updateData() {
        mTvName.setText("");
        mTvAdrMac.setText("");
        mTvRSI.setText("");

        for (String item : namesTables) {
            mTvName.append(item);
            mTvName.append("\n");
        }
        for (String item : adrTables) {
            mTvAdrMac.append(item);
            mTvAdrMac.append("\n");
        }
        for (String item : rssiTables) {
            mTvRSI.append(item);
            mTvRSI.append("\n");
        }
    }

    public void openMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
