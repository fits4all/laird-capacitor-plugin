package nl.fits4all.laird.serial.bt;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

/**
 * This class is a wrapper for the androids bluetooth low energy.
 * - BluetoothAdapter < 21 API
 * - BluetoothScanner >= 21 API
 */
public class BluetoothAdapterHelper {
	private final static String TAG = BluetoothAdapterHelper.class.getName();

	protected Activity mActivity;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothLeScanner mBluetoothScanner;
	private static BluetoothManager mBluetoothManager;
	private final BluetoothAdapterHelperCallback mBluetoothAdapterHelperCallback;

	private static long BLE_SCAN_TIMEOUT = 30 * 1000;
	private static long BLE_SCAN_PERIODICAL_INTERVAL = 1000;
	private final Handler mBleScanTimeout = new Handler();
	private final Handler mBleScanPeriodicalTimeout = new Handler();

	private boolean mIsBleScanning = false;
	private boolean mIsBleScanningPeriodically = false;

	/**
	 * @throws NullPointerException If one of the parameters is null
	 * @param activity Activity
	 * @param bluetoothAdapterHelperCallback Class to give callbacks to.
	 */
	public BluetoothAdapterHelper(Activity activity,
			BluetoothAdapterHelperCallback bluetoothAdapterHelperCallback) {
		if (activity == null || bluetoothAdapterHelperCallback == null) {
			throw new NullPointerException("Activity or BluetoothAdapterHelperCallback object passed is NULL");
		}

		mActivity = activity;
		mBluetoothAdapterHelperCallback = bluetoothAdapterHelperCallback;

		if (!initialize()) {
			throw new NullPointerException("Bluetooth hardware could not be initialized.");
		}
	}

