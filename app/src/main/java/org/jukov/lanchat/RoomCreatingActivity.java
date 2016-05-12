package org.jukov.lanchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;

import org.jukov.lanchat.db.DBHelper;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.dto.RoomData;
import org.jukov.lanchat.service.ServiceHelper;
import org.jukov.lanchat.util.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 * Created by jukov on 23.04.2016.
 */
public class RoomCreatingActivity extends BaseActivity {

    @SuppressWarnings("WeakerAccess")
    public static final String TAG = RoomCreatingActivity.class.getSimpleName();

    private EditText roomNameText;
    private CheckBox isPrivate;
    private ListView listViewPeople;

    private MenuItem menuItemDone;
    private MenuItem menuItemUndone;

    private ArrayAdapter<PeopleData> arrayAdapterPeople;
    private List<PeopleData> privateParticipants;

    private boolean nameCorrect;
    private boolean privateCorrect;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_creating);

        initValues();
        initAdaptersAndLists();
        initViews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done, menu);

        menuItemDone = menu.findItem(R.id.action_menu_done);
        menuItemUndone = menu.findItem(R.id.action_menu_undone);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_menu_done:
                DBHelper dbHelper = DBHelper.getInstance(getApplicationContext());
                RoomData roomData = new RoomData(
                        roomNameText.getText().toString(),
                        Utils.newRoomUID(getApplicationContext()),
                        isPrivate.isChecked() ? privateParticipants : null);
                dbHelper.insertOrUpdateRoom(roomData);
                ServiceHelper.sendRoom(getApplicationContext(), roomData);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
        return true;
    }

    private void setDoneButton() {
        if (nameCorrect && privateCorrect) {
            menuItemDone.setVisible(true);
            menuItemUndone.setVisible(false);
        } else {
            menuItemDone.setVisible(false);
            menuItemUndone.setVisible(true);
        }
    }

    private void initValues() {
        nameCorrect = false;
        privateCorrect = true;
    }

    private void initAdaptersAndLists() {
        privateParticipants = new ArrayList<>();

        DBHelper dbHelper = DBHelper.getInstance(getApplicationContext());
        List<PeopleData> people = dbHelper.getPeople();

        String myUID = Utils.getAndroidID(getApplicationContext());
        Iterator<PeopleData> iterator = people.iterator();
        while (iterator.hasNext()) {
            PeopleData peopleData = iterator.next();
            if (peopleData.getUid().equals(myUID)) {
                iterator.remove();
                privateParticipants.add(peopleData);
                break;
            }
        }

        arrayAdapterPeople = new ArrayAdapter<>(
                this,
                R.layout.listview_people_check,
                R.id.textViewName,
                people);
    }

    @Override
    protected void initViews() {
        super.initViews();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setTitle(R.string.new_room);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        roomNameText = (EditText) findViewById(R.id.editTextRoomName);
        isPrivate = (CheckBox) findViewById(R.id.checkBoxIsPrivate);
        listViewPeople = (ListView) findViewById(R.id.listViewPeople);

        if (roomNameText != null) {
            roomNameText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() > 0) {
                        nameCorrect = true;
                        setDoneButton();
                    } else {
                        nameCorrect = false;
                        setDoneButton();
                    }
                }
            });
        }

        if (isPrivate != null) {
            isPrivate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    listViewPeople.setEnabled(isChecked);
                    if (isChecked) {
                        listViewPeople.setVisibility(View.VISIBLE);
                        privateCorrect = privateParticipants.size() > 1;
                    } else {
                        listViewPeople.setVisibility(View.GONE);
                        privateCorrect = true;
                    }
                    setDoneButton();
                }
            });
        }

        if (listViewPeople != null) {
            listViewPeople.setAdapter(arrayAdapterPeople);

            listViewPeople.setEnabled(false);
            listViewPeople.setVisibility(View.GONE);

            listViewPeople.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.d(TAG, "onItemClick()");
                    CheckBox isParticipant = (CheckBox) view.findViewById(R.id.isParticipant);
                    isParticipant.setChecked(!isParticipant.isChecked());
                    if (isParticipant.isChecked()) {
                        privateParticipants.add(arrayAdapterPeople.getItem(position));
                    } else {
                        privateParticipants.remove(arrayAdapterPeople.getItem(position));
                    }
                    privateCorrect = privateParticipants.size() > 1;
                    setDoneButton();
                }
            });
        }
    }
}
