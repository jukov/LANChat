package org.jukov.lanchat.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import org.jukov.lanchat.NavigationDrawerActivity;
import org.jukov.lanchat.R;
import org.jukov.lanchat.adapter.ChatAdapter;

/**
 * Created by jukov on 20.04.2016.
 */
public abstract class ChatFragment extends BaseFragment {

    private ChatAdapter chatAdapter;

    EditText editTextMessage;
    ImageButton buttonSend;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAdapter();
    }

    String getMessageText() {
        return editTextMessage.getText().toString();
    }

    void initViews() {
        ListView listViewMessages = (ListView) layout.findViewById(R.id.listViewMessages);
        buttonSend = (ImageButton) layout.findViewById(R.id.imageButtonSend);
        editTextMessage = (EditText) layout.findViewById(R.id.editTextMessage);

        listViewMessages.setAdapter(chatAdapter);
    }

    private void initAdapter() {
        chatAdapter = ((NavigationDrawerActivity) getActivity()).getChatAdapter();
    }

}
