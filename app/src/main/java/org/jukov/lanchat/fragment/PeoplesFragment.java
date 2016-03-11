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

import org.jukov.lanchat.MessagingActivity;
import org.jukov.lanchat.R;
import org.jukov.lanchat.MainActivity;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.util.IntentStrings;

/**
 * Created by jukov on 16.02.2016.
 */
public class PeoplesFragment extends BaseFragment {

    MainActivity mainActivity;

    private ArrayAdapter<PeopleData> arrayAdapterPeoples;

    public static PeoplesFragment newInstance(Context context) {

        Bundle args = new Bundle();

        PeoplesFragment fragment = new PeoplesFragment();
        fragment.setTitle(context.getString(R.string.peoples));
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

        listViewPeoples.setAdapter(arrayAdapterPeoples);

        listViewPeoples.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), MessagingActivity.class);
                intent.putExtra(IntentStrings.EXTRA_NAME, arrayAdapterPeoples.getItem(position).getName());
                startActivity(intent);
            }
        });

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
