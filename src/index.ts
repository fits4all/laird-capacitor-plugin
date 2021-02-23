import { registerPlugin } from '@capacitor/core';

import type { LairdCapacitorPlugin } from './definitions';

const Laird = registerPlugin<LairdCapacitorPlugin>('Laird', {
  web: () => import('./web').then(m => new m.LairdCapacitorWeb()),
});

export * from './definitions';
export { Laird };
