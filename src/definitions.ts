import { PluginListenerHandle } from "@capacitor/core";

export interface MainPlugin {

  /**
   * Start the scanning process for finding bluetooth devices.
   */
  startScanningDevices(): Promise<{ status: number, body: string }>;

  /**
   * Stops the scanning process for finding bluetooth devices.
   */
  stopScanningDevices(): Promise<{ status: number, body: string }>;

  /**
   * 
   */
  addListener(eventName: 'deviceFoundEvent', listenerFunc: (notification: { deviceName: string, deviceType: number, deviceAddress: string }) => void): Promise<PluginListenerHandle> & PluginListenerHandle;
  

}