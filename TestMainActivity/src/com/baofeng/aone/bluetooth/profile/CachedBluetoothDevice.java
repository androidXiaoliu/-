package com.baofeng.aone.bluetooth.profile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.baofeng.aone.ReflectUtil;
import com.baofeng.aone.bluetooth.BluetoothManager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.text.TextUtils;

public class CachedBluetoothDevice {
    private final Context mContext;
    private final BluetoothManager mManager;
    private LocalBluetoothProfileManager mProfileManager;
    private final BluetoothDevice mDevice;
    private String mName;
    private short mRssi;
    private BluetoothClass mBtClass;
    private HashMap<LocalBluetoothProfile, Integer> mProfileConnectionState;

    private final List<LocalBluetoothProfile> mProfiles = new ArrayList<LocalBluetoothProfile>();

    // List of profiles that were previously in mProfiles, but have been removed
    private final List<LocalBluetoothProfile> mRemovedProfiles = new ArrayList<LocalBluetoothProfile>();

    // Device supports PANU but not NAP: remove PanProfile after device
    // disconnects from NAP
    private boolean mLocalNapRoleConnected;

    private boolean mVisible;

    private int mPhonebookPermissionChoice;

    private int mMessagePermissionChoice;

    private int mMessageRejectionCount;

    private final Collection<Callback> mCallbacks = new ArrayList<Callback>();

    // Following constants indicate the user's choices of Phone book/message
    // access settings
    // User hasn't made any choice or settings app has wiped out the memory
    public final static int ACCESS_UNKNOWN = 0;
    // User has accepted the connection and let Settings app remember the
    // decision
    public final static int ACCESS_ALLOWED = 1;
    // User has rejected the connection and let Settings app remember the
    // decision
    public final static int ACCESS_REJECTED = 2;

    // How many times user should reject the connection to make the choice
    // persist.
    private final static int MESSAGE_REJECTION_COUNT_LIMIT_TO_PERSIST = 2;

    private final static String MESSAGE_REJECTION_COUNT_PREFS_NAME = "bluetooth_message_reject";

    /**
     * When we connect to multiple profiles, we only want to display a single
     * error even if they all fail. This tracks that state.
     */
    private boolean mIsConnectingErrorPossible;

    /**
     * Last time a bt profile auto-connect was attempted. If an ACTION_UUID
     * intent comes in within MAX_UUID_DELAY_FOR_AUTO_CONNECT milliseconds, we
     * will try auto-connect again with the new UUIDs
     */
    private long mConnectAttempted;

    // See mConnectAttempted
    private static final long MAX_UUID_DELAY_FOR_AUTO_CONNECT = 5000;

    /** Auto-connect after pairing only if locally initiated. */
    private boolean mConnectAfterPairing;

    /**
     * Describes the current device and profile for logging.
     *
     * @param profile
     *            Profile to describe
     * @return Description of the device and profile
     */
    private String describe(LocalBluetoothProfile profile) {
        StringBuilder sb = new StringBuilder();
        sb.append("Address:").append(mDevice);
        if (profile != null) {
            sb.append(" Profile:").append(profile);
        }

        return sb.toString();
    }

    void onProfileStateChanged(LocalBluetoothProfile profile, int newProfileState) {
        if (mManager.getBluetoothState() == BluetoothAdapter.STATE_TURNING_OFF) {
            return;
        }
        mProfileConnectionState.put(profile, newProfileState);
        if (newProfileState == BluetoothProfile.STATE_CONNECTED) {
            if (!mProfiles.contains(profile)) {
                mRemovedProfiles.remove(profile);
                mProfiles.add(profile);
            }
        }
    }

    CachedBluetoothDevice(Context context, BluetoothManager manager, LocalBluetoothProfileManager profileManager,
            BluetoothDevice device) {
        mContext = context;
        mManager = manager;
        mProfileManager = profileManager;
        mDevice = device;
        mProfileConnectionState = new HashMap<LocalBluetoothProfile, Integer>();
        fillData();
    }

