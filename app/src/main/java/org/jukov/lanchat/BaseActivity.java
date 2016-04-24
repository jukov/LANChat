package org.jukov.lanchat;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

/**
 * Created by jukov on 23.04.2016.
 */
public class BaseActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_PRIVATE_CHAT = 1;
    public static final int REQUEST_CODE_ROOM_CREATING = 2;

    protected Toolbar toolbar;

    protected void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }
}
