package nl.fits4all.laird;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;

@CapacitorPlugin(
    name = "Main",
    permissions = {
        @Permission(
            alias = "location",
            strings = {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION // Android 10+
            }
        ),
        @Permission(
            alias = "bluetooth",
            strings = {
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
            }
        )
    }
)
public class MainPlugin extends Plugin {

    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private boolean scanning = false;

    @Override
    public void load() {
        getActivity().registerReceiver(mBluetoothActionFound, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        getActivity().registerReceiver(mBluetoothStartedScan, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        getActivity().registerReceiver(mBluetoothFinishedScan, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
    }

    /**
     * Start the scanning process for finding bluetooth devices.
     * @param call PluginCall
     */
    @PluginMethod
    public void startScanningDevices(final PluginCall call) {
        if (scanning) {
            JSObject ret = new JSObject();
            ret.put("status", 1);
            ret.put("body", "The bluetooth adapter is already scanning for bluetooth devices.");
            call.resolve(ret);
            return;
        }

        Log.i(null, "Starting scanning of bluetooth devices");

        mBluetoothAdapter.startDiscovery();

        JSObject ret = new JSObject();
        ret.put("status", 0);
        ret.put("body", "The bluetooth adapter has started scanning for bluetooth devices.");
        call.resolve(ret);
    }

    /**
     * Stops the scanning process for finding bluetooth devices.
     * @param call PluginCall
     */
    @PluginMethod
    public void stopScanningDevices(final PluginCall call) {
        if (!scanning) {
            JSObject ret = new JSObject();
            ret.put("status", 1);
            ret.put("body", "The bluetooth adapter is not scanning for bluetooth devices.");
            call.resolve(ret);
            return;
        }

        Log.i(null, "Stopping scanning of bluetooth devices");

        mBluetoothAdapter.cancelDiscovery();
        getActivity().unregisterReceiver(mBluetoothActionFound);
        JSObject ret = new JSObject();
        ret.put("status", 0);
        ret.put("body", "The bluetooth adapter has stopped scanning for bluetooth devices.");
        call.resolve(ret);
    }

    /**
     * Event for receiving bluetooth devices.
     */
    private final BroadcastReceiver mBluetoothActionFound = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                JSObject ret = new JSObject();
                ret.put("deviceName", device.getName());
                ret.put("deviceType", device.getType());
                ret.put("deviceAddress", device.getAddress());
                // ret.put("deviceAllias", device.getAlias());
                Log.i(null, "Device found from scanning of bluetooth devices.");
                Log.i(null, device.getName());
                notifyListeners("deviceFoundEvent", ret);
            }
        }
    };

    /**
     * Event for turning on.
     */
    private final BroadcastReceiver mBluetoothStartedScan = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                scanning = true;
            }
        }
    };

    /**
     * Event for turning off.
     */
    private final BroadcastReceiver mBluetoothFinishedScan = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                scanning = false;
            }
        }
    };

}
