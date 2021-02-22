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

import nl.fits4all.laird.serial.BluetoothSerial;

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

    private final BluetoothSerial serial = new BluetoothSerial();

    @Override
    protected void handleOnStart() {
        super.handleOnStart();
    }

    @Override
    protected void handleOnDestroy() {
        super.handleOnDestroy();
    }

    @PluginMethod
    public void startDiscovering(PluginCall call) {
        Log.d(null, "Triggered method startDiscovering()");
        serial.startDiscovering();
        call.resolve();
    }

    @PluginMethod
    public void cancelDiscovering(PluginCall call) {
        Log.d(null, "Triggered method cancelDiscovering()");
        serial.cancelDiscovering();
        call.resolve();
    }

}
