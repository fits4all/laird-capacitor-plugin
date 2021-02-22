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

    async connectToDevice(): Promise<any> {
        return new Promise<any>((_resolve, reject) => {
            reject(new Error("Not implemented for web."));
        });
    }

    async disconnectFromDevice(): Promise<any> {
        return new Promise<any>((_resolve, reject) => {
            reject(new Error("Not implemented for web."));
        });
    }

    async sendDataToDevice(): Promise<any> {
        return new Promise<any>((_resolve, reject) => {
            reject(new Error("Not implemented for web."));
        });
    }

}
