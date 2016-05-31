package org.jukov.lanchat.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.jukov.lanchat.BaseActivity;
import org.jukov.lanchat.MainActivity;
import org.jukov.lanchat.PrivateChatActivity;
import org.jukov.lanchat.R;
import org.jukov.lanchat.adapter.PeopleAdapter;
import org.jukov.lanchat.db.DBHelper;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.service.ServiceHelper;

/**
 * Created by jukov on 16.02.2016.
 */
public class PeopleFragment extends ListFragment {

    private PeopleAdapter peopleAdapter;

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
        layout = inflater.inflate(R.layout.fragment_people, container, false);
        initViews();
        return layout;
    }

    private void initViews() {
        listView = (ListView) layout.findViewById(R.id.listViewPeople);

        listView.setAdapter(peopleAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getContext(), PrivateChatActivity.class);
                PeopleData peopleData = peopleAdapter.getItem(position);
                intent.putExtra(ServiceHelper.IntentConstants.EXTRA_NAME, peopleData.getName());
                intent.putExtra(ServiceHelper.IntentConstants.EXTRA_UID, peopleData.getUid());
                intent.putExtra(ServiceHelper.IntentConstants.EXTRA_PEOPLE_AROUND, mainActivity.getPeopleAround());
                getActivity().startActivityForResult(intent, BaseActivity.REQUEST_CODE_PRIVATE_CHAT);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.choose_action))
                        .setItems(new String[] {getString(R.string.delete_messages)}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).setTitle(getString(R.string.you_sure))
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                PeopleData peopleData = peopleAdapter.getItem(position);
                                                DBHelper dbHelper = DBHelper.getInstance(getContext());
                                                dbHelper.deleteMessages(peopleData);
                                            }
                                        })
                                        .setNegativeButton(android.R.string.cancel, null)
                                        .create();
                                alertDialog.show();
                            }
                        });
                builder.create().show();
                return true;
            }
        });
    }

    private void initAdapter() {
        peopleAdapter = ((MainActivity) getActivity()).getPeopleAdapter();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(getClass().getSimpleName(), "onSaveInstanceState()");
    }

}
