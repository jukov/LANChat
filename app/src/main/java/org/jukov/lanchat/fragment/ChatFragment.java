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

import org.jukov.lanchat.PrivateMessagingActivity;
import org.jukov.lanchat.R;
import org.jukov.lanchat.service.ServiceHelper;

/**
 * Created by jukov on 26.02.2016.
 */
public class ChatFragment extends BaseFragment {

    private PrivateMessagingActivity privateMessagingActivity;

    private ListView listViewMessages;
    private Button buttonSend;
    private EditText editTextMessage;

    private ArrayAdapter<String> arrayAdapterMessages;

    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        privateMessagingActivity = (PrivateMessagingActivity) getActivity();

        initAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_chat, container, false);

        initViews();

        return layout;
    }

    private void initAdapter() {
        arrayAdapterMessages = privateMessagingActivity.getArrayAdapterMessages();
    }

    private void initViews() {
        listViewMessages = (ListView) layout.findViewById(R.id.frChatMessageList);
        buttonSend = (Button) layout.findViewById(R.id.frChatSendButton);
        editTextMessage = (EditText) layout.findViewById(R.id.frChatMessageText);

        listViewMessages.setAdapter(arrayAdapterMessages);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextMessage.getText().length() > 0)
                    ServiceHelper.sendMessage(
                            privateMessagingActivity,
                            ServiceHelper.MessageType.PRIVATE,
                            editTextMessage.getText().toString(),
                            privateMessagingActivity.getCompanionUID());
                editTextMessage.setText("");
            }
        });
    }
}
