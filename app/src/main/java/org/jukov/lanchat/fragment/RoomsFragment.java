package org.jukov.lanchat.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.jukov.lanchat.BaseActivity;
import org.jukov.lanchat.MainActivity;
import org.jukov.lanchat.R;
import org.jukov.lanchat.RoomChatActivity;
import org.jukov.lanchat.RoomCreatingActivity;
import org.jukov.lanchat.dto.RoomData;

import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_NAME;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_UID;

/**
 * Created by jukov on 16.02.2016.
 */
public class RoomsFragment extends ListFragment {

    MainActivity mainActivity;
    private ArrayAdapter<RoomData> arrayAdapter;

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
        mainActivity = (MainActivity) getActivity();
        initAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_rooms, container, false);
        initViews();
        return layout;
    }

    private void initAdapter() {
        arrayAdapter = ((MainActivity) getActivity()).getArrayAdapterRooms();
    }

    private void initViews() {
        listView = (ListView) layout.findViewById(R.id.frList);
        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(R.id.frFab);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RoomData roomData = arrayAdapter.getItem(position);
                Intent intent = new Intent(getContext(), RoomChatActivity.class);
                intent.putExtra(EXTRA_NAME, roomData.getName());
                intent.putExtra(EXTRA_UID, roomData.getUid());
                getActivity().startActivityForResult(intent, BaseActivity.REQUEST_CODE_ROOM_CHAT);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), RoomCreatingActivity.class);
                getActivity().startActivityForResult(intent, BaseActivity.REQUEST_CODE_ROOM_CREATING);
            }
        });
    }
}
