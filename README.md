# laird-capacitor-plugin

WIP

## Install

```bash
npm install laird-capacitor-plugin
npx cap sync
```

## API

<docgen-index>

* [`startScanningDevices()`](#startscanningdevices)
* [`stopScanningDevices()`](#stopscanningdevices)
* [`addListener(...)`](#addlistener)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### startScanningDevices()

```typescript
startScanningDevices() => any
```

Start the scanning process for finding bluetooth devices.

**Returns:** <code>any</code>

--------------------


### stopScanningDevices()

```typescript
stopScanningDevices() => any
```

Stops the scanning process for finding bluetooth devices.

**Returns:** <code>any</code>

--------------------


### addListener(...)

```typescript
addListener(eventName: 'deviceFoundEvent', listenerFunc: (notification: { deviceName: string; deviceType: number; deviceAddress: string; }) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param              | Type                                                                                                       |
| ------------------ | ---------------------------------------------------------------------------------------------------------- |
| **`eventName`**    | <code>"deviceFoundEvent"</code>                                                                            |
| **`listenerFunc`** | <code>(notification: { deviceName: string; deviceType: number; deviceAddress: string; }) =&gt; void</code> |

**Returns:** <code>any</code>

--------------------


### Interfaces


#### PluginListenerHandle

| Prop         | Type                      |
| ------------ | ------------------------- |
| **`remove`** | <code>() =&gt; any</code> |

</docgen-api>
