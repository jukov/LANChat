package org.jukov.lanchat.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.jukov.lanchat.R;

/**
 * Created by jukov on 16.02.2016.
 */
public class RoomsFragment extends ListFragment {

    public static RoomsFragment newInstance(Context context) {

        Bundle args = new Bundle();

        RoomsFragment fragment = new RoomsFragment();
        fragment.setTitle(context.getString(R.string.rooms));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_rooms, container, false);
        initViews();
        return layout;
    }

    private void initViews() {
        listView = (ListView) layout.findViewById(R.id.frList);
        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(R.id.frFab);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(getContext(), PrivateChatActivity.class);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(getContext(), PrivateChatActivity.class);
            }
        });
    }
}