    void disconnect() {
        for (LocalBluetoothProfile profile : mProfiles) {
            disconnect(profile);
        }
        // Disconnect PBAP server in case its connected
        // This is to ensure all the profiles are disconnected as some CK/Hs do
        // not
        // disconnect PBAP connection when HF connection is brought down
    }

    void disconnect(LocalBluetoothProfile profile) {
        if (profile.disconnect(mDevice)) {

        }
    }

    void connect(boolean connectAllProfiles) {
        if (!ensurePaired()) {
            return;
        }

        mConnectAttempted = SystemClock.elapsedRealtime();
        connectWithoutResettingTimer(connectAllProfiles);
    }

    void onBondingDockConnect() {
        // Attempt to connect if UUIDs are available. Otherwise,
        // we will connect when the ACTION_UUID intent arrives.
        connect(false);
    }

    private void connectWithoutResettingTimer(boolean connectAllProfiles) {
        // Try to initialize the profiles if they were not.
        if (mProfiles.isEmpty()) {
            // if mProfiles is empty, then do not invoke updateProfiles. This
            // causes a race
            // condition with carkits during pairing, wherein RemoteDevice.UUIDs
            // have been updated
            // from bluetooth stack but ACTION.uuid is not sent yet.
            // Eventually ACTION.uuid will be received which shall trigger the
            // connection of the
            // various profiles
            // If UUIDs are not available yet, connect will be happen
            // upon arrival of the ACTION_UUID intent.
            return;
        }

        // Reset the only-show-one-error-dialog tracking variable
        mIsConnectingErrorPossible = true;

        int preferredProfiles = 0;
        for (LocalBluetoothProfile profile : mProfiles) {
            if (connectAllProfiles ? profile.isConnectable() : profile.isAutoConnectable()) {
                if (profile.isPreferred(mDevice)) {
                    ++preferredProfiles;
                    connectInt(profile);
                }
            }
        }
        if (preferredProfiles == 0) {
            connectAutoConnectableProfiles();
        }
    }

    private void connectAutoConnectableProfiles() {
        if (!ensurePaired()) {
            return;
        }
        // Reset the only-show-one-error-dialog tracking variable
        mIsConnectingErrorPossible = true;

        for (LocalBluetoothProfile profile : mProfiles) {
            if (profile.isAutoConnectable()) {
                profile.setPreferred(mDevice, true);
                connectInt(profile);
            }
        }
    }

    /**
     * Connect this device to the specified profile.
     *
     * @param profile
     *            the profile to use with the remote device
     */
    void connectProfile(LocalBluetoothProfile profile) {
        mConnectAttempted = SystemClock.elapsedRealtime();
        // Reset the only-show-one-error-dialog tracking variable
        mIsConnectingErrorPossible = true;
        connectInt(profile);
        // Refresh the UI based on profile.connect() call
        refresh();
    }

    synchronized void connectInt(LocalBluetoothProfile profile) {
        if (!ensurePaired()) {
            return;
        }
        if (profile.connect(mDevice)) {
            return;
        }
    }

    private boolean ensurePaired() {
        if (getBondState() == BluetoothDevice.BOND_NONE) {
            startPairing();
            return false;
        } else {
            return true;
        }
    }

    boolean startPairing() {
        // Pairing is unreliable while scanning, so cancel discovery
        if (mManager.isDiscovering()) {
            mManager.cancelDiscovery();
        }

        if (!mDevice.createBond()) {
            return false;
        }

        mConnectAfterPairing = true; // auto-connect after pairing
        return true;
    }

    /**
     * Return true if user initiated pairing on this device. The message text is
     * slightly different for local vs. remote initiated pairing dialogs.
     */
    boolean isUserInitiatedPairing() {
        return mConnectAfterPairing;
    }

    void unpair() {
        int state = getBondState();

        if (state == BluetoothDevice.BOND_BONDING) {
        	ReflectUtil.invoke(mDevice, "cancelBondProcess");
//            mDevice.cancelBondProcess();
        }

        if (state != BluetoothDevice.BOND_NONE) {
            final BluetoothDevice dev = mDevice;
            if (dev != null) {
            	final boolean successful = (boolean)ReflectUtil.invoke(dev, "removeBond");
//                final boolean successful = dev.removeBond();
                if (successful) {
                }
            }
        }
    }

