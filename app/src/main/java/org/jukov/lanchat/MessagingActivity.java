package org.jukov.lanchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.jukov.lanchat.db.DBHelper;
import org.jukov.lanchat.fragment.ChatFragment;
import org.jukov.lanchat.service.ServiceHelper;
import org.jukov.lanchat.util.Utils;

import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_MESSAGE;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_NAME;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_UID;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.PRIVATE_CHAT_ACTION;

/**
 * Created by jukov on 10.03.2016.
 */
public class MessagingActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private String companionName;
    private String companionUID;

    private BroadcastReceiver broadcastReceiver;

    private ArrayAdapter<String> arrayAdapterMessages;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        Intent intent = getIntent();
        companionName = intent.getStringExtra(EXTRA_NAME);
        companionUID = intent.getStringExtra(EXTRA_UID);

        initValues();
        initViews();
        initAdapter();
        initFragment();
        initBroadcastReceiver();
        initService();
    }

    private void initValues() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public ArrayAdapter<String> getArrayAdapterMessages() {
        return arrayAdapterMessages;
    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(companionName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initAdapter() {
        DBHelper dbHelper = DBHelper.getInstance(this);

        arrayAdapterMessages = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                dbHelper.getPrivateMessages(Utils.getAndroidID(this), companionUID));
    }

    private void initFragment() {
        ChatFragment chatFragment = ChatFragment.newInstance(companionName);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, chatFragment)
                .commit();
    }

    private void initBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                String senderName = intent.getStringExtra(EXTRA_NAME);
                if (senderName.equals(sharedPreferences.getString("name", context.getString(R.string.default_name)))
                        || senderName.equals(companionName))
                    arrayAdapterMessages.add(intent.getStringExtra(EXTRA_NAME) + ": " + intent.getStringExtra(EXTRA_MESSAGE));
            }
        };
        IntentFilter intentFilter = new IntentFilter(PRIVATE_CHAT_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void initService() {
        Log.d(getClass().getSimpleName(), "Connecting to service");
        ServiceHelper.startService(this);
    }
}
