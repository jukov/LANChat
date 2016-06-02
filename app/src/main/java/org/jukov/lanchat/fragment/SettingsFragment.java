package org.jukov.lanchat.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import org.jukov.lanchat.R;

import static org.jukov.lanchat.util.PreferenceConstants.ENABLE_LED;
import static org.jukov.lanchat.util.PreferenceConstants.ENABLE_NOTIFICATIONS;
import static org.jukov.lanchat.util.PreferenceConstants.ENABLE_VIBRATION;
import static org.jukov.lanchat.util.PreferenceConstants.NOTIFICATION_RINGTONE;

/**
 * Created by jukov on 17.02.2016.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    private static final int REQUEST_CODE_ALERT_RINGTONE = 21;

    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener;

    private Preference ringtonePreferenceValue;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);

        final Preference notificationsPreference = findPreference(ENABLE_NOTIFICATIONS);
        ringtonePreferenceValue = findPreference(NOTIFICATION_RINGTONE);
        final Preference vibrationPreference = findPreference(ENABLE_VIBRATION);
        final Preference ledPreference = findPreference(ENABLE_LED);

        Ringtone ringtone = RingtoneManager.getRingtone(getContext(), Uri.parse(getRingtonePreferenceValue()));
        ringtonePreferenceValue.setSummary(ringtone.getTitle(getContext()));

        if (((CheckBoxPreference) notificationsPreference).isChecked()) {
            ringtonePreferenceValue.setEnabled(true);
            vibrationPreference.setEnabled(true);
            ledPreference.setEnabled(true);
        } else {
            ringtonePreferenceValue.setEnabled(false);
            vibrationPreference.setEnabled(false);
            ledPreference.setEnabled(false);
        }

        notificationsPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if (((CheckBoxPreference) preference).isChecked()) {
                    ringtonePreferenceValue.setEnabled(false);
                    vibrationPreference.setEnabled(false);
                    ledPreference.setEnabled(false);
                } else {
                    ringtonePreferenceValue.setEnabled(true);
                    vibrationPreference.setEnabled(true);
                    ledPreference.setEnabled(true);
                }
                return true;
            }
        });
    }


    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference.getKey().equals(NOTIFICATION_RINGTONE)) {
            Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);

            String existingValue = getRingtonePreferenceValue();
            if (existingValue != null) {
                if (existingValue.length() == 0) {
                    // Select "Silent"
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
                } else {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existingValue));
                }
            } else {
                // No ringtone has been selected, set to the default
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
            }

            startActivityForResult(intent, REQUEST_CODE_ALERT_RINGTONE);
            return true;
        } else {
            return super.onPreferenceTreeClick(preference);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ALERT_RINGTONE && data != null) {
            Uri ringtoneUri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            if (ringtoneUri != null) {
                Ringtone ringtone = RingtoneManager.getRingtone(getContext(), ringtoneUri);
                ringtonePreferenceValue.setSummary(ringtone.getTitle(getContext()));
                setRingtonPreferenceValue(ringtoneUri.toString());
            } else {
                // "Silent" was selected
                ringtonePreferenceValue.setSummary("None");
                setRingtonPreferenceValue("");
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
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

    public String getRingtonePreferenceValue() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sharedPreferences.getString(NOTIFICATION_RINGTONE, "");
    }

    public void setRingtonPreferenceValue(String ringtonePreferenceValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(NOTIFICATION_RINGTONE, ringtonePreferenceValue);
        editor.apply();
    }
}