    int getProfileConnectionState(LocalBluetoothProfile profile) {
        if (mProfileConnectionState == null || mProfileConnectionState.get(profile) == null) {
            // If cache is empty make the binder call to get the state
            int state = profile.getConnectionStatus(mDevice);
            mProfileConnectionState.put(profile, state);
        }
        return mProfileConnectionState.get(profile);
    }

    public void clearProfileConnectionState() {
        for (LocalBluetoothProfile profile : getProfiles()) {
            mProfileConnectionState.put(profile, BluetoothProfile.STATE_DISCONNECTED);
        }
    }

    // TODO: do any of these need to run async on a background thread?
    private void fillData() {
        fetchName();
        fetchBtClass();
        updateProfiles();
        migratePhonebookPermissionChoice();
        migrateMessagePermissionChoice();
        fetchMessageRejectionCount();

        mVisible = false;
        dispatchAttributesChanged();
    }

    BluetoothDevice getDevice() {
        return mDevice;
    }

    String getName() {
        return mName;
    }

    /**
     * Populate name from BluetoothDevice.ACTION_FOUND intent
     */
    void setNewName(String name) {
        if (mName == null) {
            mName = name;
            if (mName == null || TextUtils.isEmpty(mName)) {
                mName = mDevice.getAddress();
            }
            dispatchAttributesChanged();
        }
    }

    /**
     * user changes the device name
     */
    void setName(String name) {
        if (!mName.equals(name)) {
            mName = name;
            ReflectUtil.invoke(mDevice, "setAlias", name);
//            mDevice.setAlias(name);
            dispatchAttributesChanged();
        }
    }

    void refreshName() {
        fetchName();
        dispatchAttributesChanged();
    }

    private void fetchName() {
    	 mName = (String) ReflectUtil.invoke(mDevice, "getAliasName");
//        mName = mDevice.getAliasName();

        if (TextUtils.isEmpty(mName)) {
            mName = mDevice.getAddress();
        }
    }

    void refresh() {
        dispatchAttributesChanged();
    }

    boolean isVisible() {
        return mVisible;
    }

    void setVisible(boolean visible) {
        if (mVisible != visible) {
            mVisible = visible;
            dispatchAttributesChanged();
        }
    }

    int getBondState() {
        return mDevice.getBondState();
    }

    void setRssi(short rssi) {
        if (mRssi != rssi) {
            mRssi = rssi;
            dispatchAttributesChanged();
        }
    }

    /**
     * Checks whether we are connected to this device (any profile counts).
     *
     * @return Whether it is connected.
     */
    boolean isConnected() {
        for (LocalBluetoothProfile profile : mProfiles) {
            int status = getProfileConnectionState(profile);
            if (status == BluetoothProfile.STATE_CONNECTED) {
                return true;
            }
        }

        return false;
    }

    boolean isConnectedProfile(LocalBluetoothProfile profile) {
        int status = getProfileConnectionState(profile);
        return status == BluetoothProfile.STATE_CONNECTED;

    }

    boolean isBusy() {
        for (LocalBluetoothProfile profile : mProfiles) {
            int status = getProfileConnectionState(profile);
            if (status == BluetoothProfile.STATE_CONNECTING || status == BluetoothProfile.STATE_DISCONNECTING) {
                return true;
            }
        }
        return getBondState() == BluetoothDevice.BOND_BONDING;
    }

    /**
     * Fetches a new value for the cached BT class.
     */
    private void fetchBtClass() {
        mBtClass = mDevice.getBluetoothClass();
    }

    private boolean updateProfiles() {
        ParcelUuid[] uuids = mDevice.getUuids();
        if (uuids == null)
            return false;

        ParcelUuid[] localUuids = mManager.getUuids();
        if (localUuids == null)
            return false;

        /**
         * Now we know if the device supports PBAP, update permissions...
         */
        processPhonebookAccess();

        mProfileManager.updateProfiles(uuids, localUuids, mProfiles, mRemovedProfiles, mLocalNapRoleConnected, mDevice);

        return true;
    }

