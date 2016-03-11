package org.jukov.lanchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.jukov.lanchat.fragment.ChatFragment;
import org.jukov.lanchat.fragment.GroupChatFragment;
import org.jukov.lanchat.service.ServiceHelper;
import org.jukov.lanchat.util.IntentStrings;

/**
 * Created by jukov on 10.03.2016.
 */
public class MessagingActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private String talkerName;

    private BroadcastReceiver broadcastReceiver;

    private ArrayAdapter<String> arrayAdapterMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        Intent intent = getIntent();
        talkerName = intent.getStringExtra(IntentStrings.EXTRA_NAME);

        initViews();
        initAdapter();
        initFragment();
        initBroadcastReceiver();
        initService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    public ArrayAdapter<String> getArrayAdapterMessages() {
        return arrayAdapterMessages;
    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(talkerName);
    }

    private void initAdapter() {
        arrayAdapterMessages = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
    }

    private void initFragment() {
        ChatFragment chatFragment = ChatFragment.newInstance(talkerName);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, chatFragment)
                .commit();
    }

    private void initBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
//                if (talkerName.equals(intent.getStringExtra(IntentStrings.EXTRA_NAME)))
                    arrayAdapterMessages.add(intent.getStringExtra(IntentStrings.EXTRA_NAME) + ": " + intent.getStringExtra(IntentStrings.EXTRA_MESSAGE));
            }
        };
        IntentFilter intentFilter = new IntentFilter(IntentStrings.PRIVATE_CHAT_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void initService() {
        Log.d(getClass().getSimpleName(), "Connecting to service");
        ServiceHelper.startService(this);
    }
}
