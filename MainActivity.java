package com.example.bluetoothscanner;

import android.Manifest;//check permissions and access other manifest-related information programmatically.
import android.bluetooth.BluetoothAdapter;//controlling Bluetooth functionality such as enabling or disabling Bluetooth, querying paired devices, etc.
import android.bluetooth.BluetoothDevice;// provides methods to query information about the device and to establish Bluetooth connections.
import android.bluetooth.le.BluetoothLeScanner;//provides methods for Bluetooth Low Energy (BLE) scanning operations. It allows your app to scan for nearby BLE devices.
import android.bluetooth.le.ScanCallback;//receiving callbacks during BLE scan operations. You would typically implement this interface to handle scan results.
import android.bluetooth.le.ScanResult;//represents the result of a BLE scan operation. It contains information about a scanned BLE device, such as its Bluetooth device object and signal strength.
import android.content.Context;//accessing system services and managing app resources.
import android.content.Intent;//start activities, services, or deliver broadcasts between components of an Android application.
import android.content.pm.PackageManager;// check for permissions and package information.
import android.graphics.Color;//represent and manipulate colors in various formats.
import android.location.LocationManager;//querying and managing location providers (e.g., GPS, network) and obtaining location updates.
import android.os.Build;// build-related information about the device, such as the device manufacturer, model, and Android version.
import android.os.Bundle;// pass data between Android components, such as activities and fragments. Bundles are commonly used for passing parameters when starting activities or for saving instance states.
import android.os.Handler;//schedule messages and runnables to be executed at some point in the future. Handlers are often used for performing operations on the UI thread from background threads.
import android.os.Looper;// used with a Handler to handle messages and runnables on a specific thread.
import android.provider.Settings;//allows your app to open specific settings screens, such as Wi-Fi, Bluetooth, or Location settings.
import android.widget.ArrayAdapter;//bind an Array or List of data to views (such as ListView). It's commonly used with ListView, Spinner, or AutoCompleteTextView to display data.
import android.widget.Button;// push-button widget in Android UI
import android.widget.ListView;//view group that displays a list of scrollable items. It's commonly used to display large datasets efficiently.
import android.widget.Toast;//show short-duration messages (toasts) to the user. It's often used to display informative or error messages.
import androidx.annotation.RequiresApi;//indicate that a certain API should be used only on devices running a specific API level or higher
import androidx.appcompat.app.AppCompatActivity;//used as the base class for activities in apps that target versions of Android earlier than API level 11.
import androidx.core.app.ActivityCompat;//request permissions at runtime and handle permission callbacks.
import androidx.core.content.ContextCompat;//
import java.util.ArrayList;// create dynamic arrays that can grow or shrink in size as needed.
import java.util.Collections;// sorting, searching, and manipulating collections.
import java.util.Map;//collection of key-value pairs. Maps are used to store and retrieve data based on keys. In your code, it's used to store unique devices and their information.
import java.util.TreeMap;//stores key-value pairs in a sorted order based on the natural ordering of its keys or a comparator provided at map creation time. In your code, it's used to store scanned devices in a sorted order based on timestamps.