    /**
     * Refreshes the UI for the BT class, including fetching the latest value
     * for the class.
     */
    void refreshBtClass() {
        fetchBtClass();
        dispatchAttributesChanged();
    }

    /**
     * Refreshes the UI when framework alerts us of a UUID change.
     */
    void onUuidChanged() {
        updateProfiles();

        /*
         * If a connect was attempted earlier without any UUID, we will do the
         * connect now.
         */
        if (!mProfiles.isEmpty()
                && (mConnectAttempted + MAX_UUID_DELAY_FOR_AUTO_CONNECT) > SystemClock.elapsedRealtime()) {
            connectWithoutResettingTimer(false);
        }
        dispatchAttributesChanged();
    }

    void onBondingStateChanged(int bondState) {
        if (bondState == BluetoothDevice.BOND_NONE) {
            mProfiles.clear();
            mConnectAfterPairing = false; // cancel auto-connect
            setPhonebookPermissionChoice(ACCESS_UNKNOWN);
            setMessagePermissionChoice(ACCESS_UNKNOWN);
            mMessageRejectionCount = 0;
            saveMessageRejectionCount();
        }

        refresh();

        if (bondState == BluetoothDevice.BOND_BONDED) {
        	boolean result = (boolean)ReflectUtil.invoke(mDevice, "isBluetoothDock");
        	
            if (result) {
                onBondingDockConnect();
            } else if (mConnectAfterPairing) {
                connect(false);
            }
            mConnectAfterPairing = false;
        }
    }

    void setBtClass(BluetoothClass btClass) {
        if (btClass != null && mBtClass != btClass) {
            mBtClass = btClass;
            dispatchAttributesChanged();
        }
    }

    BluetoothClass getBtClass() {
        return mBtClass;
    }

    List<LocalBluetoothProfile> getProfiles() {
        return Collections.unmodifiableList(mProfiles);
    }

    List<LocalBluetoothProfile> getConnectableProfiles() {
        List<LocalBluetoothProfile> connectableProfiles = new ArrayList<LocalBluetoothProfile>();
        for (LocalBluetoothProfile profile : mProfiles) {
            if (profile.isConnectable()) {
                connectableProfiles.add(profile);
            }
        }
        return connectableProfiles;
    }

    List<LocalBluetoothProfile> getRemovedProfiles() {
        return mRemovedProfiles;
    }

    void registerCallback(Callback callback) {
        synchronized (mCallbacks) {
            mCallbacks.add(callback);
        }
    }

    void unregisterCallback(Callback callback) {
        synchronized (mCallbacks) {
            mCallbacks.remove(callback);
        }
    }

    private void dispatchAttributesChanged() {
        synchronized (mCallbacks) {
            for (Callback callback : mCallbacks) {
                callback.onDeviceAttributesChanged();
            }
        }
    }

    @Override
    public String toString() {
        return mDevice.toString();
    }

    @Override
    public boolean equals(Object o) {
        if ((o == null) || !(o instanceof CachedBluetoothDevice)) {
            return false;
        }
        return mDevice.equals(((CachedBluetoothDevice) o).mDevice);
    }

    @Override
    public int hashCode() {
        return mDevice.getAddress().hashCode();
    }

    // This comparison uses non-final fields so the sort order may change
    // when device attributes change (such as bonding state). Settings
    // will completely refresh the device list when this happens.
    public int compareTo(CachedBluetoothDevice another) {
        // Connected above not connected
        int comparison = (another.isConnected() ? 1 : 0) - (isConnected() ? 1 : 0);
        if (comparison != 0)
            return comparison;

        // Paired above not paired
        comparison = (another.getBondState() == BluetoothDevice.BOND_BONDED ? 1 : 0)
                - (getBondState() == BluetoothDevice.BOND_BONDED ? 1 : 0);
        if (comparison != 0)
            return comparison;

        // Visible above not visible
        comparison = (another.mVisible ? 1 : 0) - (mVisible ? 1 : 0);
        if (comparison != 0)
            return comparison;

        // Stronger signal above weaker signal
        comparison = another.mRssi - mRssi;
        if (comparison != 0)
            return comparison;

        // Fallback on name
        return mName.compareTo(another.mName);
    }

