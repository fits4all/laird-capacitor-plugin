package nl.fits4all.laird.serial.bt;

import android.bluetooth.BluetoothDevice;

/**
 * Scanning/Stopping and finding BT classic and BLE devices callback's.
 * implement this interface in the class that you want to get this callback's.
 */
public interface BluetoothAdapterHelperCallback {

	/**
	 * Callback that is indicating that a BLE scanning process has started.
	 */
	void onBleStartScan();

	/**
	 * Callback that is indicating that a BLE scanning process has stopped.
	 */
	void onBleStopScan();

	/**
	 * Callback reporting a BLE device found during a scan that was initiated.
	 * 
	 * @param device
	 *            Identifies the remote device
	 * @param rssi
	 *            The RSSI value for the remote device as reported by the
	 *            Bluetooth hardware. 0 if no RSSI value is available.
	 * @param scanRecord
	 * 			  Used the check UUIDs of a device for verification.
	 */
	void onBleDeviceFound(BluetoothDevice device, int rssi,	byte[] scanRecord);
}