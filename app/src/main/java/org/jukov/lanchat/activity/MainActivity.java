package org.jukov.lanchat.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.jukov.lanchat.R;
import org.jukov.lanchat.service.LANChatService;
import org.jukov.lanchat.util.IntentStrings;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "LC_Activity";

    private ListView listViewMessages;
    private Button buttonSend;
    private EditText editTextMessage;
    private TextView textViewDebug;

    private ArrayAdapter<String> arrayAdapterMessages;

    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        initViews();
        initService();
        initBroadcastReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_global_chat:
                break;
            case R.id.nav_peers:
                break;
            case R.id.nav_rooms:
                break;
            case R.id.nav_settings:
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initViews() {
        listViewMessages = (ListView) findViewById(R.id.listViewMessages);
        buttonSend = (Button) findViewById(R.id.buttonSend);
        editTextMessage = (EditText) findViewById(R.id.editTextMessage);
        textViewDebug = (TextView) findViewById(R.id.textViewDebug);

        arrayAdapterMessages = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listViewMessages.setAdapter(arrayAdapterMessages);

        buttonSend.setEnabled(false);
        editTextMessage.setEnabled(false);

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Send message to service");
                Intent intent = new Intent(getApplicationContext(), LANChatService.class);
                intent.putExtra(IntentStrings.EXTRA_TYPE, IntentStrings.TYPE_MESSAGE);
                intent.putExtra(IntentStrings.EXTRA_MESSAGE, editTextMessage.getText().toString());
                startService(intent);
            }
        });
    }

    private void initService() {
        Log.d(TAG, "Creating Service");
        Intent intent = new Intent(this, LANChatService.class);
        intent.putExtra(IntentStrings.EXTRA_TYPE, IntentStrings.TYPE_START_SERVICE);
        startService(intent);
    }

    private void initBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, final Intent intent) {
                Log.d(TAG, "Receive message");
                if (intent.hasExtra(IntentStrings.EXTRA_TYPE)) {
                    Log.d(TAG, IntentStrings.EXTRA_TYPE);
                    switch (intent.getStringExtra(IntentStrings.EXTRA_TYPE)) {
                        case IntentStrings.TYPE_MESSAGE:
                            arrayAdapterMessages.add(intent.getStringExtra(IntentStrings.EXTRA_NAME) + ": " + intent.getStringExtra(IntentStrings.EXTRA_MESSAGE));
                            break;
                        case IntentStrings.TYPE_DEBUG:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textViewDebug.setText(intent.getStringExtra(IntentStrings.EXTRA_DEBUG));
                                }
                            });
                            break;
                        case IntentStrings.TYPE_UNLOCK_VIEWS:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    buttonSend.setEnabled(true);
                                    editTextMessage.setEnabled(true);
                                }
                            });
                            break;
                        default:
                            Log.d(TAG, "Unexpected intent type");
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(IntentStrings.BROADCAST_ACTION);
        registerReceiver(broadcastReceiver, intentFilter);
    }
}
