package org.jukov.lanchat.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import org.jukov.lanchat.R;

/**
 * Created by jukov on 17.02.2016.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            sharedPreferenceChangeListener = (SharedPreferences.OnSharedPreferenceChangeListener) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }
}
