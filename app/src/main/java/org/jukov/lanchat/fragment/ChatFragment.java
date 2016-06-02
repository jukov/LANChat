package org.jukov.lanchat.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import org.jukov.lanchat.NavigationDrawerActivity;
import org.jukov.lanchat.R;
import org.jukov.lanchat.adapter.ChatAdapter;
import org.jukov.lanchat.db.DBHelper;
import org.jukov.lanchat.dto.ChatData;

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

        SupplicantState supplicantState;
        WifiManager wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        supplicantState = wifiInfo.getSupplicantState();

        if (supplicantState != SupplicantState.COMPLETED) {
            buttonSend.setEnabled(false);
            editTextMessage.setEnabled(false);
        }

        listViewMessages.setAdapter(chatAdapter);

        listViewMessages.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.choose_action))
                        .setItems(new String[]{
                                getString(R.string.delete_message),
                                getString(R.string.star_message),
                                getString(R.string.copy_text)}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ChatData chatData = chatAdapter.getItem(position);
                                DBHelper dbHelper = DBHelper.getInstance(getContext());
                                switch (which) {
                                    case 0:
                                        dbHelper.deleteMessage(chatData);
                                        chatAdapter.remove(position);
                                        break;
                                    case 1:

                                        break;
                                    case 2:
                                        ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clipData = ClipData.newPlainText("LANChat", chatData.getText());
                                        clipboardManager.setPrimaryClip(clipData);
                                        Toast.makeText(getContext(), R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }
                        });
                builder.create().show();
                return true;
            }
        });
    }

    private void initAdapter() {
        chatAdapter = ((NavigationDrawerActivity) getActivity()).getChatAdapter();
    }

}
