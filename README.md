# Laird Capacitor Plugin

A work in progress capacitor plugin that communicates with bluetooth laird devices.

## Install

```bash
npm install laird-capacitor-plugin
npx cap sync
```

## API

<docgen-index>

* [`startDiscovering()`](#startdiscovering)
* [`cancelDiscovering()`](#canceldiscovering)
* [`connectToDevice(...)`](#connecttodevice)
* [`disconnectFromDevice()`](#disconnectfromdevice)
* [`sendDataToDevice(...)`](#senddatatodevice)
* [`addListener(...)`](#addlistener)
* [`addListener(...)`](#addlistener)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### startDiscovering()

```typescript
startDiscovering() => any
```

Start the discovering process for finding bluetooth devices.

**Returns:** <code>any</code>

--------------------


### cancelDiscovering()

```typescript
cancelDiscovering() => any
```

Cancels the discovering process for finding bluetooth devices.

**Returns:** <code>any</code>

--------------------


### connectToDevice(...)

```typescript
connectToDevice(options: { address: string; }) => any
```

Connects to the specified device with the address.

| Param         | Type                              |
| ------------- | --------------------------------- |
| **`options`** | <code>{ address: string; }</code> |

**Returns:** <code>any</code>

--------------------


### disconnectFromDevice()

```typescript
disconnectFromDevice() => any
```

Disconnects the current connected device.

**Returns:** <code>any</code>

--------------------


### sendDataToDevice(...)

```typescript
sendDataToDevice(options: { data: string; }) => any
```

Sends data to the connected device.

| Param         | Type                           |
| ------------- | ------------------------------ |
| **`options`** | <code>{ data: string; }</code> |

**Returns:** <code>any</code>

--------------------


### addListener(...)

```typescript
addListener(eventName: 'deviceFoundEvent', callback: (ret: { name: string; type: number; address: string; }) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

DeviceFoundListener

| Param           | Type                                                                            | Description      |
| --------------- | ------------------------------------------------------------------------------- | ---------------- |
| **`eventName`** | <code>"deviceFoundEvent"</code>                                                 | DeviceFoundEvent |
| **`callback`**  | <code>(ret: { name: string; type: number; address: string; }) =&gt; void</code> | Callback         |

**Returns:** <code>any</code>

--------------------


### addListener(...)

```typescript
addListener(eventName: 'deviceRecvDataEvent', callback: (ret: { data: string; }) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

DeviceRecvDataListener

| Param           | Type                                             | Description         |
| --------------- | ------------------------------------------------ | ------------------- |
| **`eventName`** | <code>"deviceRecvDataEvent"</code>               | DeviceRecvDataEvent |
| **`callback`**  | <code>(ret: { data: string; }) =&gt; void</code> | Callback            |

**Returns:** <code>any</code>

--------------------


### Interfaces


#### PluginListenerHandle

| Prop         | Type                      |
| ------------ | ------------------------- |
| **`remove`** | <code>() =&gt; any</code> |

</docgen-api>
