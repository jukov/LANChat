package org.jukov.lanchat;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;

import org.jukov.lanchat.db.DBHelper;
import org.jukov.lanchat.dto.PeopleData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.jukov.lanchat.service.ServiceHelper.IntentConstants.EXTRA_PARTICIPANTS;

/**
 * Created by jukov on 05.05.2016.
 */
public class AddingPeopleActivity extends BaseActivity {

    @SuppressWarnings("unused")
    private static final String TAG = AddingPeopleActivity.class.getSimpleName();

    private ArrayAdapter<PeopleData> arrayAdapterPeople;
    private List<PeopleData> participants;
    private List<PeopleData> newParticipants;

    private MenuItem menuItemDone;
    private MenuItem menuItemUndone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);

        Intent intent = getIntent();
        Parcelable[] parcelableArray = intent.getParcelableArrayExtra(EXTRA_PARTICIPANTS);
        participants = new ArrayList<>(
                Arrays.asList(
                        Arrays.copyOf(parcelableArray, parcelableArray.length, PeopleData[].class)));

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
                Intent intent = new Intent();
                Object[] objects = newParticipants.toArray();
                intent.putExtra(EXTRA_PARTICIPANTS, Arrays.copyOf(objects, objects.length, Parcelable[].class));
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

    private void setDoneButton(boolean condition) {
        menuItemDone.setVisible(condition);
        menuItemUndone.setVisible(!condition);
    }

    private void initAdaptersAndLists() {
        newParticipants = new ArrayList<>();
        DBHelper dbHelper = DBHelper.getInstance(getApplicationContext());
        List<PeopleData> people = dbHelper.getPeople();

        for (PeopleData peopleData : participants) {
            if (people.contains(peopleData))
                people.remove(peopleData);
        }

        arrayAdapterPeople = new ArrayAdapter<>(
                this,
                R.layout.listview_people_check,
                R.id.listviewPeopleName,
                people);
    }

    @Override
    protected void initViews() {
        super.initViews();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setTitle(R.string.add_people);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ListView listViewPeople = (ListView) findViewById(R.id.listViewPeople);

        if (listViewPeople != null) {
            listViewPeople.setAdapter(arrayAdapterPeople);

            listViewPeople.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    CheckBox isParticipant = (CheckBox) view.findViewById(R.id.isParticipant);
                    isParticipant.setChecked(!isParticipant.isChecked());
                    if (isParticipant.isChecked()) {
                        newParticipants.add(arrayAdapterPeople.getItem(position));
                    } else {
                        newParticipants.remove(arrayAdapterPeople.getItem(position));
                    }
                    if (newParticipants.size() > 0) {
                        setDoneButton(true);
                    } else {
                        setDoneButton(false);
                    }
                }
            });
        }
    }
}
