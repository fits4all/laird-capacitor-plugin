import { WebPlugin } from '@capacitor/core';

import type { MainPlugin } from './definitions';

export class MainWeb extends WebPlugin implements MainPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
