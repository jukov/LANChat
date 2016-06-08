package org.jukov.lanchat.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.widget.Toast;

import org.jukov.lanchat.MainActivity;
import org.jukov.lanchat.R;
import org.jukov.lanchat.service.ServiceHelper;
import org.jukov.lanchat.util.Base64Converter;
import org.jukov.lanchat.util.StorageHelper;

import java.io.IOException;

import static org.jukov.lanchat.util.PreferenceConstants.ENABLE_LED;
import static org.jukov.lanchat.util.PreferenceConstants.ENABLE_NOTIFICATIONS;
import static org.jukov.lanchat.util.PreferenceConstants.ENABLE_VIBRATION;
import static org.jukov.lanchat.util.PreferenceConstants.NOTIFICATION_RINGTONE;
import static org.jukov.lanchat.util.PreferenceConstants.PROFILE_PICTURE;

/**
 * Created by jukov on 17.02.2016.
 */
public class SettingsFragment extends PreferenceFragmentCompat {

    public static final int REQUEST_CODE_ALERT_RINGTONE = 21;
    public static final int REQUEST_CODE_PROFILE_PICTURE_GALLERY = 22;
    public static final int REQUEST_CODE_PROFILE_PICTURE_CAMERA = 24;
    public static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 23;

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
        switch (preference.getKey()){
            case NOTIFICATION_RINGTONE:
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
            case PROFILE_PICTURE:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.choose_action))
                        .setItems(new String[]{
                                getString(R.string.choose_from_gallery),
                                getString(R.string.take_a_picture)}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                switch (which) {
                                    case 0:
                                        intent.setAction(Intent.ACTION_PICK);
                                        intent.setType("image/*");
                                        startActivityForResult(intent, REQUEST_CODE_PROFILE_PICTURE_GALLERY);
                                        break;
                                    case 1:
                                        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                                        startActivityForResult(intent, REQUEST_CODE_PROFILE_PICTURE_CAMERA);
                                        break;
                                }
                            }
                        });
                builder.create().show();
                return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap bitmap;
        switch (requestCode) {
                case REQUEST_CODE_ALERT_RINGTONE:
                    if (data != null) {
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
                    }
                    return;
                case REQUEST_CODE_PROFILE_PICTURE_GALLERY:
                    if (data != null) {
                        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                Toast.makeText(getContext(), "explanation", Toast.LENGTH_LONG).show();
                                return;
                            } else {
                                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
                            }
                        }
                        Uri imageUri = data.getData();
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);

                            if (bitmap.getWidth() > 256 || bitmap.getHeight() > 256 )
                                bitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true);

                            StorageHelper.storeProfilePicture(getContext(), bitmap, "profile_picture.jpg");

                            ((MainActivity) getActivity()).setProfilePicture(bitmap);
                            ServiceHelper.changeProfilePicture(getContext(), Base64Converter.bitmapToString(bitmap));

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return;
                case REQUEST_CODE_PROFILE_PICTURE_CAMERA:
                    if (data.hasExtra("data")) {
                        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                                Toast.makeText(getContext(), "explanation", Toast.LENGTH_LONG).show();
                                return;
                            } else {
                                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
                            }
                        }
                        bitmap = (Bitmap) data.getExtras().get("data");

                        if (bitmap != null && (bitmap.getWidth() > 256 || bitmap.getHeight() > 256 ))
                            bitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true);

                        StorageHelper.storeProfilePicture(getContext(), bitmap, "profile_picture.jpg");

                        ((MainActivity) getActivity()).setProfilePicture(bitmap);
                        ServiceHelper.changeProfilePicture(getContext(), Base64Converter.bitmapToString(bitmap));
                    }
                    return;
            }
        super.onActivityResult(requestCode, resultCode, data);
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
