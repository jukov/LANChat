package org.jukov.lanchat.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.jukov.lanchat.MainActivity;
import org.jukov.lanchat.PrivateChatActivity;
import org.jukov.lanchat.R;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.service.ServiceHelper;

/**
 * Created by jukov on 16.02.2016.
 */
public class PeopleFragment extends BaseFragment {

    MainActivity mainActivity;

    private ArrayAdapter<PeopleData> arrayAdapterPeople;

    public static PeopleFragment newInstance(Context context) {

        Bundle args = new Bundle();

        PeopleFragment fragment = new PeopleFragment();
        fragment.setTitle(context.getString(R.string.people));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        initAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_peoples, container, false);
        ListView listViewPeoples = (ListView) layout.findViewById(R.id.frPeoplesPeoplesList);

        listViewPeoples.setAdapter(arrayAdapterPeople);

        listViewPeoples.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), PrivateChatActivity.class);
                PeopleData peopleData = arrayAdapterPeople.getItem(position);
                intent.putExtra(ServiceHelper.IntentConstants.EXTRA_NAME, peopleData.getName());
                intent.putExtra(ServiceHelper.IntentConstants.EXTRA_UID, peopleData.getUid());
                startActivity(intent);
            }
        });

        return layout;
    }

    private void initAdapter() {
        arrayAdapterPeople = ((MainActivity) getActivity()).getArrayAdapterPeople();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(getClass().getSimpleName(), "onSaveInstanceState()");
    }

}
