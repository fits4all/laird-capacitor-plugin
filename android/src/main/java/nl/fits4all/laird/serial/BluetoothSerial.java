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

import java.util.HashMap;

import nl.fits4all.laird.MainPlugin;


public class BluetoothSerial extends BluetoothSerialManager {

    private final String TAG = "BluetoothSerial";

    private BluetoothAdapter bluetoothAdapter;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private final HashMap<String, BluetoothDevice> devices = new HashMap<>();

    /**
     * Constructor.
     */
    public BluetoothSerial(MainPlugin plugin, Activity activity) {
        super(plugin, activity);

        if (bluetoothAdapter == null) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            Log.d(TAG, "Initialized bluetooth adapter.");
        }

        if (activity != null) {
            activity.registerReceiver(new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        if (device != null && device.getType() == BluetoothDevice.DEVICE_TYPE_LE) {
                            if (device.getName() != null && !device.getName().startsWith("AE-")) {
                                return;
                            }
                            devices.put(device.getAddress(), device);

                            Log.d(TAG, "Bluetooth device found:");
                            Log.d(TAG, " - Device name: " + device.getName());
                            Log.d(TAG, " - Device type: " + device.getType());
                            Log.d(TAG, " - Device address: " + device.getAddress());

                            JSObject js = new JSObject();
                            js.put("name", device.getName());
                            js.put("type", device.getType());
                            js.put("address", device.getAddress());
                            plugin.notifyCapacitorListeners("deviceFoundEvent", js);
                        }
                    }
                }
            }, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            Log.d(TAG, "Initialized bluetooth action listeners.");
        }
    }

    /**
     * Starts the discovering process of finding devices.
     */
    public void startDiscovering() {
        if (!bluetoothAdapter.isDiscovering()) {
            devices.clear(); // Clear list before discovering.
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

    /**
     * Connects to the address that was discovered.
     * @param address Address of discovered device.
     * @param secure Security.
     */
    public void connect(String address, boolean secure) {
        BluetoothDevice device = devices.get(address);

        if (device != null) {
            Log.d(null, "Connecting to bluetooth device.");
            connect(device, secure);
        } else {
            Log.d(null, "Could not connect to bluetooth device. Device not found.");
        }
    }

    /**
     * Disconnects the current connected device.
     */
    public void disconnect() {
        if (getConnectionState() == BluetoothProfile.STATE_CONNECTED) {
            disconnect();
        }
    }

    /**
     * Sends data to the bluetooth device.
     */
    public void sendData(String data) {
        if (getConnectionState() == BluetoothProfile.STATE_CONNECTED) {
            startDataTransfer(data);
        }
    }


}
