package org.jukov.lanchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.jukov.lanchat.db.DBHelper;

import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_ID;
import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_NAME;

/**
 * Created by jukov on 23.04.2016.
 */
public class RoomCreatingActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_creating);

        initViews();
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
        return true;
    }

    @Override
    protected void initViews() {
        super.initViews();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setTitle(R.string.new_room);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Button createButton = (Button) findViewById(R.id.createButton);
        final TextInputLayout roomNameTextLayout = (TextInputLayout) findViewById(R.id.roomNameTextLayout);
        final EditText roomNameText = (EditText) findViewById(R.id.roomNameText);

        if (createButton != null)
            createButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (roomNameText != null && roomNameTextLayout != null) {
                        if (!roomNameText.getText().toString().equals("")) {
                            DBHelper dbHelper = DBHelper.getInstance(getApplicationContext());
                            int id = dbHelper.insertRoom(roomNameText.getText().toString());
                            Intent intent = new Intent();
                            intent.putExtra(EXTRA_ID, id);
                            intent.putExtra(EXTRA_NAME, roomNameText.getText().toString());
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            roomNameTextLayout.setError(getString(R.string.room_name_not_empty));
                        }
                    }
                }
            });
    }
}