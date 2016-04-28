package org.jukov.lanchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import org.jukov.lanchat.db.DBHelper;
import org.jukov.lanchat.fragment.RoomChatFragment;
import org.jukov.lanchat.service.LANChatService;

import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_DESTINATION_UID;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_ID;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_MESSAGE;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_NAME;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_UID;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.ROOM_MESSAGE_ACTION;

/**
 * Created by jukov on 24.04.2016.
 */
public class RoomChatActivity extends NavigationDrawerActivity {

    private String name;
    private String roomUID;
//    private List<String> particants;

    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        name = intent.getStringExtra(EXTRA_NAME);
        roomUID = intent.getStringExtra(EXTRA_UID);

//        particants = new ArrayList<>();
//        particants.add(Utils.getAndroidID(this));

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

    public ArrayAdapter<String> getArrayAdapterMessages() {
        return arrayAdapterMessages;
    }

    public String getRoomUID() {
        return roomUID;
    }

    protected void initViews() {
        super.initViews();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setTitle(name);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initAdapter() {
        DBHelper dbHelper = DBHelper.getInstance(this);

        arrayAdapterMessages = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                dbHelper.getRoomMessages(roomUID));
    }

    private void initFragment() {
        RoomChatFragment roomChatFragment = RoomChatFragment.newInstance();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, roomChatFragment)
                .commit();
    }

    private void initBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                String destinationUID = intent.getStringExtra(EXTRA_DESTINATION_UID);
                if (destinationUID.equals(roomUID))
                    arrayAdapterMessages.add(intent.getStringExtra(EXTRA_NAME) + ": " + intent.getStringExtra(EXTRA_MESSAGE));
            }
        };
        IntentFilter intentFilter = new IntentFilter(ROOM_MESSAGE_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void initService() {
//        Log.d(getClass().getSimpleName(), "Connecting to service");
//        ServiceHelper.startService(this);
    }
}
