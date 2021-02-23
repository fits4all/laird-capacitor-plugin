package nl.fits4all.laird.serial.bt;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

/**
 * This class is a wrapper for the Android BluetoothAdapter class
 *
 * Responsible for:
 * - Initialising the local Bluetooth hardware</li>
 * - Checking if BT and BLE hardware is currently enabled</li>
 * - Checking if BLE central and/or peripheral modes are supported.
 * - Scanning for BT Classic and BLE device.
 *
 */
public class BluetoothAdapterHelper {
	private final static String TAG = BluetoothAdapterHelper.class.getName();

	protected Activity mActivity;
	private static BluetoothAdapter mBluetoothAdapter;
	private static BluetoothManager mBluetoothManager;
	private BluetoothAdapterHelperCallback mBluetoothAdapterHelperCallback;

	// how long the scan operation will take, default 30 seconds
	private static long BLE_SCAN_TIMEOUT = 30 * 1000;
	/*
	 * the BLE scan interval for stoppign and starting BLE scanning while doing
	 * a periodically scan. use this to get updated RSSI values from android
	 * devices that only fetch a remote device once in there callback with each
	 * BLE scan
	 */
	private static long BLE_SCAN_PERIODICAL_INTERVAL = 1000; // 1 second?
	private final Handler mBleScanTimeout = new Handler();
	private final Handler mBleScanPeriodicalTimeout = new Handler();

	private boolean mIsBleScanning = false;
	private boolean mIsBleScanningPeriodically = false;

	private boolean mIsFilterFoundDevices = false;
	private UUID[] mSearchForDevicesWithSpecificUuidServices;

	/**
	 * @throws NullPointerException If one of the parameters is null
	 * @param activity Activity
	 * @param bluetoothAdapterHelperCallback Class to give callbacks to.
	 */
	public BluetoothAdapterHelper(Activity activity,
			BluetoothAdapterHelperCallback bluetoothAdapterHelperCallback) {
		if (activity == null || bluetoothAdapterHelperCallback == null)
			throw new NullPointerException(
					"Activity or BluetoothAdapterHelperCallback object passed is NULL");

		mActivity = activity;
		mBluetoothAdapterHelperCallback = bluetoothAdapterHelperCallback;

		if (!initialise()) throw new NullPointerException("BT adapter could not be initialised");
	}

	public BluetoothManager getBluetoothManager() {
		return mBluetoothManager;
	}

	public BluetoothAdapter getBluetoothAdapter() {
		return mBluetoothAdapter;
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
	 * the {@link #startBleScanPeriodically(UUID[])} methods
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
	 * check if this device supports BT Classic.
	 * 
	 * @return boolean false if BT classic is not supported, otherwise true
	 */
	public boolean checkBtClassicSupport() {
		boolean hasBt = mActivity.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH);

		Log.i(TAG, "BT Classic support: " + hasBt);

		return hasBt;
	}

	/**
	 * check if this device has BT Classic and BLE hardware functionality.
	 * 
	 * @return boolean false if BLE hardware is not available, otherwise true
	 */
	public boolean checkBleCentralSupport() {
		boolean hasBle = mActivity.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE);

		Log.i(TAG, "BLE Central support: " + hasBle);

		return hasBle;
	}

	/**
	 * checks if this device supports peripheral mode (advertisements)
	 * 
	 * @return true if supports peripheral mode, false otherwise
	 */
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public boolean checkBlePeripheralSupport() {
		boolean hasPeripheral = false;

		hasPeripheral = mBluetoothAdapter
				.isMultipleAdvertisementSupported();

		Log.i(TAG, "BLE peripheral support: " + hasPeripheral);

		return hasPeripheral;
	}

	/**
	 * initiates BT Classic scan operation. The scan time operation is 12
	 * seconds. <br/>
	 * The method
	 * {@link BluetoothAdapterHelperCallback#onDiscoveryDeviceFound(BluetoothDevice, int)}
	 * will be called when a device is found
	 * 
	 * @return true on success, false on error
	 */
	public boolean startDiscovery()	{
		boolean discoveryInitiatedSuccessfully = mBluetoothAdapter
				.startDiscovery();

		if (discoveryInitiatedSuccessfully)	{
			registerReceiver();
		}

		return discoveryInitiatedSuccessfully;
	}

	/**
	 * Cancel the current device discovery process. <br>
	 * Because discovery is a heavyweight procedure for the Bluetooth adapter,
	 * this method should always be called before attempting to connect to a
	 * remote device.
	 * 
	 * @return true on success, false on error
	 */
	public boolean stopDiscovery() {
		return mBluetoothAdapter.cancelDiscovery();
	}

	/**
	 * Initiates stop scanning operation, the onBleStopScan callback gets called
	 * whenever the BLE scanning operations stops/finishes by either calling
	 * this method or when the timeout of the BLE scanning operation runs out
	 */
	@SuppressWarnings("deprecation")
	public void stopBleScan() {
		if (isBleScanning()) {
			getBluetoothAdapter().stopLeScan(mLeScanCallback);
			mIsBleScanning = false;
			mIsBleScanningPeriodically = false;
			mIsFilterFoundDevices = false;
			mBleScanTimeout.removeCallbacksAndMessages(null);
			mBleScanPeriodicalTimeout.removeCallbacksAndMessages(null);
			mBluetoothAdapterHelperCallback.onBleStopScan();
		} else {
			Log.i(TAG, "BLE Scanning has already been stopped!");
		}
	}