    public interface Callback {
        void onDeviceAttributesChanged();
    }

    int getPhonebookPermissionChoice() {
    	int permission = (int)ReflectUtil.invoke(mDevice, "getPhonebookAccessPermission");
//        int permission = mDevice.getPhonebookAccessPermission();
    	int ACCESS_ALLOWED = (int)ReflectUtil.getField(BluetoothDevice.class, "ACCESS_ALLOWED");
    	int ACCESS_REJECTED = (int)ReflectUtil.getField(BluetoothDevice.class, "ACCESS_REJECTED");
        if (permission == ACCESS_ALLOWED) {
            return ACCESS_ALLOWED;
        } else if (permission == ACCESS_REJECTED) {
            return ACCESS_REJECTED;
        }
        return ACCESS_UNKNOWN;
    }

    void setPhonebookPermissionChoice(int permissionChoice) {
    	int ACCESS_UNKNOWN = (int)ReflectUtil.getField(BluetoothDevice.class, "ACCESS_UNKNOWN");
    	int ACCESS_ALLOWED = (int)ReflectUtil.getField(BluetoothDevice.class, "ACCESS_ALLOWED");
    	int ACCESS_REJECTED = (int)ReflectUtil.getField(BluetoothDevice.class, "ACCESS_REJECTED");
        int permission = ACCESS_UNKNOWN;
        if (permissionChoice == ACCESS_ALLOWED) {
            permission = ACCESS_ALLOWED;
        } else if (permissionChoice == ACCESS_REJECTED) {
            permission = ACCESS_REJECTED;
        }
        ReflectUtil.invoke(mDevice, "setPhonebookAccessPermission", permission);
//        mDevice.setPhonebookAccessPermission(permission);
    }

