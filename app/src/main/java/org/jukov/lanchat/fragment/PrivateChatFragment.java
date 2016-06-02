package org.jukov.lanchat.fragment;

import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jukov.lanchat.PrivateChatActivity;
import org.jukov.lanchat.R;
import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.service.ServiceHelper;

import static org.jukov.lanchat.dto.ChatData.MessageType.PRIVATE;

/**
 * Created by jukov on 26.02.2016.
 */
public class PrivateChatFragment extends ChatFragment {

    private PrivateChatActivity privateChatActivity;

    public static PrivateChatFragment newInstance() {
        return new PrivateChatFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        privateChatActivity = (PrivateChatActivity) getActivity();
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
                if (editTextMessage.getText().length() > 0)
                    ServiceHelper.sendMessage(getContext(),
                            new ChatData(
                                    getContext(),
                                    PRIVATE,
                                    getMessageText(),
                                    privateChatActivity.getCompanionUID()));
                editTextMessage.setText("");
            }
        });
    }
}
