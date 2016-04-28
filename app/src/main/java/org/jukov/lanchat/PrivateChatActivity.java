package org.jukov.lanchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import org.jukov.lanchat.db.DBHelper;
import org.jukov.lanchat.fragment.PrivateChatFragment;
import org.jukov.lanchat.service.LANChatService;
import org.jukov.lanchat.service.ServiceHelper;
import org.jukov.lanchat.util.Utils;

import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_ID;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_MESSAGE;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_NAME;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_RECEIVER_UID;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_UID;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.SEND_PRIVATE_MESSAGE_ACTION;

/**
 * Created by jukov on 10.03.2016.
 */
public class PrivateChatActivity extends NavigationDrawerActivity {

    private String companionName;
    private String companionUID;
    private String myUID;

    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        companionName = intent.getStringExtra(EXTRA_NAME);
        companionUID = intent.getStringExtra(EXTRA_UID);
        myUID = Utils.getAndroidID(this);

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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.drawerMenuExit:
                DBHelper.getInstance(this).close();
                stopService(new Intent(getApplicationContext(), LANChatService.class));
                finishAffinity();
                break;
            default:
                Intent intent = new Intent();
                intent.putExtra(EXTRA_ID, item.getItemId());
                setResult(RESULT_OK, intent);
                finish();
        }
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
        return true;
    }

    public String getCompanionUID() {
        return companionUID;
    }

    protected void initViews() {
        super.initViews();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setTitle(companionName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initAdapter() {
        DBHelper dbHelper = DBHelper.getInstance(this);

        arrayAdapterMessages = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                dbHelper.getPrivateMessages(Utils.getAndroidID(this), companionUID));
    }

    private void initFragment() {
        PrivateChatFragment privateChatFragment = PrivateChatFragment.newInstance();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, privateChatFragment)
                .commit();
    }

    private void initBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                String receiverUID = intent.getStringExtra(EXTRA_RECEIVER_UID);
                if (receiverUID.equals(myUID) || receiverUID.equals(companionUID))
                    arrayAdapterMessages.add(intent.getStringExtra(EXTRA_NAME) + ": " + intent.getStringExtra(EXTRA_MESSAGE));
            }
        };
        IntentFilter intentFilter = new IntentFilter(SEND_PRIVATE_MESSAGE_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void initService() {
        Log.d(getClass().getSimpleName(), "Connecting to service");
        ServiceHelper.startService(this);
    }
}
