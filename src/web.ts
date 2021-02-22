import { WebPlugin } from '@capacitor/core';

import type { MainPlugin } from './definitions';

export class MainWeb extends WebPlugin implements MainPlugin { 

    async startDiscovering(): Promise<any> {
        return new Promise<any>((_resolve, reject) => {
            reject(new Error("Not implemented for web."));
        });
    }

    async cancelDiscovering(): Promise<any> {
        return new Promise<any>((_resolve, reject) => {
            reject(new Error("Not implemented for web."));
        });
    }

}
