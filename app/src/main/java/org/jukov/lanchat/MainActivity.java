package org.jukov.lanchat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.jukov.lanchat.db.DBHelper;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.fragment.BaseFragment;
import org.jukov.lanchat.fragment.GroupChatFragment;
import org.jukov.lanchat.fragment.PeopleFragment;
import org.jukov.lanchat.fragment.RoomFragment;
import org.jukov.lanchat.fragment.SettingsFragment;
import org.jukov.lanchat.service.LANChatService;
import org.jukov.lanchat.service.ServiceHelper;
import org.jukov.lanchat.util.Constants;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private View navigationDrawerHeaderView;
    private TextView textViewMode;

    private HashMap<Integer, Fragment> fragments;

    private int currentNavigationId;

    private BroadcastReceiver broadcastReceiver;

    private ArrayAdapter<PeopleData> arrayAdapterPeople;
    private ArrayAdapter<String> arrayAdapterMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initValues();
        initViews();
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
    public void onBackPressed() {
        Log.d(getClass().getSimpleName(), "onBackPressed");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
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

    @SuppressWarnings("StatementWithEmptyBody")
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
                    stopService(new Intent(getApplicationContext(), LANChatService.class));
                    finish();
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(getClass().getSimpleName(), "onSharedPreferenceChanged() " + key);
        switch (key) {
            case "name":
                String name = sharedPreferences.getString("name", getString(R.string.default_name));
                TextView textView = (TextView) navigationDrawerHeaderView.findViewById(R.id.navTextViewName);
                ServiceHelper.changeName(this, name);
                textView.setText(getString(R.string.nav_header_hello, name));
                break;
        }
    }

    public ArrayAdapter<PeopleData> getArrayAdapterPeople() {
        return arrayAdapterPeople;
    }

    public ArrayAdapter<String> getArrayAdapterMessages() {
        return arrayAdapterMessages;
    }

    private void initValues() {
        currentNavigationId = R.id.drawerMenuGlobalChat;
    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getString(R.string.global_chat));

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        actionBarDrawerToggle = new ActionBarDrawerToggle (
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                InputMethodManager inputMethodManager = (InputMethodManager) MainActivity.
                        this.getSystemService(Activity.INPUT_METHOD_SERVICE);
                if (MainActivity.this.getCurrentFocus().getWindowToken() != null)
                    inputMethodManager.hideSoftInputFromWindow(MainActivity.this
                            .getCurrentFocus().getWindowToken(), 0);
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        actionBarDrawerToggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationDrawerHeaderView = navigationView.getHeaderView(0);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        TextView textView = (TextView) navigationDrawerHeaderView.findViewById(R.id.navTextViewName);
        textView.setText(getString(R.string.nav_header_hello, sharedPreferences.getString("name", getString(R.string.default_name))));

        textViewMode = (TextView) navigationDrawerHeaderView.findViewById(R.id.naeTextViewPeoplesAround);
    }

    private void initFragments() {

        fragments = new HashMap<>();

        fragments.put(R.id.drawerMenuGlobalChat, GroupChatFragment.newInstance(this));
        fragments.put(R.id.drawerMenuPeoples, PeopleFragment.newInstance(this));
        fragments.put(R.id.drawerMenuRooms, RoomFragment.newInstance(this));
        fragments.put(R.id.drawerMenuSettings, new SettingsFragment());

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragments.get(R.id.drawerMenuGlobalChat))
                .commit();
    }

    private void initAdapters() {
        DBHelper dbHelper = DBHelper.getInstance(this);

        arrayAdapterMessages = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, dbHelper.getPublicMessages());
        arrayAdapterPeople = new ArrayAdapter<>(this, R.layout.listview_people, R.id.listviewPeoplesName);
    }

    private void initBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                switch (intent.getAction()) {
                    case Constants.IntentConstants.ACTIVITY_ACTION:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textViewMode.setText(intent.getStringExtra(Constants.IntentConstants.EXTRA_MODE));
                            }
                        });
                        break;
                    case Constants.IntentConstants.GLOBAL_CHAT_ACTION:
                        arrayAdapterMessages.add(intent.getStringExtra(Constants.IntentConstants.EXTRA_NAME) + ": " + intent.getStringExtra(Constants.IntentConstants.EXTRA_MESSAGE));
                        break;
                    case Constants.IntentConstants.PEOPLE_ACTION:
                        String name = intent.getStringExtra(Constants.IntentConstants.EXTRA_NAME);
                        String uid = intent.getStringExtra(Constants.IntentConstants.EXTRA_UID);
                        int action = intent.getIntExtra(Constants.IntentConstants.EXTRA_ACTION, -1);
                        PeopleData peopleData = new PeopleData(name, uid, action);
                        switch (action) {
                            case PeopleData.ACTION_CONNECT:
                                arrayAdapterPeople.add(peopleData);
                                break;
                            case PeopleData.ACTION_DISCONNECT:
                                arrayAdapterPeople.remove(peopleData);
                                break;
                            case PeopleData.ACTION_CHANGE_NAME:
                                arrayAdapterPeople.remove(peopleData);
                                arrayAdapterPeople.add(peopleData);
                        }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.IntentConstants.ACTIVITY_ACTION);
        intentFilter.addAction(Constants.IntentConstants.GLOBAL_CHAT_ACTION);
        intentFilter.addAction(Constants.IntentConstants.PEOPLE_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void initService() {
        Log.d(getClass().getSimpleName(), "Connecting to service");
        ServiceHelper.startService(this);
    }
}
