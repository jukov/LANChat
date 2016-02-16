package org.jukov.lanchat.activity;

import android.app.Activity;
import android.content.Intent;
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
import android.view.inputmethod.InputMethodManager;

import org.jukov.lanchat.R;
import org.jukov.lanchat.fragment.BaseFragment;
import org.jukov.lanchat.fragment.ChatFragment;
import org.jukov.lanchat.fragment.ListFragment;
import org.jukov.lanchat.service.LANChatService;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = "LC_Activity";

    private HashMap<Integer, BaseFragment> fragments;

    private Toolbar toolbar;
    private DrawerLayout drawer;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initFragments();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
        Log.d(TAG, Integer.toString(id));

//        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragments.get(id)).commit();
        switch (id) {
            case R.id.nav_global_chat:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragments.get(id)).commit();
                toolbar.setTitle(getString(R.string.global_chat));
                break;
            case R.id.nav_peoples:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragments.get(id)).commit();
                toolbar.setTitle(getString(R.string.peoples));
                break;
            case R.id.nav_rooms:
                break;
            case R.id.nav_settings:
                break;
            case R.id.nav_exit:
                stopService(new Intent(getApplicationContext(), LANChatService.class));
                finish();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getString(R.string.global_chat));

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        drawer.setDrawerListener(new DrawerLayout.DrawerListener() {
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
    }

    private void initFragments() {

        fragments = new HashMap<>();

        fragments.put(R.id.nav_global_chat, new ChatFragment());
        fragments.put(R.id.nav_peoples, new ListFragment());

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragments.get(R.id.nav_global_chat)).commit();

    }
}
