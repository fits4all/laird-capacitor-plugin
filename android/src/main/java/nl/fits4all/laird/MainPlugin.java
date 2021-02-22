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

import org.json.JSONException;

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

    private BluetoothSerial serial;

    @Override
    protected void handleOnStart() {
        super.handleOnStart();

        serial = new BluetoothSerial(this, getActivity());
    }

    @Override
    protected void handleOnDestroy() {
        super.handleOnDestroy();
    }

    @PluginMethod
    public void startDiscovering(PluginCall call) {
        Log.d(null, "Triggered method startDiscovering()");
        serial.startDiscovering();
        JSObject js = new JSObject();
        js.put("status", "Discovering has started.");
        call.resolve(js);
    }

    @PluginMethod
    public void cancelDiscovering(PluginCall call) {
        Log.d(null, "Triggered method cancelDiscovering()");
        serial.cancelDiscovering();
        JSObject js = new JSObject();
        js.put("status", "Discovering has been canceled.");
        call.resolve(js);
    }

    @PluginMethod
    public void connectToDevice(PluginCall call) {
        Log.d(null, "Triggered method connectToDevice()");

        if (!call.getData().has("address")) {
            call.reject("Did not specify address parameter.");
            return;
        }

        serial.connect(call.getData().getString("address"), true);
        JSObject js = new JSObject();
        js.put("status", "Connected to bluetooth device.");
        call.resolve(js);
    }

    @PluginMethod
    public void disconnectFromDevice(PluginCall call) {
        Log.d(null, "Triggered method disconnectFromDevice()");

        serial.disconnect();
        JSObject js = new JSObject();
        js.put("status", "Disconnected from bluetooth device.");
        call.resolve(js);
    }

    @PluginMethod
    public void sendDataToDevice(PluginCall call) {
        Log.d(null, "Triggered method sendDataToDevice()");

        if (!call.getData().has("data")) {
            call.reject("Did not specify data parameter.");
            return;
        }

        serial.sendData(call.getString("data"));
        JSObject js = new JSObject();
        js.put("status", "Successfully sent data to device.");
        call.resolve(js);
    }

    /**
     * Notifies capacitors event listeners.
     * @param eventName EventName
     * @param js Javascript Object
     */
    public void notifyCapacitorListeners(String eventName, JSObject js) {
        Log.d(null, "Triggered listener " + eventName);
        this.notifyListeners(eventName, js);
    }

}
