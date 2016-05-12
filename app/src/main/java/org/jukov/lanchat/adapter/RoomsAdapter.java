package org.jukov.lanchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.jukov.lanchat.R;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.dto.RoomData;
import org.jukov.lanchat.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jukov on 02.05.2016.
 */
public final class RoomsAdapter extends BaseAdapter {

    @SuppressWarnings("WeakerAccess")
    public static final String TAG = RoomsAdapter.class.getSimpleName();

    private final Context context;
    private final List<RoomData> rooms;
    private final LayoutInflater layoutInflater;

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

        TextView textView = (TextView) view.findViewById(R.id.textViewName);
        textView.setText(roomData.toString());

        if (roomData.getParticipants() != null && roomData.getParticipants().size() > 0) {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, R.drawable.ic_secure, 0);
        } else {
            textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,0,0);
        }
        return view;
    }

    public void add(RoomData roomData) {
        boolean isParticipant = false;
        if (roomData.getParticipants() != null && roomData.getParticipants().size() > 0) {
            for (PeopleData peopleData1 : roomData.getParticipants()) {
                if (peopleData1.getUid().contains(Utils.getAndroidID(context))) {
                    isParticipant = true;
                    break;
                }
            }
        } else {
            isParticipant = true;
        }
        if (isParticipant) {
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
}
