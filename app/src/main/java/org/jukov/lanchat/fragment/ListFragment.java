package org.jukov.lanchat.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.jukov.lanchat.R;

/**
 * Created by jukov on 15.02.2016.
 */
public class ListFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        layout = inflater.inflate(R.layout.peoples_list_layout, container, false);

        setTitle(getString(R.string.peoples));

        return layout;
    }
}
