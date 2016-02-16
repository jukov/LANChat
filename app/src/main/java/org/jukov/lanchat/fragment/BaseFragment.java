package org.jukov.lanchat.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Created by jukov on 15.02.2016.
 */
public class BaseFragment extends Fragment {

    private String title;
    protected Context context;
    protected View layout;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
