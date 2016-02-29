package org.jukov.lanchat.fragment;

import android.os.Bundle;

import org.jukov.lanchat.R;

/**
 * Created by jukov on 16.02.2016.
 */
public class RoomFragment extends BaseFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.rooms));
    }
}
