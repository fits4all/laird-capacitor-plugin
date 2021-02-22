package nl.fits4all.laird.serial;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import nl.fits4all.laird.MainPlugin;

public class BluetoothSerial {

    private final String TAG = "BluetoothSerial";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSerialService bluetoothSerialService;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    /**
     * Constructor.
     */
    public BluetoothSerial(Activity activity) {
        if (bluetoothAdapter == null) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Log.d(TAG, "Initialized bluetooth adapter.");
        }

        if (bluetoothSerialService == null) {
            bluetoothSerialService = new BluetoothSerialService(new BluetoothSerialHandler());
            Log.d(TAG, "Initialized bluetooth serial service.");
        }

        if (activity != null) {
            BroadcastReceiver mBluetoothActionFound = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        Log.d(TAG, "Device name: " + device.getName());
                        Log.d(TAG, "Device type: " + device.getType());
                        Log.d(TAG, "Device address: " + device.getAddress());
                    }
                }
            };

            activity.registerReceiver(mBluetoothActionFound, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            Log.d(TAG, "Initialized bluetooth action listeners.");
        }
    }

    /**
     * Starts the discovering process of finding devices.
     */
    public void startDiscovering() {
        if (!bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.startDiscovery();
            Log.d(null, "Started discovering of bluetooth devices");
        } else {
            Log.d(null, "Could not start discovering bluetooth devices.");
        }
    }

    /**
     * Cancels the discovering process of finding devices.
     */
    public void cancelDiscovering() {
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            Log.d(null, "Canceled discovering of bluetooth devices");
        } else {
            Log.d(null, "Could not cancel discovering bluetooth devices.");
        }
    }


}
