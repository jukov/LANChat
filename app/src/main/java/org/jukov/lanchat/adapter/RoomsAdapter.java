package org.jukov.lanchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.jukov.lanchat.R;
import org.jukov.lanchat.dto.RoomData;

import java.util.List;

/**
 * Created by jukov on 02.05.2016.
 */
public class RoomsAdapter extends ArrayAdapter<RoomData> {

    public static final String TAG = RoomsAdapter.class.getSimpleName();

    public RoomsAdapter(Context context, int resource) {
        super(context, resource);
    }

    public RoomsAdapter(Context context, int resource, List<RoomData> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            view = layoutInflater.inflate(R.layout.listview_people, parent, false);
        }

        RoomData roomData = getItem(position);

        TextView textView = (TextView) view.findViewById(R.id.listviewPeopleName);
        textView.setText(roomData.toString());

        if (roomData.getParticipantUIDs() != null && roomData.getParticipantUIDs().size() > 0) {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_secure, 0);
        }
        return view;
    }
}
