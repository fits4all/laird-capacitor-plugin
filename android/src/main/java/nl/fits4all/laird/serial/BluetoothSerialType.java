package nl.fits4all.laird.serial;

public enum BluetoothSerialType {

    MESSAGE_STATE_CHANGE(1),
    MESSAGE_READ(2),
    MESSAGE_WRITE(3),
    MESSAGE_DEVICE_NAME(4),
    MESSAGE_TOAST(5),
    MESSAGE_READ_RAW(6);

    private final int value;

    BluetoothSerialType(int value) {
        this.value = value;
    }

    /**
     * Gets the type of the bluetooth serial protocol.
     * @return Type value.
     */
    public int getType() {
        return value;
    }
}
