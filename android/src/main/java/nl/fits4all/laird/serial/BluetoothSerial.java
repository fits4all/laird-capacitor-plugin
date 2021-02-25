package nl.fits4all.laird.serial;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.getcapacitor.JSObject;

import java.util.Arrays;
import java.util.HashMap;

import nl.fits4all.laird.LairdCapacitorPlugin;
import nl.fits4all.laird.serial.bt.BluetoothAdapterHelper;
import nl.fits4all.laird.serial.bt.BluetoothAdapterHelperCallback;


public class BluetoothSerial implements BluetoothAdapterHelperCallback {

    private final String TAG = this.getClass().getName();

    private final LairdCapacitorPlugin plugin;
    private BluetoothAdapterHelper bluetoothAdapterHelper;
    private BluetoothSerialManager bluetoothSerialManager;
    private final HashMap<String, BluetoothDevice> devices = new HashMap<>();

    public BluetoothSerial(LairdCapacitorPlugin plugin, Activity activity) {
        this.plugin = plugin;

        if (bluetoothAdapterHelper == null) {
            bluetoothAdapterHelper = new BluetoothAdapterHelper(activity, this);
            Log.d(TAG, "Initialized bluetooth adapter.");
        }

        if (bluetoothSerialManager == null) {
            bluetoothSerialManager = new BluetoothSerialManager(plugin, activity);
            Log.d(TAG, "Initialized bluetooth manager.");
        }
    }

    /**
     * Starts the discovering process of finding devices.
     */
    public void startDiscovering(boolean periodically) {
        if (!bluetoothAdapterHelper.isDiscovering()) {
            devices.clear(); // Clear list before discovering.
            if (periodically) {
                bluetoothAdapterHelper.startBleScanPeriodically();
                Log.d(TAG, "Started discovering of bluetooth devices periodically.");
            } else {
                bluetoothAdapterHelper.startBleScan();
                Log.d(TAG, "Started discovering of bluetooth devices.");
            }
        } else {
            Log.d(TAG, "Could not start discovering bluetooth devices.");
        }
    }

    /**
     * Cancels the discovering process of finding devices.
     */
    public void cancelDiscovering() {
        if (bluetoothAdapterHelper.isDiscovering()) {
            bluetoothAdapterHelper.stopBleScan();
            Log.d(TAG, "Canceled discovering of bluetooth devices");
        } else {
            Log.d(TAG, "Could not cancel discovering bluetooth devices.");
        }
    }

    /**
     * Connects to the address that was discovered.
     * @param address Address of discovered device.
     * @param autoConnect AutoConnect of discovered device.
     */
    public void connect(String address, boolean autoConnect) {
        BluetoothDevice device = devices.get(address);

        if (device != null) {
            Log.d(TAG, "Connecting to bluetooth device.");
            bluetoothSerialManager.connect(device, autoConnect);
        } else {
            Log.d(TAG, "Could not connect to bluetooth device. Device not found.");
        }
    }

    /**
     * Disconnects the current connected device.
     */
    public void disconnect() {
        if (bluetoothSerialManager.getConnectionState() == BluetoothProfile.STATE_CONNECTED) {
            bluetoothSerialManager.disconnect();
        }
    }

    /**
     * Sends data to the bluetooth device.
     */
    public void sendData(String data) {
        if (bluetoothSerialManager.getConnectionState() == BluetoothProfile.STATE_CONNECTED) {
            bluetoothSerialManager.startDataTransfer(data);
        }
    }

    /**
     * Gets the bluetooth adapter.
     * @return bluetoothAdapter
     */
    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapterHelper.getBluetoothAdapter();
    }

    @Override
    public void onBleStartScan() {
        JSObject js = new JSObject();
        plugin.notifyCapacitorListeners("discoveryStartEvent", js);
    }

    @Override
    public void onBleStopScan() {
        JSObject js = new JSObject();
        plugin.notifyCapacitorListeners("discoveryStopEvent", js);
    }

    @Override
    public void onBleDeviceFound(BluetoothDevice device, int rssi, byte[] scanRecord) {
        JSObject js = new JSObject();
        js.put("name", device.getName());
        js.put("type", device.getType());
        js.put("address", device.getAddress());
        js.put("rssi", rssi);
        plugin.notifyCapacitorListeners("deviceFoundEvent", js);
    }
}
