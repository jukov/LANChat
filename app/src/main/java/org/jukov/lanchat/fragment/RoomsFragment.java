package org.jukov.lanchat.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.jukov.lanchat.BaseActivity;
import org.jukov.lanchat.MainActivity;
import org.jukov.lanchat.R;
import org.jukov.lanchat.RoomChatActivity;
import org.jukov.lanchat.RoomCreatingActivity;
import org.jukov.lanchat.adapter.RoomsAdapter;
import org.jukov.lanchat.db.DBHelper;
import org.jukov.lanchat.dto.RoomData;
import org.jukov.lanchat.service.ServiceHelper;

import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_ROOM;

/**
 * Created by jukov on 16.02.2016.
 */
public class RoomsFragment extends ListFragment {

    private RoomsAdapter roomsAdapter;

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
        roomsAdapter = ((MainActivity) getActivity()).getRoomsAdapter();
    }

    private void initViews() {
        listView = (ListView) layout.findViewById(R.id.listViewRooms);
        FloatingActionButton fab = (FloatingActionButton) layout.findViewById(R.id.fab);

        listView.setAdapter(roomsAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RoomData roomData = roomsAdapter.getItem(position);
                Intent intent = new Intent(getContext(), RoomChatActivity.class);
                intent.putExtra(EXTRA_ROOM, roomData);
                intent.putExtra(ServiceHelper.IntentConstants.EXTRA_PEOPLE_AROUND, mainActivity.getPeopleAround());
                getActivity().startActivityForResult(intent, BaseActivity.REQUEST_CODE_ROOM_CHAT);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.choose_action))
                        .setItems(new String[] {getString(R.string.delete_room), getString(R.string.rename_room)}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {
                                final RoomData roomData = roomsAdapter.getItem(position);
                                final DBHelper dbHelper = DBHelper.getInstance(getContext());
                                switch (which) {
                                    case 0:
                                        AlertDialog alertDialogDeletion = new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.you_sure))
                                                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dbHelper.deleteRoom(roomData);
                                                        roomsAdapter.remove(position);
                                                    }
                                                })
                                                .setNegativeButton(android.R.string.cancel, null)
                                                .create();
                                        alertDialogDeletion.show();
                                        break;
                                    case 1:
                                        final AlertDialog alertDialogRenaming = new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.type_new_name))
                                               .setView(getActivity().getLayoutInflater().inflate(R.layout.dialog_edtitext, null))
                                               .setPositiveButton(android.R.string.ok, null)
                                               .setNegativeButton(android.R.string.cancel, null).create();
                                        alertDialogRenaming.show();
                                        View button = (alertDialogRenaming.getButton(DialogInterface.BUTTON_POSITIVE));
                                        button.setOnClickListener(new View.OnClickListener() {
                                            @SuppressWarnings("ConstantConditions")
                                            @Override
                                            public void onClick(View v) {
                                                EditText editText = (EditText) alertDialogRenaming.findViewById(R.id.editTextName);
                                                String name = editText.getText().toString();
                                                if (name.length() > 0) {
                                                    roomData.setName(editText.getText().toString());
                                                    roomsAdapter.notifyDataSetChanged();
                                                    dbHelper.insertOrUpdateRoom(roomData);
                                                    ServiceHelper.sendRoom(getContext(), roomData);
                                                    alertDialogRenaming.dismiss();
                                                } else {
                                                    editText.setError(getString(R.string.incorrect_name));
                                                }
                                            }
                                        });
                                        break;
                                }

                            }
                        });
                builder.create().show();
                return true;
            }
        });

        SupplicantState supplicantState;
        WifiManager wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        supplicantState = wifiInfo.getSupplicantState();

        if (supplicantState != SupplicantState.COMPLETED) {
            fab.setEnabled(false);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), RoomCreatingActivity.class);
                if (roomsAdapter.getCount() <= 1000)
                    getActivity().startActivityForResult(intent, BaseActivity.REQUEST_CODE_ROOM_CREATING);
                else
                    Toast.makeText(getContext(), R.string.cant_create_room, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
