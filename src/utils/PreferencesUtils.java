package utils;

import java.util.prefs.Preferences;

public class PreferencesUtils {
    public static final String KEY_HOST = "HOST";
    public static final String KEY_PORT = "PORT";
    public static final String KEY_CAPABILITIES = "CAPABILITIES";
    private static final Preferences prefs = Preferences.userRoot();

    public static void save(String key, String value) {
        prefs.put(key, value);
    }

    public static String load(String key) {
        return prefs.get(key, null);
    }

    public static void clear(String key) {
        prefs.remove(key);
    }
}
