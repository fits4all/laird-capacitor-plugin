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
   * Add listener.
   */
  addListener(eventName: 'deviceFoundEvent', listenerFunc: (notification: { deviceName: string, deviceType: number, deviceAddress: string }) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
  

}