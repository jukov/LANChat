package org.jukov.lanchat.fragment;

import android.content.Context;
import android.os.Bundle;

import org.jukov.lanchat.R;

/**
 * Created by jukov on 16.02.2016.
 */
public class RoomFragment extends BaseFragment {

    public static RoomFragment newInstance(Context context) {

        Bundle args = new Bundle();

        RoomFragment fragment = new RoomFragment();
        fragment.setTitle(context.getString(R.string.rooms));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
