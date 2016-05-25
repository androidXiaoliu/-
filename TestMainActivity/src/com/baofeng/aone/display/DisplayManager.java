package com.baofeng.aone.display;

import com.baofeng.aone.AndroidManager;
import com.baofeng.aone.LauncherApplication;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.Settings;

/**
 * display controller
 *
 * @author donghuajie
 *
 */
public class DisplayManager extends AndroidManager {
    /**
     * come from   \frameworks\base\core\res\res\values\config.xml
     * <integer name="config_screenBrightnessSettingMaximum">255</integer>
     */
    private static String BRIGHTNESS_MAX_VALUE = "255";
    public static DisplayManager mDisplayManager;

    public static AndroidManager getAndroidManager() {
        return getDisplayManager();
    }

    public static synchronized DisplayManager getDisplayManager() {
        if (mDisplayManager == null) {
            mDisplayManager = new DisplayManager();
        }
        return mDisplayManager;
    }
    /**
     * set the display value and save
     *
     * @param value
     */
    public static void setDisplayValue(String volume) {
        ContentResolver resolver = LauncherApplication.getInstance().getContentResolver();
        Uri uri = Settings.System.getUriFor("screen_brightness");
        Settings.System.putInt(resolver, "screen_brightness", Integer.parseInt(volume));
        resolver.notifyChange(uri, null);
    }

    /**
     * get the display value
     *
     * @return current display value
     */
    public static String getDisplayValue() {
        String value = "";
        String maxValue = "";
        try {
            value = Settings.System.getInt(LauncherApplication.getInstance()
                    .getContentResolver(), Settings.System.SCREEN_BRIGHTNESS)+"";
            maxValue = BRIGHTNESS_MAX_VALUE;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return value+","+maxValue;
    }

}
