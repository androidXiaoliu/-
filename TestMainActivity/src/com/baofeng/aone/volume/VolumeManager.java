package com.baofeng.aone.volume;

import com.baofeng.aone.AndroidManager;
import com.baofeng.aone.LauncherApplication;
import com.baofeng.aone.display.DisplayManager;

import android.content.Context;
import android.media.AudioManager;

/**
 * volume controller
 *
 * @author donghuajie
 *
 */
public class VolumeManager extends AndroidManager {

    private static AudioManager mAudioManager;

    public static VolumeManager mVolumeManager;

    public static AndroidManager getAndroidManager() {
        return getVolumeManager();
    }

    public static synchronized VolumeManager getVolumeManager() {
        if (mVolumeManager == null) {
            mVolumeManager = new VolumeManager();
        }
        return mVolumeManager;
    }

    static {
        mAudioManager = (AudioManager) LauncherApplication.getInstance()
                .getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * set the volume value to the system
     *
     * @param volume
     *            the value that you want to set
     */
    public static void setVolumeChange(String volume) {
        mAudioManager = (AudioManager) LauncherApplication.getInstance()
                .getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, Integer.parseInt(volume),
                AudioManager.FLAG_PLAY_SOUND);
    }

    /**
     * get the voluem value
     *
     * @return a array contains current value and total value int [0] current
     *         volume value int [1] total volume value
     */
    public static String getVolumeValue() {
        String current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)+"";
        String total = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)+"";
        return current+","+total;
    }
}
