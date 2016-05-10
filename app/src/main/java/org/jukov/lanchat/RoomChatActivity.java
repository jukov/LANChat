package org.jukov.lanchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import org.jukov.lanchat.adapter.ChatAdapter;
import org.jukov.lanchat.db.DBHelper;
import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.dto.RoomData;
import org.jukov.lanchat.fragment.RoomChatFragment;
import org.jukov.lanchat.service.LANChatService;
import org.jukov.lanchat.service.ServiceHelper;

import java.util.Arrays;
import java.util.List;

import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_ID;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_MESSAGE;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_PARTICIPANTS;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_ROOM;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.ROOM_MESSAGE_ACTION;

/**
 * Created by jukov on 24.04.2016.
 */
public class RoomChatActivity extends NavigationDrawerActivity {

    public static final String TAG = RoomChatActivity.class.getSimpleName();

    DBHelper dbHelper;

    private String roomName;
    private String roomUid;
    private List<PeopleData> participants;

    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        RoomData roomData = intent.getParcelableExtra(EXTRA_ROOM);
        roomName = roomData.getName();
        roomUid = roomData.getUid();
        participants = roomData.getParticipants();

        dbHelper = DBHelper.getInstance(this);

        initViews();
        initAdapter();
        initFragment();
        initBroadcastReceiver();
//        initService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (participants != null && participants.size() > 0)
            getMenuInflater().inflate(R.menu.menu_plus, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu_plus:
                Intent intent = new Intent(getApplicationContext(), AddingPeopleActivity.class);
                PeopleData[] array = Arrays.copyOf(participants.toArray(), participants.toArray().length, PeopleData[].class);
                intent.putExtra(EXTRA_PARTICIPANTS, array);
                startActivityForResult(intent, REQUEST_CODE_ADDING_PEOPLE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Parcelable[] parcelableArray = data.getParcelableArrayExtra(EXTRA_PARTICIPANTS);
            for (PeopleData peopleData :
                    Arrays.asList(
                            Arrays.copyOf(parcelableArray, parcelableArray.length, PeopleData[].class))) {
                participants.add(peopleData);
            }
            RoomData roomData = new RoomData(roomName, roomUid, participants);
            dbHelper.insertOrUpdateRoom(roomData);
            ServiceHelper.sendRoom(getApplicationContext(), roomData);
        }
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

    public ChatAdapter getChatAdapter() {
        return chatAdapter;
    }

    public String getRoomUid() {
        return roomUid;
    }

    protected void initViews() {
        super.initViews();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setTitle(roomName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initAdapter() {
        chatAdapter = new ChatAdapter(getApplicationContext(), dbHelper.getRoomMessages(roomUid));
    }

    private void initFragment() {
        RoomChatFragment roomChatFragment = RoomChatFragment.newInstance();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, roomChatFragment)
                .commit();
    }

    @Override
    protected void initBroadcastReceiver() {
        super.initBroadcastReceiver();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                ChatData chatData = intent.getParcelableExtra(EXTRA_MESSAGE);
                if (chatData.getDestinationUID().equals(roomUid))
                    chatAdapter.add(chatData);
            }
        };
        IntentFilter intentFilter = new IntentFilter(ROOM_MESSAGE_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }

//    private void initService() {
//        Log.d(getClass().getSimpleName(), "Connecting to service");
//        ServiceHelper.startService(this);
//    }
}