	/**
	 * initiates BLE scan operation, the callback
	 * {@link BluetoothAdapterHelperCallback#onBleStopScan()} gets called when
	 * the {@link #stopBleScan()} method is called or when the BLE_SCAN_TIMEOUT
	 * runs out
	 * 
	 * @return true, if the scan was started successfully
	 */
	@SuppressWarnings("deprecation")
	public boolean startBleScan() {
		boolean scanInitiatedSuccessfully = mBluetoothAdapter
				.startLeScan(mLeScanCallback);

		if (scanInitiatedSuccessfully) {
			// scanning was initiated successfully
			mIsBleScanning = true;
			bleStopScanTimeout();
		}

		return scanInitiatedSuccessfully;
	}

	/**
	 * initiates BLE scan operation for devices that are advertising the given
	 * UUIDs given, the callback
	 * {@link BluetoothAdapterHelperCallback#onBleStopScan()} gets called when
	 * the {@link #stopBleScan()} method is called or when the BLE_SCANN_TIMEOUT
	 * runs out
	 * 
	 * @param serviceUUIDs
	 *            the services UUIDs to search BLE devices, Make sure that the
	 *            BLE device is actually advertising the UUIDs looking for in
	 *            it's advertisement data
	 * @return true, if the scan was started successfully, otherwise false
	 */
	@SuppressWarnings("deprecation")
	public boolean startBleScan(final UUID[] serviceUUIDs) {
		mSearchForDevicesWithSpecificUuidServices = serviceUUIDs;
		mIsFilterFoundDevices = true;

		boolean scanInitiatedSuccessfully = mBluetoothAdapter
				.startLeScan(mLeScanCallback);

		if (scanInitiatedSuccessfully) {
			Log.i(TAG, "Scan for devices with specific UUIDs");

			// scanning was initiated successfully
			mIsBleScanning = true;
			bleStopScanTimeout();
		} else {
			mIsFilterFoundDevices = false;
			mSearchForDevicesWithSpecificUuidServices = null;
		}

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
			// scan was initiated successfully
			Log.i(TAG, "Scan for BLE devices periodically");
			bleStartScanPeriodically();
		}

