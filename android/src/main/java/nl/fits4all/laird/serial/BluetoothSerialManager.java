package nl.fits4all.laird.serial;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;

import com.getcapacitor.JSObject;

import nl.fits4all.laird.LairdCapacitorPlugin;
import nl.fits4all.laird.serial.bt.ble.vsp.VirtualSerialPortDevice;

public class BluetoothSerialManager extends VirtualSerialPortDevice
{

    private final LairdCapacitorPlugin plugin;

    public BluetoothSerialManager(LairdCapacitorPlugin plugin, Activity activity)
    {
        super(activity);
        this.plugin = plugin;

        SEND_DATA_TO_REMOTE_DEVICE_DELAY = 1;
    }

    @Override
    public void onVspSendDataSuccess(final BluetoothGatt gatt,
                                     final BluetoothGattCharacteristic ch)
    {
        sendDataHandler.postDelayed(() -> {
            if (mFifoAndVspManagerState == FifoAndVspManagerState.UPLOADING) {/*
             * what to do after the data was send successfully
             */
                if (isBufferSpaceAvailable()) {
                    uploadNextDataFromFifoToRemoteDevice();
                }
            }
        }, SEND_DATA_TO_REMOTE_DEVICE_DELAY);
    }

    @Override
    public void onVspReceiveData(BluetoothGatt gatt,
                                 BluetoothGattCharacteristic ch)
    {
        mRxBuffer.write(ch.getStringValue(0));

        while (mRxBuffer.read(mRxDest) != 0)
        {
            /*
             * found data
             */
            String rxBufferDataRead = mRxDest.toString();
            mRxDest.delete(0, mRxDest.length());

            JSObject js = new JSObject();
            js.put("data", rxBufferDataRead);
            plugin.notifyCapacitorListeners("deviceRecvDataEvent", js);
        }
    }

    @Override
    public void onVspIsBufferSpaceAvailable(
            boolean isBufferSpaceAvailableOldState,
            boolean isBufferSpaceAvailableNewState)
    {
        if (mFifoAndVspManagerState == FifoAndVspManagerState.UPLOADING) {/*
         * callback for what to do when data was send successfully from the
         * android device and when the module buffer was full and now it has
         * been cleared, which means it now has available space
         */
            if (!isBufferSpaceAvailableOldState
                    && isBufferSpaceAvailableNewState) {
                uploadNextDataFromFifoToRemoteDevice();
            }
        }
    }

    @Override
    public void onUploaded()
    {
        super.onUploaded();
    }
}