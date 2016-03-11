package org.jukov.lanchat.fragment;

import android.content.Context;
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

import org.jukov.lanchat.MainActivity;
import org.jukov.lanchat.R;
import org.jukov.lanchat.service.ServiceHelper;

/**
 * Created by jukov on 15.02.2016.
 */
public class GroupChatFragment extends BaseFragment {

    private ListView listViewMessages;
    private Button buttonSend;
    private EditText editTextMessage;

    private ArrayAdapter<String> arrayAdapterMessages;

    public static GroupChatFragment newInstance(Context context) {

        Bundle args = new Bundle();

        GroupChatFragment fragment = new GroupChatFragment();
        fragment.setTitle(context.getString(R.string.global_chat));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(getClass().getSimpleName(), "onCreateView()");

        layout = inflater.inflate(R.layout.fragment_chat, container, false);

        initViews();

        return layout;
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
                    ServiceHelper.sendMessage(getActivity(), ServiceHelper.MessageType.GLOBAL, editTextMessage.getText().toString());
                editTextMessage.setText("");
            }
        });
    }

    private void initAdapter() {
        arrayAdapterMessages = ((MainActivity) getActivity()).getArrayAdapterMessages();
    }
}
