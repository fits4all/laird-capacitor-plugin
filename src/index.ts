import { registerPlugin } from '@capacitor/core';

import type { MainPlugin } from './definitions';

const Main = registerPlugin<MainPlugin>('Main', {
  web: () => import('./web').then(m => new m.MainWeb()),
});

export * from './definitions';
export { Main };
