package org.jukov.lanchat.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.jukov.lanchat.R;
import org.jukov.lanchat.activity.MainActivity;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.util.IntentStrings;

/**
 * Created by jukov on 16.02.2016.
 */
public class PeopleListFragment extends BaseFragment {

    private ArrayAdapter<PeopleData> arrayAdapterPeoples;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getClass().getSimpleName(), "onCreate()");
        setTitle(getString(R.string.peoples));
        initAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.peoples_list_layout, container, false);
        ListView listViewPeoples = (ListView) layout.findViewById(R.id.listViewPeoples);

        listViewPeoples.setAdapter(arrayAdapterPeoples);

        return layout;
    }

    private void initAdapter() {
        arrayAdapterPeoples = ((MainActivity) getActivity()).getArrayAdapterPeoples();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(getClass().getSimpleName(), "onSaveInstanceState()");
    }

}
