# react-native-sync-adapter

[![Circle CI](https://circleci.com/gh/ferrannp/react-native-sync-adapter.svg?style=shield)](https://circleci.com/gh/ferrannp/react-native-sync-adapter) [![npm version](https://badge.fury.io/js/react-native-sync-adapter.svg)](https://badge.fury.io/js/react-native-sync-adapter)

[Intelligent Job-Scheduling](https://developer.android.com/topic/performance/scheduling.html) port to React Native: Scheduling data background synchronizations that run in your JavaScript.

Read a broader introduction in the following post: [React Native and Native Modules: The Android SyncAdapter](https://blog.callstack.io/react-native-and-native-modules-the-android-syncadapter-517ddf851bf4#.qb5ed9din)

## Requirements
* React Native 0.60+

## Pros
Under the hood, this library uses a [SyncAdapter](https://developer.android.com/reference/android/content/AbstractThreadedSyncAdapter.html):

* Android will trigger a sync using our `syncFlexTime` to decide when is the best moment to do so (battery efficiency)
* No need to worry about internet connection
* No need to worry about the user restarting the device
* Compatible with all Android versions supported by RN (4.1+)

## Caveats
This library is only for Android. If you want to do something similar on iOS, I recommend using [react-native-background-fetch](https://github.com/transistorsoft/react-native-background-fetch).

## Getting started

```
yarn add @mjschutz/react-native-sync-adapter
```

### Installation

The library will be linked automatically by [Autolinking](https://github.com/react-native-community/cli/blob/master/docs/autolinking.md).
However, this library requires some manual steps:

#### Manual Android required step
Open up the `string.xml` file of your Android project. You need to add the following (just change the content):
```xml
<string name="app_name">YourAppName</string>
<string name="rnsb_sync_account_type" translatable="false">your.android.package.name</string>
<string name="rnsb_content_authority" translatable="false">your.android.package.name.provider</string>
```

This will override the default values from the library and make them unique for your app.

## Usage
You need to [register a task](https://facebook.github.io/react-native/docs/headless-js-android.html#the-js-api) with a specific name and only with this specific name: `TASK_SYNC_ADAPTER`. You should do it in the same place where you register your app:

```js
AppRegistry.registerComponent('MyApp', () => MyApp);
AppRegistry.registerHeadlessTask('TASK_SYNC_ADAPTER', () => TestTask);
```

Then, for example, on your root component:
```js
import {useEffect} from 'react';
import SyncAdapter from '@mjschutz/react-native-sync-adapter';

useEffect(() => {
  SyncAdapter.init({
    syncInterval,
    syncFlexTime,
  });
}, []);
```

That is all! Some extras:

### Timeout

The default timeout for your Headless JS task is 5 minutes (300000ms). If you want to override this value, you will also need to override `strings.xml` again:

```xml
<!-- Overrides default timeout to 10 minutes -->
<string name="rnsb_default_timeout" translatable="false">600000</string>
```

### User visible

By default the user will not be able to manually enable/disable syncs through the Account settings. If you want the user to have this option, you need to override `strings.xml`:

```xml
<!-- Allows the user to enable/disable the sync functionality through the Account settings -->
<string name="rnsb_user_visible" translatable="false">true</string>
```

### Running the task while the app is in the foreground

~~By default, the sync task will only run if the app is **not** in the foreground. This is one of the default [caveats](https://facebook.github.io/react-native/docs/headless-js-android.html#caveats) from HeadlessJS.~~

The sync task will always run on foreground. For foreground services to run (since Android 8.0), a notification is show to the user to inform that a foreground service is running, to change the info about the notification, you need to override `strings.xml`:

```xml
<string name="rnsb_notification_name" translatable="false">React Native SyncAdapter Foreground Service</string>
<string name="rnsb_notification_text" translatable="false">App is running on foreground</string>
<string name="rnsb_notification_channel_id" translatable="false">RNSAHeadlessServiceFgChId</string>
```

Each option is described bellow:

* rnsb_notification_name - The user visible name of the notification channel.
* rnsb_notification_text - The text/title of the notification
* rnsb_notification_channel_id - The id of the notification channel. Must be unique per package.

The notification will use the App icon (res/mipmap-(hdpi|mdpi|...)/ic_launcher) as the notification icon when displayed.

### Broadcast Receiver

If you want to trigger a sync natively (e.g. responding to a broadcast receiver), you can call:

```java
SyncAdapter.syncImmediately(Context context, int syncInterval, int syncFlexTime);
```

## API

### init

Schedules background syncs within your app.

```js
Object: {
  syncInterval: number;
  syncFlexTime: number;
}
```

* `syncInterval`: The amount of time in seconds that you wish to elapse between periodic syncs
* `syncFlexTime`: The amount of flex time in seconds before `syncInterval` that you permit for the sync to take place. Must be less than `syncInterval`

A good example could be `syncInterval: 12 * 60 * 60` (12 hours) and `syncFlexTime: 0.5 * 60 * 60` (30 minutes).

Notice that `syncFlexTime` only works for Android 4.4+, for older versions, that value will be ignored and syncs will be always exact.

### syncImmediately

Invoke the sync task. Use the same values as in the [init](#init) call.

```js
Object: {
  syncInterval: number;
  syncFlexTime: number;
}
```

## Running example

You can try this library running the `example` app:

```
cd example && yarn && npm start
```

Then just run:

```
yarn android
```

**Be careful**: The installed example app will trigger a sync around every minute (or 15 minutes since Android 7.0). If you debug the app, you should be able to see the HeadlessJS outputing: `Headless JS task was fired!`. After you try it, I recommend to uninstall the app so you don't harm your device battery life.
