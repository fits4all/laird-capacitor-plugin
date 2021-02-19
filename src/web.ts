import { WebPlugin } from '@capacitor/core';

import type { MainPlugin } from './definitions';

export class MainWeb extends WebPlugin implements MainPlugin { 

    async startScanningDevices(): Promise<any> {
        return new Promise<any>((_resolve, reject) => {
            reject(new Error("Not implemented yet!"));
        });
    }

    async stopScanningDevices(): Promise<{ status: number, body: string }> {
        return new Promise<any>((_resolve, reject) => {
            reject(new Error("Not implemented yet!"));
        });
    }

}
