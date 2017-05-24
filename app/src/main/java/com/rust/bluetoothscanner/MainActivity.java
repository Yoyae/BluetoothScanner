package com.rust.bluetoothscanner;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.rust.btscanner.BtDeviceItem;
import com.rust.btscanner.BtScanner;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    BtScanner mScanner = new BtScanner(3000);
    TextView mTv;
    Button mScanBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTv = (TextView) findViewById(R.id.textView);
        mScanner.setScanPeriod(50000);
        mScanner.setNotifyInterval(500);
        mScanner.addListener(new BtScanner.Listener() {
            @Override
            public void onDeviceListUpdated(ArrayList<BtDeviceItem> list) {
                mTv.setText("");
                for (BtDeviceItem item : list) {
                    mTv.append(item.toString());
                    mTv.append("\n");
                }
            }

            @Override
            public void onScanning(boolean scan) {
                mScanBtn.setText(scan ? "Scanning" : "Scan");
            }
        });


        mScanBtn = (Button) findViewById(R.id.searchBtn);
        mScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mScanner.isScanning()) {
                    mScanner.stopScan();
                } else {
                    mScanner.startScan();
                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mScanner.stopScan();
        mScanner.clearListener();
    }
}
