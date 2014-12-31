package br.com.estudiotrilha.apphooks.helper;

import android.app.Activity;
import android.content.Context;

import br.com.estudiotrilha.apphooks.ConfigureGcm;

/**
 * Created by mauricio on 12/30/14.
 */
public class Util {

    private static ConfigureGcm gcm;

    public static void configureGcm(Activity activity) {
        gcm = ConfigureGcm.getInstance();
        gcm.make(activity);
    }

    public static String getPushId(Context context) {
        gcm = ConfigureGcm.getInstance();
        return gcm.getRegistrationId(context);
    }
}
