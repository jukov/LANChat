package org.jukov.lanchat.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import org.jukov.lanchat.MainActivity;
import org.jukov.lanchat.R;

/**
 * Created by jukov on 20.04.2016.
 */
public abstract class ChatFragment extends BaseFragment {

    protected ListView listViewMessages;
    protected Button buttonSend;
    protected EditText editTextMessage;

    protected ArrayAdapter<String> arrayAdapterMessages;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAdapter();
    }

    protected void initViews() {
        listViewMessages = (ListView) layout.findViewById(R.id.frChatMessageList);
        buttonSend = (Button) layout.findViewById(R.id.frChatSendButton);
        editTextMessage = (EditText) layout.findViewById(R.id.frChatMessageText);

        listViewMessages.setAdapter(arrayAdapterMessages);
    }

    protected void initAdapter() {
        arrayAdapterMessages = ((MainActivity) getActivity()).getArrayAdapterMessages();
    }

}
