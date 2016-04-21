package org.jukov.lanchat.fragment;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.jukov.lanchat.dto.PeopleData;

/**
 * Created by jukov on 21.04.2016.
 */
public abstract class ListFragment extends BaseFragment {

    protected ArrayAdapter<PeopleData> arrayAdapter;
    protected ListView listView;
}
