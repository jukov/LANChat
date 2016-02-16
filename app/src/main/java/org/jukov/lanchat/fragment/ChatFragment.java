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
import android.widget.TextView;

import org.jukov.lanchat.R;
import org.jukov.lanchat.service.LANChatService;
import org.jukov.lanchat.util.IntentStrings;

/**
 * Created by jukov on 15.02.2016.
 */
public class ChatFragment extends BaseFragment {

    private ListView listViewMessages;
    private Button buttonSend;
    private EditText editTextMessage;
    private TextView textViewDebug;

    private ArrayAdapter<String> arrayAdapterMessages;

    private BroadcastReceiver broadcastReceiver;
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        layout = inflater.inflate(R.layout.chat_layout, container, false);

        setTitle(getString(R.string.global_chat));
        initViews();
        initService();
        initBroadcastReceiver();

        return layout;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(broadcastReceiver);
        Log.d(getClass().getSimpleName(), "onDestroy()");
    }

    private void initViews() {
        listViewMessages = (ListView) layout.findViewById(R.id.listViewMessages);
        buttonSend = (Button) layout.findViewById(R.id.buttonSend);
        editTextMessage = (EditText) layout.findViewById(R.id.editTextMessage);
        textViewDebug = (TextView) layout.findViewById(R.id.textViewDebug);

        arrayAdapterMessages = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        listViewMessages.setAdapter(arrayAdapterMessages);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), LANChatService.class);
                intent.putExtra(IntentStrings.EXTRA_TYPE, IntentStrings.TYPE_MESSAGE);
                intent.putExtra(IntentStrings.EXTRA_MESSAGE, editTextMessage.getText().toString());
                getActivity().startService(intent);
            }
        });
    }

    private void initService() {
        Log.d(getClass().getSimpleName(), "Connecting to service");
        Intent intent = new Intent(getActivity(), LANChatService.class);
        intent.putExtra(IntentStrings.EXTRA_TYPE, IntentStrings.TYPE_CONNECT_TO_SERVICE);
        getActivity().startService(intent);
    }

    private void initBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                Log.d(getClass().getSimpleName(), "Receive message");
                if (intent.hasExtra(IntentStrings.EXTRA_TYPE)) {
                    Log.d(getClass().getSimpleName(), IntentStrings.EXTRA_TYPE);
                    switch (intent.getStringExtra(IntentStrings.EXTRA_TYPE)) {
                        case IntentStrings.TYPE_MESSAGE:
                            arrayAdapterMessages.add(intent.getStringExtra(IntentStrings.EXTRA_NAME) + ": " + intent.getStringExtra(IntentStrings.EXTRA_MESSAGE));
                            break;
                        case IntentStrings.TYPE_DEBUG:
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textViewDebug.setText(intent.getStringExtra(IntentStrings.EXTRA_DEBUG));
                                }
                            });
                            break;
                        default:
                            Log.d(getClass().getSimpleName(), "Unexpected intent type");
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(IntentStrings.BROADCAST_ACTION);
        getActivity().registerReceiver(broadcastReceiver, intentFilter);
    }
}
