package com.baofeng.aone.bluetooth.profile;

import android.bluetooth.BluetoothDevice;

interface LocalBluetoothProfile {

    /**
     * Returns true if the user can initiate a connection, false otherwise.
     */
    boolean isConnectable();

    /**
     * Returns true if the user can enable auto connection for this profile.
     */
    boolean isAutoConnectable();

    boolean connect(BluetoothDevice device);

    boolean disconnect(BluetoothDevice device);

    int getConnectionStatus(BluetoothDevice device);

    boolean isPreferred(BluetoothDevice device);

    int getPreferred(BluetoothDevice device);

    void setPreferred(BluetoothDevice device, boolean preferred);

    boolean isProfileReady();

    /** Display order for device profile settings. */
    int getOrdinal();
}