@RequiresApi(api = Build.VERSION_CODES.S)//specify the minimum API level required for certain code to run.annotated code is only applicable for devices running Android version "S"
public class MainActivity extends AppCompatActivity//inheritance, offers additional features and compatibility for activities in Android apps, particularly for older versions of Android.
{
    private int REQUEST_ENABLE_BLUETOOTH = 1;//An integer used as a request code for enabling Bluetooth.
    private Handler mHandler;//class type object//A handler used to schedule tasks to be executed on the main thread.
    private int permissionIndex = 0;//An integer used to track the index of requested permissions.
    private  int REQUEST_CODE = 1;
    private String[] permissions = {//array of strings (android permission)//An array of strings representing permissions required for Bluetooth and location access.
            Manifest.permission.BLUETOOTH,//app to communicate with paired Bluetooth devices.
            Manifest.permission.BLUETOOTH_ADMIN,// perform Bluetooth administration tasks, such as turning Bluetooth on/off and pairing with devices.
            Manifest.permission.BLUETOOTH_CONNECT,// initiate Bluetooth connections to paired devices.
            Manifest.permission.BLUETOOTH_SCAN,// perform Bluetooth scans to discover nearby devices.
            Manifest.permission.ACCESS_FINE_LOCATION,//access the device's precise location using GPS or other location sources.
            Manifest.permission.ACCESS_COARSE_LOCATION//access the device's approximate location using network-based methods.
    };
    private BluetoothAdapter bluetoothAdapter;//4 : An instance of BluetoothAdapter used to manage Bluetooth functionality.
    private BluetoothLeScanner bluetoothLeScanner;//6  An instance of BluetoothLeScanner used for BLE scanning operations.
    private Button scanButton;//20 A Button UI component used to start or stop scanning for BLE devices.
    private ListView listView;//21 A ListView UI component used to display scanned device information.
    private boolean isScanning = false;
    ArrayAdapter<String> adapter;//adapt an ArrayList or an array of objects into View items to be displayed in a ListView or other AdapterView.
    private int delay = 3000;//first scan delay
    private boolean first = true;//A boolean flag indicating whether it's the first scan update.
    private ArrayList<BluetoothDevice> devices = new ArrayList<>();//This line declares and initializes an ArrayList named devices that stores BluetoothDevice objects. An ArrayList is a dynamic array-like data structure that can dynamically grow or shrink in size as elements are added or removed. In this context, devices likely stores a list of Bluetooth devices detected during the scanning process.
    private Map<String, DeviceInfo> uniqueDevices = new TreeMap<>();//declares and initializes a TreeMap named uniqueDevices that maps String keys (device addresses) to DeviceInfo values. A TreeMap is a type of Map that maintains its entries in sorted order based on the natural ordering of its keys or a custom comparator. In this context, uniqueDevices likely stores information about unique Bluetooth devices detected during the scanning process, with each device identified by its address and associated with additional information stored in a DeviceInfo object.
    private Map<Long, Map<String, DeviceInfo>> scans = new TreeMap<>(Collections.reverseOrder());//declares and initializes a TreeMap named scans that maps Long keys (timestamps) to inner Map<String, DeviceInfo> values. The outer TreeMap sorts its entries based on the natural ordering of the keys in reverse order (i.e., descending order of timestamps). Each inner map (Map<String, DeviceInfo>) represents the devices detected during a specific scan, with the device addresses (String) mapped to corresponding DeviceInfo objects. In this context, scans likely stores information about multiple scans, with each scan identified by its timestamp and associated with the devices detected during that scan.

    private Runnable mRunnable = new Runnable() {//commonly used in multithreaded programming to represent code that needs to run concurrently with other code.handle tasks
        @Override
        public void run() {// abstract method called run(), which serves as the entry point for the task's execution. When you implement the Runnable interface, you need to provide an implementation for the run() method.
            addToListView();
            if (first) {
                mHandler.postDelayed(this, delay);
                first = false;
            } else
                mHandler.postDelayed(this, delay + 60000);
        }
    };

