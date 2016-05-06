package org.jukov.lanchat.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.jukov.lanchat.R;
import org.jukov.lanchat.dto.RoomData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jukov on 02.05.2016.
 */
public class RoomsAdapter extends BaseAdapter {

    public static final String TAG = RoomsAdapter.class.getSimpleName();

    private Context context;
    private List<RoomData> rooms;
    LayoutInflater layoutInflater;

    public RoomsAdapter(Context context) {
        this.context = context;
        rooms = new ArrayList<>();
        layoutInflater = LayoutInflater.from(context);
    }

    public RoomsAdapter(Context context, List<RoomData> rooms) {
        this.context = context;
        this.rooms = rooms;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return rooms.size();
    }

    @Override
    public RoomData getItem(int position) {
        return rooms.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.listview_people, parent, false);
        }

        RoomData roomData = getItem(position);

        TextView textView = (TextView) view.findViewById(R.id.listviewPeopleName);
        textView.setText(roomData.toString());

        if (roomData.getParticipants() != null && roomData.getParticipants().size() > 0) {
            Log.d(TAG, roomData.getName());
            Log.d(TAG, Arrays.toString(roomData.getParticipants().toArray()));
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_secure, 0);
        } else {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0);
        }
        return view;
    }

    public void add(RoomData roomData) {
        int position = rooms.indexOf(roomData);
        rooms.remove(roomData);
        if (position != -1) {
            rooms.add(position, roomData);
            return;
        }
        rooms.add(roomData);
        notifyDataSetChanged();
    }
}
