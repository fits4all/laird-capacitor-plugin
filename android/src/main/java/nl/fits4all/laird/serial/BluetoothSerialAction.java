package nl.fits4all.laird.serial;

public enum BluetoothSerialAction {

    LIST("list"),
    CONNECT("connect"),
    CONNECT_INSECURE("connectInsecure"),
    DISCONNECT("disconnect"),
    WRITE("write"),
    AVAILABLE("available"),
    READ("read"),
    READ_UNTIL("readUntil"),
    SUBSCRIBE("subscribe"),
    UNSUBSCRIBE("unsubscribe"),
    SUBSCRIBE_RAW("subscribeRaw"),
    UNSUBSCRIBE_RAW("unsubscribeRaw"),
    IS_ENABLED("isEnabled"),
    IS_CONNECTED("isConnected"),
    CLEAR("clear"),
    SETTINGS("showBluetoothSettings"),
    ENABLE("enable"),
    DISCOVER_UNPAIRED("discoverUnpaired"),
    SET_DEVICE_DISCOVERED_LISTENER("setDeviceDiscoveredListener"),
    CLEAR_DEVICE_DISCOVERED_LISTENER("clearDeviceDiscoveredListener"),
    SET_NAME("setName"),
    SET_DISCOVERABLE("setDiscoverable");

    private final String value;

    /**
     * Represents the action of bluetooth serial protocol.
     * @param value Action value.
     */
    BluetoothSerialAction(String value) {
        this.value = value;
    }

    /**
     * Gets the action value of bluetooth serial protocol.
     * @return Action value.
     */
    public String getValue() {
        return value;
    }
}
