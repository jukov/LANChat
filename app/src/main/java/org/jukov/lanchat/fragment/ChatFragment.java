package org.jukov.lanchat.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.jukov.lanchat.R;
import org.jukov.lanchat.activity.MainActivity;
import org.jukov.lanchat.service.ServiceHelper;
import org.jukov.lanchat.util.IntentStrings;

/**
 * Created by jukov on 15.02.2016.
 */
public class ChatFragment extends BaseFragment {

    private ListView listViewMessages;
    private Button buttonSend;
    private EditText editTextMessage;

    private ArrayAdapter<String> arrayAdapterMessages;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(getClass().getSimpleName(), "onCreate()");
        setTitle(getString(R.string.global_chat));
        initAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(getClass().getSimpleName(), "onCreateView()");

        layout = inflater.inflate(R.layout.chat_layout, container, false);

        initViews();

        return layout;
    }

    private void initViews() {
        listViewMessages = (ListView) layout.findViewById(R.id.listViewMessages);
        buttonSend = (Button) layout.findViewById(R.id.buttonSend);
        editTextMessage = (EditText) layout.findViewById(R.id.editTextMessage);

        listViewMessages.setAdapter(arrayAdapterMessages);


        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServiceHelper.sendMessage(getActivity(), editTextMessage.getText().toString());
                editTextMessage.setText("");
            }
        });
    }

    private void initAdapter() {
        arrayAdapterMessages = ((MainActivity) getActivity()).getArrayAdapterMessages();
    }
}
