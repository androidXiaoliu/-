package com.baofeng.aone.bluetooth.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.baofeng.aone.LauncherApplication;
import com.baofeng.aone.bluetooth.BluetoothManager;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;

public class LocalBluetoothProfileManager {
    /** Singleton instance. */
    private static LocalBluetoothProfileManager sInstance;

    /**
     * An interface for notifying BluetoothHeadset IPC clients when they have
     * been connected to the BluetoothHeadset service. Only used by
     * {@link DockService}.
     */
    // public interface ServiceListener {
    // /**
    // * Called to notify the client when this proxy object has been
    // * connected to the BluetoothHeadset service. Clients must wait for
    // * this callback before making IPC calls on the BluetoothHeadset
    // * service.
    // */
    // void onServiceConnected();
    //
    // /**
    // * Called to notify the client that this proxy object has been
    // * disconnected from the BluetoothHeadset service. Clients must not
    // * make IPC calls on the BluetoothHeadset service after this callback.
    // * This callback will currently only occur if the application hosting
    // * the BluetoothHeadset service, but may be called more often in future.
    // */
    // void onServiceDisconnected();
    // }
    public static synchronized LocalBluetoothProfileManager getInstance() {
        if (sInstance == null) {
            sInstance = new LocalBluetoothProfileManager();
        }
        return sInstance;
    }

    private final Context mContext;
    private BluetoothManager mManager;

    private A2dpProfile mA2dpProfile;
    private final HidProfile mHidProfile;

    /**
     * Mapping from profile name, e.g. "HEADSET" to profile object.
     */
    private final Map<String, LocalBluetoothProfile> mProfileNameMap = new HashMap<String, LocalBluetoothProfile>();

    public LocalBluetoothProfileManager() {
        mContext = LauncherApplication.getInstance();
        mManager = BluetoothManager.getAndroidManager();

        ParcelUuid[] uuids = mManager.getUuids();

        // uuids may be null if Bluetooth is turned off
        if (uuids != null) {
            updateLocalProfiles(uuids);
        }

        // Always add HID profiles
        mHidProfile = new HidProfile();
        addProfile(mHidProfile, HidProfile.NAME);
    }

    /**
     * Initialize or update the local profile objects. If a UUID was previously
     * present but has been removed, we print a warning but don't remove the
     * profile object as it might be referenced elsewhere, or the UUID might
     * come back and we don't want multiple copies of the profile objects.
     *
     * @param uuids
     */
    void updateLocalProfiles(ParcelUuid[] uuids) {
        // A2DP
        if (BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.AudioSource)) {
            if (mA2dpProfile == null) {
                mA2dpProfile = new A2dpProfile();
                addProfile(mA2dpProfile, A2dpProfile.NAME);
            }
        }
    }

    private void addProfile(LocalBluetoothProfile profile, String profileName) {
        mProfileNameMap.put(profileName, profile);
    }

    public LocalBluetoothProfile getProfileByName(String name) {
        return mProfileNameMap.get(name);
    }

    public void setBluetoothStateOn() {
        ParcelUuid[] uuids = mManager.getUuids();
        if (uuids != null) {
            updateLocalProfiles(uuids);
        }
    }
    // This is called by DockService, so check Headset and A2DP.
    // public synchronized boolean isManagerReady() {
    // // Getting just the headset profile is fine for now. Will need to deal
    // with A2DP
    // // and others if they aren't always in a ready state.
    // LocalBluetoothProfile profile = mA2dpProfile;
    // if (profile != null) {
    // return profile.isProfileReady();
    // }
    // return false;
    // }

    A2dpProfile getA2dpProfile() {
        return mA2dpProfile;
    }

    /**
     * Fill in a list of LocalBluetoothProfile objects that are supported by the
     * local device and the remote device.
     *
     * @param uuids
     *            of the remote device
     * @param localUuids
     *            UUIDs of the local device
     * @param profiles
     *            The list of profiles to fill
     * @param removedProfiles
     *            list of profiles that were removed
     */
    synchronized void updateProfiles(ParcelUuid[] uuids,
            ParcelUuid[] localUuids, Collection<LocalBluetoothProfile> profiles,
            Collection<LocalBluetoothProfile> removedProfiles,
            boolean isPanNapConnected, BluetoothDevice device) {
        // Copy previous profile list into removedProfiles
        removedProfiles.clear();
        removedProfiles.addAll(profiles);
        profiles.clear();

        if (uuids == null) {
            return;
        }

        if (BluetoothUuid.containsAnyUuid(uuids, A2dpProfile.SINK_UUIDS)
                && mA2dpProfile != null) {
            profiles.add(mA2dpProfile);
            removedProfiles.remove(mA2dpProfile);
        }

        if ((BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.Hid)
                || BluetoothUuid.isUuidPresent(uuids, BluetoothUuid.Hogp))
                && mHidProfile != null) {
            profiles.add(mHidProfile);
            removedProfiles.remove(mHidProfile);
        }
    }

}
