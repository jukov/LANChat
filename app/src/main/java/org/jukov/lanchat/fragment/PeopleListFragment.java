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
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.util.IntentStrings;

/**
 * Created by jukov on 16.02.2016.
 */
public class PeopleListFragment extends BaseFragment {

    private BroadcastReceiver broadcastReceiver;

    private ArrayAdapter<PeopleData> arrayAdapterPeoples;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBroadcastReceiver();
        initAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.peoples_list_layout, container, false);

        setTitle(getString(R.string.peoples));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        ListView listViewPeoples = (ListView) layout.findViewById(R.id.listViewPeoples);

        listViewPeoples.setAdapter(arrayAdapterPeoples);

        return layout;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(broadcastReceiver);
        Log.d(getClass().getSimpleName(), "onDestroy()");
    }

    private void initAdapter() {
        if (arrayAdapterPeoples == null)
            arrayAdapterPeoples = new ArrayAdapter<>(
                    getActivity(), R.layout.people_listview_layout, R.id.textViewName);
    }

    private void initBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                Log.d(getClass().getSimpleName(), "Receive broadcast");
                if (intent.hasExtra(IntentStrings.EXTRA_NAME)) {
                    String name = intent.getStringExtra(IntentStrings.EXTRA_NAME);
                    String uid = intent.getStringExtra(IntentStrings.EXTRA_UID);
                    PeopleData peopleData = new PeopleData(name, uid);
                    if (arrayAdapterPeoples.getPosition(peopleData) == -1)
                        arrayAdapterPeoples.add(peopleData);
                    else
                        arrayAdapterPeoples.remove(peopleData);
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(IntentStrings.PEOPLES_ACTION);
        getActivity().registerReceiver(broadcastReceiver, intentFilter);
    }
}
