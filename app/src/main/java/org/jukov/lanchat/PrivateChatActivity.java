package org.jukov.lanchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import org.jukov.lanchat.adapter.ChatAdapter;
import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.fragment.PrivateChatFragment;
import org.jukov.lanchat.service.LANChatService;
import org.jukov.lanchat.service.ServiceHelper;
import org.jukov.lanchat.util.DBHelper;
import org.jukov.lanchat.util.Utils;

import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_ID;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_MESSAGE;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_NAME;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_PEOPLE_AROUND;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_STATE;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_UID;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.PRIVATE_MESSAGE_ACTION;

/**
 * Created by jukov on 10.03.2016.
 */
public class PrivateChatActivity extends NavigationDrawerActivity {

    private String companionName;
    private String companionUID;
    private String myUID;
    private boolean state;

    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        companionName = intent.getStringExtra(EXTRA_NAME);
        companionUID = intent.getStringExtra(EXTRA_UID);
        peopleAround = intent.getIntExtra(EXTRA_PEOPLE_AROUND, -1);
        state = intent.getBooleanExtra(EXTRA_STATE, false);
        myUID = Utils.getAndroidID(this);

        initViews();
        initAdapter();
        initFragment();
        initBroadcastReceiver();
    }

    @Override
    protected void onResume() {
        super.onResume();

        ServiceHelper.sendPrivateChatState(getApplicationContext(), true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        ServiceHelper.sendPrivateChatState(getApplicationContext(), false);
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

    public boolean getState() {
        return state;
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

        chatAdapter = new ChatAdapter(getApplicationContext(), dbHelper.getPrivateMessages(myUID, companionUID));
    }

    private void initFragment() {
        PrivateChatFragment privateChatFragment = PrivateChatFragment.newInstance();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.relativeLayoutFragmentContainer, privateChatFragment)
                .commit();
    }

    @Override
    protected void initBroadcastReceiver() {
        super.initBroadcastReceiver();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                ChatData chatData = intent.getParcelableExtra(EXTRA_MESSAGE);
                if (chatData.getUid().equals(myUID) || chatData.getUid().equals(companionUID))
                    chatAdapter.add(chatData);
            }
        };
        IntentFilter intentFilter = new IntentFilter(PRIVATE_MESSAGE_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }
}
