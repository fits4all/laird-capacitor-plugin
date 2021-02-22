import { PluginListenerHandle } from "@capacitor/core";

export interface MainPlugin {

  /**
   * Start the discovering process for finding bluetooth devices.
   */
  startDiscovering(): Promise<any>

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
   * DeviceFoundListener
   * @param eventName DeviceFoundEvent
   * @param callback Callback
   */
  addListener(eventName: 'deviceFoundEvent', callback: (ret: { name: string, type: number, address: string }) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
  
  /**
   * DeviceRecvDataListener
   * @param eventName DeviceRecvDataEvent
   * @param callback Callback
   */
  addListener(eventName: 'deviceRecvDataEvent', callback: (ret: { data: string }) => void): Promise<PluginListenerHandle> & PluginListenerHandle;

}