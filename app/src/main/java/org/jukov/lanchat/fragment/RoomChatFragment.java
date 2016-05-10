package org.jukov.lanchat.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jukov.lanchat.R;
import org.jukov.lanchat.RoomChatActivity;
import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.service.ServiceHelper;

/**
 * Created by jukov on 25.04.2016.
 */
public class RoomChatFragment extends ChatFragment {

    RoomChatActivity roomChatActivity;

    public static RoomChatFragment newInstance() {
        return new RoomChatFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        roomChatActivity = (RoomChatActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_chat, container, false);

        initViews();

        return layout;
    }

    @Override
    protected void initViews() {
        super.initViews();

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextMessage.getText().length() > 0) {
                    ServiceHelper.sendMessage(getContext(),
                            new ChatData(
                                    getContext(),
                                    ChatData.MessageType.ROOM,
                                    getMessageText(),
                                    roomChatActivity.getRoomUid()));
                    editTextMessage.setText("");
                }
            }
        });
    }
}
