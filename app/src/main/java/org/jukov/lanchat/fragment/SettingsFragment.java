package org.jukov.lanchat.fragment;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import org.jukov.lanchat.R;

/**
 * Created by jukov on 17.02.2016.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
    }
}
