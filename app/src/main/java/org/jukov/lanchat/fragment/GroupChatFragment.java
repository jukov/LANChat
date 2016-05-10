package org.jukov.lanchat.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jukov.lanchat.R;
import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.service.ServiceHelper;

import static org.jukov.lanchat.dto.ChatData.MessageType.GLOBAL;

/**
 * Created by jukov on 15.02.2016.
 */
public class GroupChatFragment extends ChatFragment {

    public static GroupChatFragment newInstance(Context context) {
        Bundle args = new Bundle();

        GroupChatFragment fragment = new GroupChatFragment();
        fragment.setTitle(context.getString(R.string.global_chat));
        fragment.setArguments(args);
        return fragment;
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
                    ServiceHelper.sendMessage(getActivity(),
                            new ChatData(getContext(), GLOBAL, getMessageText()));
                editTextMessage.setText("");
            }
        });
    }
}
