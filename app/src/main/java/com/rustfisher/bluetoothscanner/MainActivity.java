package com.rustfisher.bluetoothscanner;


import android.Manifest;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.os.Build;
import android.widget.Toast;

import com.rustfisher.btscanner.BtDeviceItem;
import com.rustfisher.btscanner.BtScanner;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 1;
    private static final String TAG = "BTScannerApp";
    BtScanner mScanner = new BtScanner(3000);
    TextView mTvName;
    TextView mTvAdrMac;
    TextView mTvDistance;
    Button mBtnTables;
    Activity ScanningActivity;
    String[] namesTables;
    String[] adrTables;
    String[] rssiTables;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ScanningActivity = this;

        if (Build.VERSION.SDK_INT >= 23) {
            // Marshmallow+ Permission APIs
            checkRequestPermission();
        }

        mTvName = (TextView) findViewById(R.id.textViewName);
        mTvAdrMac = (TextView) findViewById(R.id.textViewAdrMac);
        mTvDistance = (TextView) findViewById(R.id.textViewDistance);

        mBtnTables = (Button) findViewById(R.id.tablesBtn);

        try {
            JSONArray jsonArrayName = new JSONArray(PreferenceManager
                    .getDefaultSharedPreferences(this).getString("namestables", null));
            JSONArray jsonArrayAdr = new JSONArray(PreferenceManager
                    .getDefaultSharedPreferences(this).getString("adrtables", null));
            JSONArray jsonArrayRssi = new JSONArray(PreferenceManager
                    .getDefaultSharedPreferences(this).getString("rssitables", null));

            namesTables = new String[jsonArrayName.length()];
            for (int i = 0; i < jsonArrayName.length(); i++) {
                namesTables[i] = jsonArrayName.getString(i);
            }
            adrTables = new String[jsonArrayAdr.length()];
            for (int i = 0; i < jsonArrayAdr.length(); i++) {
                adrTables[i] = jsonArrayAdr.getString(i);
            }
            rssiTables = new String[jsonArrayRssi.length()];
            for (int i = 0; i < jsonArrayRssi.length(); i++) {
                rssiTables[i] = jsonArrayRssi.getString(i);
            }

            Log.d("your JSON Array", namesTables.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (namesTables == null) {
            namesTables = new String[0];
            adrTables = new String[0];
            rssiTables = new String[0];
        }

        mBtnTables.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openTablesActivity();
            }
        });

        initScanner();

        mScanner.startScan();
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveData();
        Log.v("On Stop", "********************************");
    }

    private void initScanner() {
        mScanner.setLoadBondDevice(false);
        mScanner.setScanPeriod(5000);
        mScanner.setNotifyInterval(3000);
        if (BtScanner.sdkLLOrLater()) {
//            mScanner.addScanFilter(new ScanFilter.Builder()
//                    .setDeviceName("XXX")
//                    .setDeviceAddress("12:34:56:78:90:EA")
//                    .build());

            mScanner.setScanSettings(new ScanSettings.Builder()
                    .setReportDelay(100)
                    .build());
        }

        mScanner.addListener(new BtScanner.Listener() {
            @Override
            public void onDeviceListUpdated(ArrayList<BtDeviceItem> list) {
                Log.d(TAG, "update: " + list);
                updateText(list);
                updateArray(list);
            }

            @Override
            public void onScanning(boolean scan) {
                Log.d(TAG, "onScanning: " + scan);
            }
        });
    }

    private void updateText(ArrayList<BtDeviceItem> list) {
        mTvName.setText("");
        mTvAdrMac.setText("");
        mTvDistance.setText("");

        for (BtDeviceItem item : list) {
            mTvName.append(item.getName());
            mTvName.append("\n");
            //if(item.getAddress().contains("28:11:A5:20")){
            //    mTvName.setTextColor(Color.RED);
            //    mTvAdrMac.setTextColor(Color.RED);
            //    mTvDistance.setTextColor(Color.RED);
            //}else{
                mTvName.setTextColor(Color.DKGRAY);
                mTvAdrMac.setTextColor(Color.DKGRAY);
                mTvDistance.setTextColor(Color.DKGRAY);
            //}
            mTvAdrMac.append(item.getAddress());
            mTvAdrMac.append("\n");
            double mDistance = Math.round(estimateDistanceFromRssi(59.0,item.getRssi())*100.0)/100.0; //tx Power : hard coded power value. Usually ranges between -59 to -65;
            String mStrDistance;
            if(mDistance >= 60.0){ mStrDistance = ">60";} else {mStrDistance = ""+mDistance;}
            mTvDistance.append(""+mStrDistance +"(" + item.getRssi() +")");
            mTvDistance.append("\n");
        }
    }

    private void saveData(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear().apply();

        JSONArray jsonArray = new JSONArray();
        for (String item : namesTables) {
            jsonArray.put(item);
        }
        editor.putString("namestables", jsonArray.toString());

        jsonArray = new JSONArray();
        for (String item : adrTables) {
            jsonArray.put(item);
        }
        editor.putString("adrtables", jsonArray.toString());

        jsonArray = new JSONArray();
        for (String item : rssiTables) {
            jsonArray.put(item);
        }
        editor.putString("rssitables", jsonArray.toString());

        editor.commit();

    }


    private void updateArray(ArrayList<BtDeviceItem> list) {
        for (BtDeviceItem item : list) {

            List mlist = Arrays.asList(namesTables);
            // check if string exists in list
            if (!mlist.contains(item.getName())) {
                Log.d(TAG, "updateData: name " + item.getName());
                namesTables = pushArray(namesTables, item.getName());
                adrTables = pushArray(adrTables, item.getAddress());
                rssiTables = pushArray(rssiTables, "" + item.getRssi());
            }
        }
    }

    public static <T> T[] pushArray(T[] arr, T item) {
        T[] tmp = Arrays.copyOf(arr, arr.length + 1);
        tmp[tmp.length - 1] = item;
        return tmp;
    }

    public static <T> T[] popArray(T[] arr) {
        T[] tmp = Arrays.copyOf(arr, arr.length - 1);
        return tmp;
    }

    public void openTablesActivity(){
        Intent intent = new Intent(this, TablesActivity.class);
        Log.d(TAG, "toTablesActivity: names length " + namesTables.length);
        intent.putExtra("namestables", namesTables);
        intent.putExtra("adrtables", adrTables);
        intent.putExtra("rssitables", rssiTables);
        startActivity(intent);
    }

    protected static double estimateDistanceFromRssi(double txPower, double mRssi){
        if (mRssi == 0) {
            return -1.0; // if we cannot determine distance, return -1.
        }
        double ratio = mRssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double distance =  Math.pow(10,(ratio)/20.0);//(0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return distance;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);

                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);

                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    // All Permissions Granted
                    Toast.makeText(this, "All Permission GRANTED !! Thank You :)", Toast.LENGTH_SHORT)
                            .show();


                } else {
                    // Permission Denied
                    Toast.makeText(this, "One or More Permissions are DENIED Exiting App :(", Toast.LENGTH_SHORT)
                            .show();

                    finish();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void checkRequestPermission() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("ACCESS_FINE_LOCATION");
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_COARSE_LOCATION))
            permissionsNeeded.add("ACCESS_COARSE_LOCATION");
        if (!addPermission(permissionsList, Manifest.permission.BLUETOOTH))
            permissionsNeeded.add("BLUETOOTH");
        if (!addPermission(permissionsList, Manifest.permission.BLUETOOTH_ADMIN))
            permissionsNeeded.add("BLUETOOTH_ADMIN");
        if (permissionsList.size() > 0) {
            if (permissionsNeeded.size() > 0) {

                // Need Rationale
                String message = "App need access to " + permissionsNeeded.get(0);

                for (int i = 1; i < permissionsNeeded.size(); i++)
                    message = message + ", " + permissionsNeeded.get(i);

                showMessageOKCancel(message,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
                            }
                        });
                return;
            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {

        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }
}