    // Migrates data from old data store (in Settings app's shared preferences)
    // to new (in Bluetooth
    // app's shared preferences).
    private void migratePhonebookPermissionChoice() {
        SharedPreferences preferences = mContext.getSharedPreferences("bluetooth_phonebook_permission",
                Context.MODE_PRIVATE);
        if (!preferences.contains(mDevice.getAddress())) {
            return;
        }
        int ACCESS_UNKNOWN = (int)ReflectUtil.getField(BluetoothDevice.class, "ACCESS_UNKNOWN");
    	int ACCESS_ALLOWED = (int)ReflectUtil.getField(BluetoothDevice.class, "ACCESS_ALLOWED");
    	int ACCESS_REJECTED = (int)ReflectUtil.getField(BluetoothDevice.class, "ACCESS_REJECTED");
    	int accessPermission = (int)ReflectUtil.invoke(mDevice, "getPhonebookAccessPermission");
        if (accessPermission == ACCESS_UNKNOWN) {
            int oldPermission = preferences.getInt(mDevice.getAddress(), ACCESS_UNKNOWN);
            if (oldPermission == ACCESS_ALLOWED) {
//                mDevice.setPhonebookAccessPermission(ACCESS_ALLOWED);
                ReflectUtil.invoke(mDevice, "setPhonebookAccessPermission", ACCESS_ALLOWED);
            } else if (oldPermission == ACCESS_REJECTED) {
//                mDevice.setPhonebookAccessPermission(ACCESS_REJECTED);
                ReflectUtil.invoke(mDevice, "setPhonebookAccessPermission", ACCESS_REJECTED);
            }
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(mDevice.getAddress());
        editor.commit();
    }

    int getMessagePermissionChoice() {
    	int ACCESS_ALLOWED = (int)ReflectUtil.getField(BluetoothDevice.class, "ACCESS_ALLOWED");
    	int ACCESS_REJECTED = (int)ReflectUtil.getField(BluetoothDevice.class, "ACCESS_REJECTED");
    	int accessPermission = (int)ReflectUtil.invoke(mDevice, "getMessageAccessPermission");
        int permission = accessPermission;
        if (permission == ACCESS_ALLOWED) {
            return ACCESS_ALLOWED;
        } else if (permission == ACCESS_REJECTED) {
            return ACCESS_REJECTED;
        }
        return ACCESS_UNKNOWN;
    }

    void setMessagePermissionChoice(int permissionChoice) {
    	 int ACCESS_UNKNOWN = (int)ReflectUtil.getField(BluetoothDevice.class, "ACCESS_UNKNOWN");
     	int ACCESS_ALLOWED = (int)ReflectUtil.getField(BluetoothDevice.class, "ACCESS_ALLOWED");
     	int ACCESS_REJECTED = (int)ReflectUtil.getField(BluetoothDevice.class, "ACCESS_REJECTED");
    
        int permission = ACCESS_UNKNOWN;
        if (permissionChoice == ACCESS_ALLOWED) {
            permission = ACCESS_ALLOWED;
        } else if (permissionChoice == ACCESS_REJECTED) {
            permission = ACCESS_REJECTED;
        }
     	ReflectUtil.invoke(mDevice, "setMessageAccessPermission",permission);
//        mDevice.setMessageAccessPermission(permission);
    }

    // Migrates data from old data store (in Settings app's shared preferences)
    // to new (in Bluetooth
    // app's shared preferences).
    private void migrateMessagePermissionChoice() {
        SharedPreferences preferences = mContext.getSharedPreferences("bluetooth_message_permission",
                Context.MODE_PRIVATE);
        if (!preferences.contains(mDevice.getAddress())) {
            return;
        }
        int ACCESS_UNKNOWN = (int)ReflectUtil.getField(BluetoothDevice.class, "ACCESS_UNKNOWN");
     	int ACCESS_ALLOWED = (int)ReflectUtil.getField(BluetoothDevice.class, "ACCESS_ALLOWED");
     	int ACCESS_REJECTED = (int)ReflectUtil.getField(BluetoothDevice.class, "ACCESS_REJECTED");
     	int accessPermission = (int)ReflectUtil.invoke(mDevice, "getMessageAccessPermission");
        if (accessPermission ==  ACCESS_UNKNOWN) {
            int oldPermission = preferences.getInt(mDevice.getAddress(), ACCESS_UNKNOWN);
            if (oldPermission == ACCESS_ALLOWED) {
//                mDevice.setMessageAccessPermission(ACCESS_ALLOWED);
                ReflectUtil.invoke(mDevice, "setMessageAccessPermission",ACCESS_ALLOWED);
            } else if (oldPermission == ACCESS_REJECTED) {
//                mDevice.setMessageAccessPermission(ACCESS_REJECTED);
                ReflectUtil.invoke(mDevice, "setMessageAccessPermission",ACCESS_REJECTED);
            }
        }

        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(mDevice.getAddress());
        editor.commit();
    }

    /**
     * @return Whether this rejection should persist.
     */
    boolean checkAndIncreaseMessageRejectionCount() {
        if (mMessageRejectionCount < MESSAGE_REJECTION_COUNT_LIMIT_TO_PERSIST) {
            mMessageRejectionCount++;
            saveMessageRejectionCount();
        }
        return mMessageRejectionCount >= MESSAGE_REJECTION_COUNT_LIMIT_TO_PERSIST;
    }

    private void fetchMessageRejectionCount() {
        SharedPreferences preference = mContext.getSharedPreferences(MESSAGE_REJECTION_COUNT_PREFS_NAME,
                Context.MODE_PRIVATE);
        mMessageRejectionCount = preference.getInt(mDevice.getAddress(), 0);
    }

    private void saveMessageRejectionCount() {
        SharedPreferences.Editor editor = mContext
                .getSharedPreferences(MESSAGE_REJECTION_COUNT_PREFS_NAME, Context.MODE_PRIVATE).edit();
        if (mMessageRejectionCount == 0) {
            editor.remove(mDevice.getAddress());
        } else {
            editor.putInt(mDevice.getAddress(), mMessageRejectionCount);
        }
        editor.commit();
    }

    private void processPhonebookAccess() {
        if (mDevice.getBondState() != BluetoothDevice.BOND_BONDED)
            return;

        ParcelUuid[] uuids = mDevice.getUuids();
    }
}
