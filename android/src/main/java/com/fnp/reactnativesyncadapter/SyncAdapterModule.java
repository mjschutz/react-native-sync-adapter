package com.fnp.reactnativesyncadapter;

import android.widget.Toast;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

@SuppressWarnings("unused") class SyncAdapterModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public SyncAdapterModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @ReactMethod
    public void init(int syncInterval, int syncFlexTime) {
        SyncAdapter.getSyncAccount(getReactApplicationContext(), syncInterval, syncFlexTime);
    }

    @ReactMethod
    public void syncImmediately(int syncInterval, int syncFlexTime) {
        SyncAdapter.syncImmediately(getReactApplicationContext(), syncInterval, syncFlexTime);
    }

    @Override
    public String getName() {
        return "SyncAdapter";
    }
}