	/**
	 * Initialize local Bluetooth hardware.
	 *
	 * BluetoothAdapter must be initialized successfully before doing any
	 * start/stop scanning operations. Starting a scan or stopping a scan
	 * with this should only be used for API version lower then 21.
	 *
	 * BluetoothLeScanner must be initialized successfully from the adapter
	 * before doing any start/stop scanning operations. Starting a scan
	 * or stopping a scan with this should only be used for API version 21
	 * and higher.
	 *
	 * @return false if something went wrong with the initialization process
	 * 		   true if noting went wrong with the initialization process.
	 */
	private boolean initialize() {
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null)	{
				return false;
			}
		}

		// Use this to start scan and stop scan for API versions lower then 21
		if (mBluetoothAdapter == null) {
			mBluetoothAdapter = mBluetoothManager.getAdapter();
			if (mBluetoothAdapter == null)	{
				Log.e(TAG, "Bluetooth adapter could not be initialized.");
				return false;
			}
		}

		// Use this to start scan and stop scan for API versions 21 and higher.
		if (mBluetoothScanner == null) {
			mBluetoothScanner = mBluetoothAdapter.getBluetoothLeScanner();
			if (mBluetoothScanner == null)	{
				Log.e(TAG, "Bluetooth scanner could not be initialized.");
			}
		}

		return true;
	}

	/**
	 * Initializes the BLE scan process.
	 *
	 * Checks for API support and start a scan based on that. This
	 * means that it will support older device that don't have the
	 * scanner wrapper in the SDK.
	 *
	 * @return true, if the scan was started successfully without any problems.
	 */
	public boolean startBleScan() {
		startScan();

		boolean scanInitiatedSuccessfully = false;
		if (mBluetoothAdapter.isDiscovering()) {
			// Scanning was initiated successfully
			mIsBleScanning = true;
			scanInitiatedSuccessfully = true;
			bleStopScanTimeout();
		}

		mBluetoothAdapterHelperCallback.onBleStartScan();
		return scanInitiatedSuccessfully;
	}

	/**
	 * start a BLE scan operation that in the background it actually starts and
	 * stop the scanning every BLE_SCAN_PERIODICAL_INTERVAL,
	 * <p>
	 * The callback {@link BluetoothAdapterHelperCallback#onBleStopScan()} gets
	 * called when the {@link #stopBleScan()} method is called or when the
	 * BLE_SCAN_TIMEOUT runs out
	 *
	 * @return true, if the scan was started successfully, otherwise false
	 */
	public boolean startBleScanPeriodically() {
		boolean scanInitiatedSuccessfully = startBleScan();

		if (scanInitiatedSuccessfully) {
			// Scan was initiated successfully
			Log.i(TAG, "Scan for BLE devices periodically");
			bleStartScanPeriodically();
		}

		return scanInitiatedSuccessfully;
	}

	/**
	 * Initiates stop scanning operation, the onBleStopScan callback gets called
	 * whenever the BLE scanning operations stops/finishes by either calling
	 * this method or when the timeout of the BLE scanning operation runs out
	 */
	public void stopBleScan() {
		if (isBleScanning()) {
			stopScan();

			mIsBleScanning = false;
			mIsBleScanningPeriodically = false;
			mBleScanTimeout.removeCallbacksAndMessages(null);
			mBleScanPeriodicalTimeout.removeCallbacksAndMessages(null);
			mBluetoothAdapterHelperCallback.onBleStopScan();
		} else {
			Log.i(TAG, "BLE Scanning has already been stopped!");
		}
	}


	/**
	 * stops BLE scan operation after a pre-defined scan period.
	 */
	private void bleStopScanTimeout() {
		mBleScanTimeout.postDelayed(() -> {
			Log.i(TAG, "stopBleScan after timeout of BLE scanning");
			stopBleScan();
		}, BLE_SCAN_TIMEOUT);
	}

	/**
	 * Once this method is called after BLE_SCAN_PERIODICAL_INTERVAL runs out
	 * it's body code will be called. BLE scanning will be stopped, if
	 * mIsBleScanningPeriodically is true then it starts BLE scanning. <br>
	 * As long as the mIsBleScanningPeriodically is true then this method will
	 * be called recursively.
	 * <p>
	 *
	 * The callback {@link BluetoothAdapterHelperCallback#onBleStopScan()} gets
	 * called when the {@link #stopBleScan()} method is called or when the
	 * BLE_SCAN_TIMEOUT runs out
	 */
	private void bleStartScanPeriodically() {
		mIsBleScanningPeriodically = true;
		mBleScanPeriodicalTimeout.postDelayed(() -> {

			// While this is true it will continue starting the BLE scan
			// the mIsBleScanningPeriodically becomes false when the
			// BLE_SCAN_TIMEOUT runs out
			if (mIsBleScanningPeriodically)	{
				startScan();
				bleStartScanPeriodically();
			}
		}, BLE_SCAN_PERIODICAL_INTERVAL);
	}

	/**
	 * Starts the scanning process. Uses the checkApiSupport
	 * to check which adapter should be used.
	 */
	private void startScan() {
		if (checkApiSupport()) {
			mBluetoothScanner.startScan(scanCallback); // API version 21 and higher.
		} else {
			mBluetoothAdapter.startLeScan(mLeScanCallback); // API version lower then 21.
		}
	}

	/**
	 * Stops the scanning process. Uses the checkApiSupport
	 * to check which adapter should be used.
	 */
	private void stopScan() {
		if (checkApiSupport()) {
			mBluetoothScanner.stopScan(scanCallback); // API version 21 and higher.
		} else {
			mBluetoothAdapter.stopLeScan(mLeScanCallback); // API version lower then 21.
		}
	}

	/**
	 * Gets the bluetooth manager.
	 * @return BluetoothManager
	 */
	public BluetoothManager getBluetoothManager() {
		return mBluetoothManager;
	}

	/**
	 * Gets the bluetooth adapter.
	 * @return Bluetooth adapter
	 */
	public BluetoothAdapter getBluetoothAdapter() {
		return mBluetoothAdapter;
	}

	/**
	 * Gets the bluetooth scanner.
	 * @return Bluetooth scanner
	 */
	public BluetoothLeScanner getBluetoothScanner() {
		return mBluetoothScanner;
	}

	/**
	 * checking if a discovery is in progress, discovery scans for all Bluetooth
	 * device types
	 * 
	 * @return true if discovering
	 */
	public boolean isDiscovering() {
		return getBluetoothAdapter().isDiscovering();
	}

	/**
	 * checking if currently there is BLE scanning operation
	 * 
	 * @return true if scanning
	 */
	public boolean isBleScanning() {
		return mIsBleScanning;
	}

	/**
	 * get the scanning time of the BLE scan operation <br>
	 * note: default scanning time is 20 seconds if it hasn't been changed
	 * 
	 * @return the BLE scan timeout
	 */
	public long getBleScanTimeout() {
		return BLE_SCAN_TIMEOUT;
	}

	/**
	 * get the interval time between the BLE scans when using the periodical
	 * scan
	 * 
	 * @return the interval time
	 */
	public long getBleScanPeriodicalInterval() {
		return BLE_SCAN_PERIODICAL_INTERVAL;
	}

	/**
	 * make sure that potential BLE scanning will take no longer than
	 * scanningTimeout seconds. <br>
	 * note: default scanning time is 20 seconds if it hasn't been changed
	 * 
	 * @param scanTimeout Scan timeout
	 */
	public void setBleScanTimeout(long scanTimeout)	{
		BLE_SCAN_TIMEOUT = scanTimeout;
	}

	/**
	 * use it to set the interval for the {@link #startBleScanPeriodically()} or
	 * the {@link #startBleScanPeriodically()} methods
	 * 
	 * @param scanPeriodicalInterval
	 *            the time to set the interval to
	 */
	public void setBleScanPeriodicalInterval(long scanPeriodicalInterval) {
		BLE_SCAN_PERIODICAL_INTERVAL = scanPeriodicalInterval;
	}

	/**
	 * Before any action check if BT is turned ON and enabled, call this in
	 * onResume to be always sure that BT is ON when Your application is put
	 * into the foreground
	 * 
	 * <p>
	 * Note: Also always check if BT in enabled just before a scanning procedure
	 * starts. The user could have disabled BT without actually sending the
	 * activity to the background, if this happens then the onResume will not be
	 * called and the app would try to do BT stuff without BT being enabled.
	 * 
	 * @return boolean false if not currently enabled, true otherwise.
	 */
	public boolean isEnabled() {
		return mBluetoothAdapter.isEnabled();
	}

	/**
	 * Check if the device supports the correct API to use the adapter or le scanner.
	 * 
	 * @return false if not supported for le scanner.
	 * 		   true if supported for le scanner.
	 */
	public boolean checkApiSupport() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
	}

	private final ScanCallback scanCallback = new ScanCallback() {
		@Override
		public void onScanResult(int callbackType, ScanResult result) {
			Log.i(TAG, "BluetoothLeScanner: A BLE device was found.");
			mBluetoothAdapterHelperCallback.onBleDeviceFound(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
		}
	};

	private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
			Log.i(TAG, "BluetoothAdapter: A BLE device was found.");
			mBluetoothAdapterHelperCallback.onBleDeviceFound(device, rssi, scanRecord);
		}
	};
}