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
import android.widget.ArrayAdapter;

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

import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.CLEAR_PEOPLE_LIST_ACTION;
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

public class MainActivity extends NavigationDrawerActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private HashMap<Integer, Fragment> fragments;

    private BroadcastReceiver broadcastReceiver;

    private ArrayAdapter<PeopleData> arrayAdapterPeople;
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

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.context_menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (currentNavigationId != id) {
            switch (id) {
                case R.id.drawerMenuSettings:
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentContainer, fragments.get(id))
                            .addToBackStack(null)
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
                            .replace(R.id.fragmentContainer, baseFragment)
                            .addToBackStack(null)
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
        Log.d(TAG, "onActivityResult() " + Integer.toString(resultCode));
        Log.d(TAG, "onActivityResult() " + Integer.toString(requestCode));
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
                            .replace(R.id.fragmentContainer, fragments.get(id))
                            .addToBackStack(null)
                            .commit();
                    currentNavigationId = id;
                    break;
            }
        }
    }

    public ArrayAdapter<PeopleData> getArrayAdapterPeople() {
        return arrayAdapterPeople;
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
                .replace(R.id.fragmentContainer, fragments.get(R.id.drawerMenuGlobalChat))
                .commit();
    }

    private void initAdapters() {
        DBHelper dbHelper = DBHelper.getInstance(this);

        roomsAdapter = new RoomsAdapter(getApplicationContext());
        arrayAdapterMessages = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dbHelper.getPublicMessages());
        arrayAdapterPeople = new ArrayAdapter<>(this, R.layout.listview_people, R.id.listviewPeopleName);
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
                        if (intent.hasExtra(EXTRA_NAME))
                            arrayAdapterMessages.add(intent.getStringExtra(EXTRA_NAME) + ": " + intent.getStringExtra(EXTRA_MESSAGE));
                        else if (intent.hasExtra(EXTRA_MESSAGE_BUNDLE)) {
                            Parcelable[] parcelables = intent.getParcelableArrayExtra(EXTRA_MESSAGE_BUNDLE);
                            ChatData[] messages = Arrays.copyOf(parcelables, parcelables.length, ChatData[].class);
                            for (ChatData chatData : messages) {
                                arrayAdapterMessages.add(chatData.toString());
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
                                    arrayAdapterPeople.add(peopleData);
                                    break;
                                case DISCONNECT:
                                    arrayAdapterPeople.remove(peopleData);
                                    break;
                                case CHANGE_NAME:
                                    arrayAdapterPeople.remove(peopleData);
                                    arrayAdapterPeople.add(peopleData);
                                    break;
                                default:
                                    Log.w(getClass().getSimpleName(), "Unexpected action type");
                            }
                        break;
                    case SEND_ROOM_ACTION:
                        Log.d(TAG, "SEND_ROOM_ACTION");
                        if (intent.hasExtra(EXTRA_ROOM)) {
                            RoomData roomData = intent.getParcelableExtra(EXTRA_ROOM);
                            if (roomData.getParticipants() != null && roomData.getParticipants().size() > 0) {
                                Log.d(TAG, Integer.toString(roomData.getParticipants().size()));
                                for (PeopleData peopleData1 : roomData.getParticipants()) {
                                    if (peopleData1.getUid().contains(Utils.getAndroidID(getApplicationContext()))) {
                                        roomsAdapter.add(roomData);
                                        break;
                                    }
                                }
                            } else {
                                roomsAdapter.add(roomData);
                            }
                        } else if (intent.hasExtra(EXTRA_MESSAGE_BUNDLE)) {
                            Parcelable[] parcelables = intent.getParcelableArrayExtra(EXTRA_MESSAGE_BUNDLE);
                            RoomData[] messages = Arrays.copyOf(parcelables, parcelables.length, RoomData[].class);
                            for (RoomData roomData : messages) {
                                roomsAdapter.add(roomData);
                            }
                        }
                        break;
                    case CLEAR_PEOPLE_LIST_ACTION:
                        arrayAdapterPeople.clear();
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GLOBAL_MESSAGE_ACTION);
        intentFilter.addAction(PEOPLE_ACTION);
        intentFilter.addAction(CLEAR_PEOPLE_LIST_ACTION);
        intentFilter.addAction(SEND_ROOM_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void initService() {
        Log.d(getClass().getSimpleName(), "Connecting to service");
        ServiceHelper.startService(this);
    }
}
