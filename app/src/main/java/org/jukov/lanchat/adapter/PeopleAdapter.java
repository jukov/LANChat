package org.jukov.lanchat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.jukov.lanchat.R;
import org.jukov.lanchat.dto.PeopleData;
import org.jukov.lanchat.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jukov on 30.05.2016.
 */
public class PeopleAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<PeopleData> people;

    public PeopleAdapter(Context context) {
        this.context = context;
        people = new ArrayList<>();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return people.size();
    }

    @Override
    public PeopleData getItem(int position) {
        return people.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.listview_people, parent, false);
        }

        PeopleData peopleData = people.get(position);

        TextView textViewName = (TextView) view.findViewById(R.id.textViewName);
        TextView textViewStatus = (TextView) view.findViewById(R.id.textViewStatus);

        textViewName.setText(peopleData.getName());

        switch (peopleData.getAction()) {
            case CONNECT:
                textViewStatus.setText(context.getString(R.string.online));
                break;
            case DISCONNECT:
                textViewStatus.setText(context.getString(R.string.offline));
                break;
            case CHANGE_NAME:
                textViewStatus.setText(context.getString(R.string.online));
                break;
            case NONE:
                textViewStatus.setText(context.getString(R.string.offline));
        }
        return view;
    }

    public void add(PeopleData peopleData) {
        if (!peopleData.getUid().equals(Utils.getAndroidID(context))) {
            if (!people.contains(peopleData)) {
                people.add(peopleData);
            } else {
                int index = people.indexOf(peopleData);
                people.remove(index);
                if (people.size() == 0)
                    people.add(peopleData);
                else
                    people.set(index, peopleData);
            }
            notifyDataSetChanged();
        }
    }

    public void addAll(List<PeopleData> people) {
        this.people.addAll(people);
        notifyDataSetChanged();
    }

    public void setOffline(PeopleData peopleData) {
        int index = people.indexOf(peopleData);
        PeopleData peopleData1 = people.get(index);
        peopleData1.setAction(PeopleData.ActionType.DISCONNECT);
        notifyDataSetChanged();
    }

    public void allOffline() {
        for (PeopleData peopleData : people) {
            peopleData.setAction(PeopleData.ActionType.NONE);
        }
        notifyDataSetChanged();
    }

    public void remove(int position) {
        if (people.get(position).getAction() == PeopleData.ActionType.DISCONNECT || people.get(position).getAction() == PeopleData.ActionType.NONE)
            people.remove(position);
        notifyDataSetChanged();
    }
}
