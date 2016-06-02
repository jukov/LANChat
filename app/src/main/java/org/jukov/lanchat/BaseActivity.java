package org.jukov.lanchat;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by jukov on 23.04.2016.
 */
public abstract class BaseActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_PRIVATE_CHAT = 11;
    public static final int REQUEST_CODE_ROOM_CREATING = 12;
    public static final int REQUEST_CODE_ROOM_CHAT = 13;
    public static final int REQUEST_CODE_ADDING_PEOPLE = 14;

    Toolbar toolbar;

    void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
}