    private ScanCallback mScanCallback = new ScanCallback() {//private member variable named mScanCallback of type ScanCallback.scan results during BLE scanning
        @Override//method that follows it (onScanResult) is being overridden from the superclass (ScanCallback).
        public void onScanResult(int callbackType, ScanResult result) {//regular or batch scan. The specific values of callbackType are predefined constants provided by the Android framework., BluetoothDevice object representing the detected device, the received signal strength indication (RSSI), and the raw scan record data.
            BluetoothDevice device = result.getDevice();
            int rssi = result.getRssi();
            //byte[] scanRecord = result.getScanRecord().getBytes();//array contains the raw byte data of the scan record

            runOnUiThread(() -> {  //utility method provided by the Activity class in Android. It allows you to run a specified Runnable object on the UI thread. This is particularly useful when you need to update the user interface from a background thread.
                uniqueDevices.put(device.getAddress(), new DeviceInfo(rssi, device.getAddress(), System.currentTimeMillis()));
                // adds an entry to the uniqueDevices map. It associates the device address (device.getAddress()) with a new DeviceInfo object containing information such as the received signal strength (rssi), the device address again, and the current timestamp (System.currentTimeMillis()).
            });
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override//child class
    protected void onCreate(Bundle savedInstanceState) {//class-bundle,saved-variable of type bundle class
        super.onCreate(savedInstanceState);//super-parent class access
        setContentView(R.layout.activity_main);//creating layout in app

        requestNextPermission();//154

        scanButton = findViewById(R.id.ScanButton);//frontend and backend connection
        listView = findViewById(R.id.listView);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();//retrieves the default Bluetooth adapter associated with the device.
        if (bluetoothAdapter == null) {// indicating that the device does not support Bluetooth or Bluetooth is not enabled.
            finish();
            return;
        }
        if (!isLocationEnabled()) {//creates an intent to open the system settings screen for location services. It uses the action Settings.ACTION_LOCATION_SOURCE_SETTINGS to specify the location settings screen.
            Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);//Intent object is used to convey the intention of opening the device's location settings screen to the Android system.
            startActivity(enableLocationIntent);
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);//system displays a dialog prompting the user to enable Bluetooth. If the user agrees to enable Bluetooth by interacting with this dialog, the Bluetooth settings screen is displayed, allowing the user to toggle Bluetooth on. If Bluetooth is already enabled, nothing happens when this line is executed.
        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE not supported", Toast.LENGTH_SHORT).show();
            finish();
        }// check whether ble support or not

        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();//obtains an instance of BluetoothLeScanner from a BluetoothAdapter. This BluetoothLeScanner instance is necessary for scanning for BLE devices nearby. BLE scanning allows the application to discover and interact with nearby BLE devices.
        mHandler = new Handler(Looper.getMainLooper());//instance of handler,instance of Handler associated with the main thread's message queue. Handler instances are commonly used in Android for scheduling tasks to be executed on specific threads. Here, mHandler is created to handle tasks that need to run on the main thread, such as updating the UI.
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new ArrayList<>());//creates a new instance of ArrayAdapter. It's used to adapt a collection of data (in this case, an empty ArrayList) into views that can be displayed in a ListView. The adapter will use the simple layout android.R.layout.simple_list_item_1 to display each item in the list.
        listView.setAdapter(adapter);//sets the adapter (adapter) created in the previous line to the ListView (listView). By setting the adapter, you're telling the ListView to use the adapter to populate its list of items. Any changes made to the adapter's data will automatically reflect in the ListView
        //implement a message loop for a thread.
        scanButton.setOnClickListener(v -> {
            if (!isScanning) {
                startScan();
            } else {
                stopScan();
            }
            isScanning = !isScanning;
        });
    }
    private void startScan() {
        scanButton.setText("Stop Scan");
        int color = Color.parseColor("#FFF44336");
        scanButton.setBackgroundColor(color);
        bluetoothLeScanner.startScan(mScanCallback);//starts a BLE scan using the bluetoothLeScanner object. The startScan() method initiates a scan for BLE devices nearby. It requires a ScanCallback object (mScanCallback) as a parameter.
        mHandler.post(mRunnable);// posts a Runnable object (mRunnable) to be executed on a specific thread's message queue. The post() method of the Handler (mHandler) schedules the Runnable for execution.
    }
    private void stopScan() {
        scanButton.setText("Start Scan");
        int color = Color.parseColor("#FF4CAF50");
        scanButton.setBackgroundColor(color);
        mHandler.removeCallbacks(mRunnable);
        bluetoothLeScanner.stopScan(mScanCallback);
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    protected void onDestroy() {
        super.onDestroy();
        stopScan();
    }
    private void requestNextPermission() {
        if (permissionIndex < permissions.length) {
            if (ContextCompat.checkSelfPermission(this, permissions[permissionIndex]) != PackageManager.PERMISSION_GRANTED)//constant
                {
                ActivityCompat.requestPermissions(this, new String[]{permissions[permissionIndex]}, REQUEST_CODE++);
            } else {
                permissionIndex++;
                requestNextPermission();
            }
        }
    }
    private void addToListView() {//method is used to update the data displayed in a ListView
        adapter.clear();//This ensures that only the latest data is shown in the ListView and avoids duplication or inconsistency issues.

        if (uniqueDevices.size() == 0) {
            showInListView();
            return;
        }
        long timestamp = System.currentTimeMillis();//current time
        Map<String, DeviceInfo> uniqueDevicesCopy = new TreeMap<>(uniqueDevices);//copy deviceinfo objects map
        scans.put(timestamp, uniqueDevicesCopy);//adding data to a Map named scans
        showInListView();

    }
    private  void showInListView() {
        for (Map.Entry<Long, Map<String, DeviceInfo>> scan : scans.entrySet()) {//nested interface-single key-value
            long diff = System.currentTimeMillis() - scan.getKey();// time difference between the current system time (in milliseconds) and the timestamp associated with a specific entry in a map
            double diffInMinutesDouble = (double) diff / (1000 * 60);
            int diffInMinutes = (int) Math.floor(diffInMinutesDouble);


            String item = String.valueOf(diffInMinutes) + " min ago\n";
            for (Map.Entry<String, DeviceInfo> entry : scan.getValue().entrySet()) {
                String address = entry.getKey();//retrieves the Bluetooth device address (key) from the current entry (entry) in the map.
                DeviceInfo rssi = entry.getValue();// information about the Bluetooth device, such as its received signal strength (RSSI).

                item += rssi.getRssi() + " -- " + address + "\n";//appends to the item string the RSSI value, followed by "--", followed by the device address, and a newline character ("\n").
            }
            item += "\n";
            adapter.add(item);//adds the constructed item string to the adapter associated with the ListView
        }
        adapter.notifyDataSetChanged();// notifies the associated adapter that the underlying data set has changed. It prompts the adapter to update the displayed views in the associated ListView.changes made to the data will not be reflected in the associated view (e.g., ListView,
        uniqueDevices.clear();//It removes all key-value pairs from the map, effectively resetting it to an empty state.
        devices.clear();
    }
}
