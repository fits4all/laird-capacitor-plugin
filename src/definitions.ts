import { PluginListenerHandle } from "@capacitor/core";

export interface LairdCapacitorPlugin {

  /**
   * Start the discovering process for finding bluetooth devices.
   * @param Options
   */
  startDiscovering(options: { periodically: boolean }): Promise<any>

  /**
   * Cancels the discovering process for finding bluetooth devices.
   */
  cancelDiscovering(): Promise<any>
  
  /**
   * Connects to the specified device with the address.
   * @param Options
   */
  connectToDevice(options: {address: string}): Promise<{ status: string }>

  /**
   * Disconnects the current connected device.
   * @param Options
   */
  disconnectFromDevice(): Promise<{ status: string }>

  /**
   * Sends data to the connected device.
   * @param Options 
   */
  sendDataToDevice(options: {data: string}): Promise<{ status: string}>

  /**
   * DiscoveryStartEvent
   */
  addListener(eventName: 'discoveryStartEvent', callback: () => void): Promise<PluginListenerHandle> & PluginListenerHandle;

  /**
   * DiscoveryStopEvent
   */
  addListener(eventName: 'discoveryStopEvent', callback: () => void): Promise<PluginListenerHandle> & PluginListenerHandle;

  /**
   * DeviceFoundListener
   * @param eventName DeviceFoundEvent
   * @param callback Callback
   */
  addListener(eventName: 'deviceFoundEvent', callback: (device: Device) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
  
  /**
   * DeviceRecvDataListener
   * @param eventName DeviceRecvDataEvent
   * @param callback Callback
   */
  addListener(eventName: 'deviceRecvDataEvent', callback: (data: DeviceData) => void): Promise<PluginListenerHandle> & PluginListenerHandle;

}

export interface Device {
  name: string;
  type: number;
  address: string;
  rssi: number;
}

export interface DeviceData {
  data: string;
}