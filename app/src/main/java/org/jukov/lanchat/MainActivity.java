package org.jukov.lanchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.MenuItem;

import org.jukov.lanchat.adapter.ChatAdapter;
import org.jukov.lanchat.adapter.PeopleAdapter;
import org.jukov.lanchat.adapter.RoomsAdapter;
import org.jukov.lanchat.db.DBHelper;
import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.dto.RoomData;
import org.jukov.lanchat.fragment.BaseFragment;
import org.jukov.lanchat.fragment.GroupChatFragment;
import org.jukov.lanchat.fragment.PeopleFragment;
import org.jukov.lanchat.fragment.RoomsFragment;
import org.jukov.lanchat.fragment.SettingsFragment;
import org.jukov.lanchat.service.LANChatService;
import org.jukov.lanchat.service.ServiceHelper;
import org.jukov.lanchat.util.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_ACTION;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_ID;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_MESSAGE;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_MESSAGE_BUNDLE;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_NAME;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_ROOM;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_UID;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.GLOBAL_MESSAGE_ACTION;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.PEOPLE_ACTION;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.SEND_ROOM_ACTION;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.SET_ALL_OFFLINE_ACTION;

public class MainActivity extends NavigationDrawerActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private HashMap<Integer, Fragment> fragments;

    private BroadcastReceiver broadcastReceiver;

    private PeopleAdapter peopleAdapter;
    private RoomsAdapter roomsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        initViews();
        initValues();
        initFragments();
        initAdapters();
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
        int id = item.getItemId();
        if (currentNavigationId != id) {
            switch (id) {
                case R.id.drawerMenuSettings:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.relativeLayoutFragmentContainer, fragments.get(id))
                            .commit();
                    toolbar.setTitle(getString(R.string.settings));
                    break;
                case R.id.drawerMenuExit:
                    DBHelper.getInstance(this).close();
                    stopService(new Intent(getApplicationContext(), LANChatService.class));
                    finishAffinity();
                    break;
                default:
                    BaseFragment baseFragment = (BaseFragment) fragments.get(id);
                    toolbar.setTitle(baseFragment.getTitle());
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.relativeLayoutFragmentContainer, baseFragment)
                            .commit();
            }
            currentNavigationId = id;
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_ROOM_CREATING:
                    Log.d(TAG, "REQUEST_CODE_ROOM_CREATING");
                    break;
                default:
                    int id = data.getIntExtra(EXTRA_ID, 0);
                    Fragment fragment = fragments.get(id);
                    if (fragment instanceof PreferenceFragmentCompat)
                        toolbar.setTitle(R.string.settings);
                    else {
                        toolbar.setTitle(((BaseFragment) fragment).getTitle());
                    }
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.relativeLayoutFragmentContainer, fragments.get(id))
                            .commit();
                    currentNavigationId = id;
                    break;
            }
        }
    }

    public PeopleAdapter getPeopleAdapter() {
        return peopleAdapter;
    }

    public RoomsAdapter getRoomsAdapter() {
        return roomsAdapter;
    }

    public int getPeopleAround() {
        return peopleAround;
    }

    @Override
    protected void initViews() {
        super.initViews();

        actionBarDrawerToggle = new ActionBarDrawerToggle (
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        actionBarDrawerToggle.syncState();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(getString(R.string.global_chat));
    }

    private void initValues() {
        currentNavigationId = R.id.drawerMenuGlobalChat;
    }

    private void initFragments() {
        fragments = new HashMap<>();

        fragments.put(R.id.drawerMenuGlobalChat, GroupChatFragment.newInstance(this));
        fragments.put(R.id.drawerMenuPeoples, PeopleFragment.newInstance(this));
        fragments.put(R.id.drawerMenuRooms, RoomsFragment.newInstance(this));
        fragments.put(R.id.drawerMenuSettings, new SettingsFragment());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.relativeLayoutFragmentContainer, fragments.get(R.id.drawerMenuGlobalChat))
                .commit();
    }

    private void initAdapters() {
        DBHelper dbHelper = DBHelper.getInstance(this);

        roomsAdapter = new RoomsAdapter(getApplicationContext());
        chatAdapter = new ChatAdapter(this, dbHelper.getPublicMessages());
        peopleAdapter = new PeopleAdapter(this);

        List<PeopleData> people = dbHelper.getPeople();
        Iterator<PeopleData> iterator = people.iterator();
        while (iterator.hasNext()) {
            PeopleData peopleData = iterator.next();
            if (peopleData.getUid().equals(Utils.getAndroidID(getApplicationContext()))) {
                iterator.remove();
                break;
            }
        }
        peopleAdapter.addAll(people);
    }

    @Override
    protected void initBroadcastReceiver() {
        super.initBroadcastReceiver();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                switch (intent.getAction()) {
                    case GLOBAL_MESSAGE_ACTION:
                        Log.d(TAG, "Receive global message");
                        if (intent.hasExtra(EXTRA_MESSAGE)) {
                            ChatData chatData = intent.getParcelableExtra(EXTRA_MESSAGE);
                            chatAdapter.add(chatData);
                        }
                        else if (intent.hasExtra(EXTRA_MESSAGE_BUNDLE)) {
                            Parcelable[] parcelables = intent.getParcelableArrayExtra(EXTRA_MESSAGE_BUNDLE);
                            ChatData[] messages = Arrays.copyOf(parcelables, parcelables.length, ChatData[].class);
                            for (ChatData chatData : messages) {
                                chatAdapter.add(chatData);
                            }
                        }
                        break;
                    case PEOPLE_ACTION:
                        String name = intent.getStringExtra(EXTRA_NAME);
                        String uid = intent.getStringExtra(EXTRA_UID);
                        int action = intent.getIntExtra(EXTRA_ACTION, -1);
                        Log.d(TAG, Integer.toString(action));
                        PeopleData peopleData = new PeopleData(name, uid, PeopleData.ActionType.fromInt(action));
                        if (!uid.equals(Utils.getAndroidID(getApplicationContext())))
                            switch (peopleData.getAction()) {
                                case CONNECT:
                                    peopleAdapter.add(peopleData);
                                    break;
                                case DISCONNECT:
                                    peopleAdapter.setOffline(peopleData);
                                    break;
                                case CHANGE_NAME:
                                    peopleAdapter.setOffline(peopleData);
                                    peopleAdapter.add(peopleData);
                                    break;
                                default:
                                    Log.w(getClass().getSimpleName(), "Unexpected action type");
                            }
                        break;
                    case SEND_ROOM_ACTION:
                        Log.d(TAG, "SEND_ROOM_ACTION");
                        if (intent.hasExtra(EXTRA_ROOM)) {
                            RoomData roomData = intent.getParcelableExtra(EXTRA_ROOM);
                            roomsAdapter.add(roomData);
                        } else if (intent.hasExtra(EXTRA_MESSAGE_BUNDLE)) {
                            Parcelable[] parcelables = intent.getParcelableArrayExtra(EXTRA_MESSAGE_BUNDLE);
                            RoomData[] messages = Arrays.copyOf(parcelables, parcelables.length, RoomData[].class);
                            for (RoomData roomData : messages) {
                                roomsAdapter.add(roomData);
                            }
                        }
                        break;
                    case SET_ALL_OFFLINE_ACTION:
                        peopleAdapter.allOffline();
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GLOBAL_MESSAGE_ACTION);
        intentFilter.addAction(PEOPLE_ACTION);
        intentFilter.addAction(SET_ALL_OFFLINE_ACTION);
        intentFilter.addAction(SEND_ROOM_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void initService() {
        Log.d(getClass().getSimpleName(), "Connecting to service");
        ServiceHelper.startService(this);
    }
}
