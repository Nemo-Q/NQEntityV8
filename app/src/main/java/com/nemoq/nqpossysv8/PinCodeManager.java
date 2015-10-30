package com.nemoq.nqpossysv8;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Base64;

import java.nio.charset.Charset;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * Class for checking the stored pin and changing it.
 * Created by Martin Backudd on 2015-10-28.
 */
public class PinCodeManager {

    private static PinCodeManager pinCodeManager;
    private Context context;


    private PinCodeManager(Context ctx) {

        context = ctx;


    }

    public static synchronized PinCodeManager getInstance(Context ctx) {


        if (pinCodeManager == null) {

            pinCodeManager = new PinCodeManager(ctx);
        }


        return pinCodeManager;
    }


    public boolean checkPin(String pin) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        String preferenceKey = context.getString(R.string.pref_key_pin);
        String storedPin = sharedPreferences.getString(preferenceKey, "");
        String defaultPin = context.getString(R.string.default_pin);
        if (pin.equals(storedPin) || pin.equals(defaultPin))
            return true;
        else
            return false;


    }

    public void storePin(String pin){

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(context.getString(R.string.pref_key_pin), pin);
        editor.apply();


    }

}