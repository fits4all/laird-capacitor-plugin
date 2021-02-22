package nl.fits4all.laird.serial;

import android.os.Handler;
import android.os.Message;

import static nl.fits4all.laird.serial.BluetoothSerialType.MESSAGE_READ;
import static nl.fits4all.laird.serial.BluetoothSerialType.MESSAGE_READ_RAW;
import static nl.fits4all.laird.serial.BluetoothSerialType.MESSAGE_STATE_CHANGE;
import static nl.fits4all.laird.serial.BluetoothSerialType.MESSAGE_WRITE;

public class BluetoothSerialHandler extends Handler {

    @Override
    public void handleMessage(Message msg) {
        if (msg.what == MESSAGE_READ.getType()) {
            byte[] bytes = (byte[]) msg.obj;
        } else if (msg.what == MESSAGE_READ_RAW.getType()) {
            byte[] bytes = (byte[]) msg.obj;
        } else if (msg.what == MESSAGE_STATE_CHANGE.getType()) {
            int state = msg.arg1;
        } else if (msg.what == MESSAGE_WRITE.getType()) {
            byte[] writeBuf = (byte[]) msg.obj;
        }
    }
}
