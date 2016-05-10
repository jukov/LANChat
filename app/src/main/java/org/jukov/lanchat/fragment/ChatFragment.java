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

    protected ListView listViewMessages;
    protected ImageButton buttonSend;
    protected EditText editTextMessage;

    protected ChatAdapter chatAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAdapter();
    }

    protected String getMessageText() {
        return editTextMessage.getText().toString();
    }

    protected void initViews() {
        listViewMessages = (ListView) layout.findViewById(R.id.frChatMessageList);
        buttonSend = (ImageButton) layout.findViewById(R.id.frChatSendButton);
        editTextMessage = (EditText) layout.findViewById(R.id.frChatMessageText);

        listViewMessages.setAdapter(chatAdapter);
    }

    protected void initAdapter() {
        chatAdapter = ((NavigationDrawerActivity) getActivity()).getChatAdapter();
    }

}
