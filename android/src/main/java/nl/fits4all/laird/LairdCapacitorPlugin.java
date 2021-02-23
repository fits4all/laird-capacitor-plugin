package nl.fits4all.laird;

import android.Manifest;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

import nl.fits4all.laird.serial.BluetoothSerial;

@CapacitorPlugin(
    name = "Laird",
    permissions = {
        @Permission(
            alias = "location",
            strings = {
                    Manifest.permission.ACCESS_FINE_LOCATION,
            }
        )
    }
)
public class LairdCapacitorPlugin extends Plugin {

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

    /**
     * Checks if location permission was granted to the application.
     * After this is done, a permission callback is triggered.
     *
     * {@link #requestDiscovering(PluginCall)}
     *
     * @param call PluginCall
     */
    @PluginMethod
    public void startDiscovering(PluginCall call) {
        Log.d(null, "Triggered method startDiscovering()");

        if (getPermissionState("location") != PermissionState.GRANTED) {
            requestAllPermissions(call, "requestDiscovering");
        } else {
            requestDiscovering(call);
        }
    }


    /**
     * Starts the discovering process if location permissions were granted.
     * Also checks if bluetooth is currently enabled. If both these are granted
     * and enabled, then the plugin will start discovering bluetooth devices in
     * range of the host device.
     *
     * @param call PluginCall.
     */
    @PermissionCallback
    public void requestDiscovering(PluginCall call) {
        Log.d(null, "Triggered permission callback requestDiscovering()");

        if (getPermissionState("location") != PermissionState.GRANTED) {
            call.reject("Location permission was denied.");
            return;
        }

        if (serial.getBluetoothAdapter() == null || !serial.getBluetoothAdapter().isEnabled()) {
            call.reject("Bluetooth is not enabled.");
            return;
        }

        serial.startDiscovering();
        JSObject js = new JSObject();
        js.put("status", "Discovering has started.");
        call.resolve(js);
    }

    /**
     * Cancels the discovering process.
     *
     * @param call PluginCall.
     */
    @PluginMethod
    public void cancelDiscovering(PluginCall call) {
        Log.d(null, "Triggered method cancelDiscovering()");
        serial.cancelDiscovering();
        JSObject js = new JSObject();
        js.put("status", "Discovering has been canceled.");
        call.resolve(js);
    }

    /**
     * Connects to the specified target bluetooth device. A address parameter
     * must be specified to start the connection with the bluetooth device.
     *
     * @param call PluginCall
     */
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

    /**
     * Disconnects the current connected bluetooth device.
     *
     * @param call PluginCall
     */
    @PluginMethod
    public void disconnectFromDevice(PluginCall call) {
        Log.d(null, "Triggered method disconnectFromDevice()");

        serial.disconnect();
        JSObject js = new JSObject();
        js.put("status", "Disconnected from bluetooth device.");
        call.resolve(js);
    }

    /**
     * Sends the current connected bluetooth device data. A data
     * parameter must be specified to start sending data to the bluetooth
     * device.
     *
     * @param call PluginCall
     */
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
     * Notifies capacitors event listeners. We wrap this because it's protected
     * and we want to use it in other classes to trigger custom events.
     *
     * @param eventName EventName
     * @param js Javascript Object
     */
    public void notifyCapacitorListeners(String eventName, JSObject js) {
        Log.d(null, "Triggered listener " + eventName);
        this.notifyListeners(eventName, js);
    }

}