		return scanInitiatedSuccessfully;
	}

	/**
	 * start a BLE scan operation for BLE devices that are advertising the given
	 * UUIDs given. This method in the background actually starts and stop the
	 * scanning every BLE_SCAN_PERIODICAL_INTERVAL
	 * <p>
	 * 
	 * The callback {@link BluetoothAdapterHelperCallback#onBleStopScan()} gets
	 * called when the {@link #stopBleScan()} method is called or when the
	 * BLE_SCAN_TIMEOUT runs out
	 * 
	 * @param serviceUUIDs
	 *            the services UUIDs to search BLE devices
	 * @return true, if the scan was started successfully, otherwise false
	 */
	public boolean startBleScanPeriodically(final UUID[] serviceUUIDs) {
		boolean scanInitiatedSuccessfully = startBleScan(serviceUUIDs);

		if (scanInitiatedSuccessfully) {
			// Scan was initiated successfully
			Log.i(TAG, "Scan for BLE devices with specific UUIDs periodically");
			bleStartScanPeriodically(serviceUUIDs);
		}

		return scanInitiatedSuccessfully;
	}

	/**
	 * Initialise local Bluetooth hardware.
	 *
	 * BluetoothAdapter must be initialised successfully before doing any
	 * start/stop scanning operations.
	 * 
	 * @return boolean false if it failed to initialise local Bluetooth
	 *         hardware, otherwise returns true.
	 */
	@SuppressLint("NewApi")
	private boolean initialise() {
		if (mBluetoothManager == null) {
			mBluetoothManager = (BluetoothManager) mActivity.getSystemService(Context.BLUETOOTH_SERVICE);
			if (mBluetoothManager == null)	{
				return false;
			}
		}
		if (mBluetoothAdapter == null) {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		}

		return mBluetoothAdapter != null;
	}

	/**
	 * Create a BroadcastReceiver for ACTION_FOUND callback for discovery
	 * operations (Bluetooth classic and BLE devices scanning)
	 */
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
				// Leave empty.
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))	{
				mBluetoothAdapterHelperCallback.onDiscoveryStop();
				unregisterReceiver();
			} else if (BluetoothDevice.ACTION_FOUND.equals(action))	{
				// When discovery inquiry finds a device
				// Get the BluetoothDevice object from the Intent
				int rssi = intent.getShortExtra(
						BluetoothDevice.EXTRA_RSSI, (short) 0);
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				mBluetoothAdapterHelperCallback.onDiscoveryDeviceFound(device,
						rssi);
			}
		}
	};

	private void registerReceiver() {
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);

		// Don't forget to unregister once we are done
		mActivity.registerReceiver(mReceiver, filter);
	}

	private void unregisterReceiver() {
		// unRegister the BroadcastReceiver
		mActivity.unregisterReceiver(mReceiver);
	}

	/**
	 * defines callback for BLE scanning results
	 */
	private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		// comes from: startLeScan
		public void onLeScan(final BluetoothDevice device, final int rssi,
				final byte[] scanRecord) {
			Log.i(TAG, "A BLE device was found");

			if (!mIsFilterFoundDevices)	{
				/*
				 * currently doing an all BLE devices scan
				 */
				mBluetoothAdapterHelperCallback.onBleDeviceFound(device, rssi,
						scanRecord);
			} else {
				/*
				 * doing a specific service UUIDs BLE devices scan
				 */
				// get the service's UUIDs that the remote device is advertising
				List<UUID> deviceAdvertisedServices = parseUUIDs(scanRecord);

				// displaying to console the UUIDs that are advertised in the
				// advertise data of the remote BLE device
				for (UUID currentUuidFromDevice : deviceAdvertisedServices)	{
					Log.i(TAG,
							"UUID advertised by remote device without connecting: "
									+ currentUuidFromDevice);
				}

				/*
				 * filtering, checking if the remote BLE device has the UUIDs
				 * asked for
				 */
				for (UUID mSearchForDevicesWithSpecificUuidService : mSearchForDevicesWithSpecificUuidServices) {
					if (deviceAdvertisedServices
							.contains(mSearchForDevicesWithSpecificUuidService)) {
						mBluetoothAdapterHelperCallback.onBleDeviceFound(
								device, rssi, scanRecord);
					}
				}
			}
		}
	};

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
		/*
		 * Stop scanning and then after BLE_SCAN_PERIODICAL_INTERVAL start BLE
		 * scanning
		 */
		mBleScanPeriodicalTimeout.postDelayed(() -> {
			mBluetoothAdapter.stopLeScan(mLeScanCallback);

			// while this is true it will continue starting the BLE scan
			// the mIsBleScanningPeriodically becomes false when the
			// BLE_SCAN_TIMEOUT runs out
			if (mIsBleScanningPeriodically)	{
				mBluetoothAdapter.startLeScan(mLeScanCallback);
				bleStartScanPeriodically();
			}
		}, BLE_SCAN_PERIODICAL_INTERVAL);
	}

	/**
	 * Once this method is called after BLE_SCAN_PERIODICAL_INTERVAL runs out
	 * it's body code will be called. BLE scanning will be stopped, if
	 * mIsBleScanningPeriodically is true then it starts BLE scanning for remote
	 * devices that advertise some specific Services. <br>
	 * As long as the mIsBleScanningPeriodically is true then this method will
	 * be called recursively.
	 */
	private void bleStartScanPeriodically(final UUID[] serviceUuids) {
		mIsBleScanningPeriodically = true;
		/*
		 * Stop scanning and then after
		 * BLE_SCANNING_PERIODICALLY_INTERVAL_TIMEOUT start BLE scanning
		 */
		mBleScanPeriodicalTimeout.postDelayed(() -> {
			getBluetoothAdapter().stopLeScan(mLeScanCallback);

			/*
			 * while this is true it will continue starting the BLE scan.
			 * the mIsBleScanPeriodically becomes false when the
			 * BLE_SCANNING_TIMEOUT runs out
			 */
			if (mIsBleScanningPeriodically)	{
				mBluetoothAdapter.startLeScan(serviceUuids, mLeScanCallback);
				bleStartScanPeriodically(serviceUuids);
			}
		}, BLE_SCAN_PERIODICAL_INTERVAL);
	}

	/**
	 * finds the UUIDs that are in the advertised data of a remote BLE device
	 * 
	 * @param advertisedData
	 *            the data to search for UUIDs
	 * 
	 * @return the list of found UUIDs that are in the advertised data
	 */
	private List<UUID> parseUUIDs(byte[] advertisedData) {
		List<UUID> UUIDs = new ArrayList<>();

		ByteBuffer buffer = ByteBuffer.wrap(advertisedData).order(
				ByteOrder.LITTLE_ENDIAN);
		while (buffer.remaining() > 2) {
			byte length = buffer.get();
			if (length == 0) break;

			byte type = buffer.get();
			switch (type) {
				case 0x02: // Partial list of 16-bit UUIDs
				case 0x03: // Complete list of 16-bit UUIDs
					while (length >= 2)	{
						UUIDs.add(UUID.fromString(String.format(
								"%08x-0000-1000-8000-00805f9b34fb",
								buffer.getShort())));
						length -= 2;
					}
					break;
				case 0x06: // Partial list of 128-bit UUIDs
				case 0x07: // Complete list of 128-bit UUIDs
					while (length >= 16) {
						long lsb = buffer.getLong();
						long msb = buffer.getLong();
						UUIDs.add(new UUID(msb, lsb));
						length -= 16;
					}
					break;
				default:
					buffer.position(buffer.position() + length - 1);
					break;
			}
		}

		return UUIDs;
	}
}