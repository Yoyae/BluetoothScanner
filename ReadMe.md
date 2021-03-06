# Android Bluetooth scanner

Make bluetooth more easier. Min SDK 18. Easy to use from LL to M.
```
compile 'com.rustfisher.hardware:bt-scanner:1.0.1'
```

Use BtScanner to search bluetooth device.

#### New a scanner
```java
BtScanner mScanner = new BtScanner(3000);  // Input scan peroid (ms)
// or use defult constructor BtScanner()
```

#### Add scan result listener
```java
mScanner.addListener(new BtScanner.Listener() {
            @Override
            public void onDeviceListUpdated(ArrayList<BtDeviceItem> list) {
                // do something..
            }

            @Override
            public void onScanning(boolean scan) {
                // scan status
            }
        });

mScanner.startScan(); // start scan
mScanner.stopScan();  // stop scan
```

#### Some configs
```java
mScanner.setScanPeriod(50000);      // Scan time: 50000 ms
mScanner.setNotifyInterval(500);    // Notify scan result every 500 ms
mScanner.setLoadBondDevice(false);  // Output the bonded device ?
```

Clear listener when your work done. 
Call this in `Activity.onDestroy()` or somewhere else.
```java
mScanner.clearListener();
```

Add `ScanFilter` and set `ScanSettings`  
Note that SDK version is LL or later
```java
    if (BtScanner.sdkLLOrLater()) {
        mScanner.addScanFilter(new ScanFilter.Builder()
                .setDeviceName("XXX")
                .setDeviceAddress("12:34:56:78:90:EA")
                .build());

        mScanner.setScanSettings(new ScanSettings.Builder()
                .setReportDelay(100)
                .build());
    }
```

### Source code

Use different API by Android SDK version 
```java
    if (laterThanLL()) {
        bleScanner.startScan(bleScanCallback);
        Log.d(TAG, "bleScanner startScan");
    } else {
        btAdapter.startLeScan(defBleScanCallback);
        Log.d(TAG, "btAdapter startLeScan");
    }

    private static boolean laterThanLL() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
```

A `NotifyScanListThread` would output the scan result.

### About this demo

![demo](https://raw.githubusercontent.com/RustFisher/BluetoothScanner/master/ref/d1.png)


