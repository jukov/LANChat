package org.jukov.lanchat.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jukov.lanchat.R;
import org.jukov.lanchat.dto.ChatData;
import org.jukov.lanchat.util.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jukov on 07.05.2016.
 */
public final class ChatAdapter extends BaseAdapter {

    @SuppressWarnings("unused")
    public static final String TAG = RoomsAdapter.class.getSimpleName();

    private final Context context;
    private final List<ChatData> messages;
    private final LayoutInflater layoutInflater;

    private final String myUID;

    @SuppressWarnings("unused")
    public ChatAdapter(Context context) {
        this(context, new ArrayList<ChatData>());
    }

    public ChatAdapter(Context context, List<ChatData> messages) {
        this.messages = messages;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        myUID = Utils.getAndroidID(context);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public ChatData getItem(int position) {
        return messages.get(position);
    }

    @SuppressWarnings("deprecation")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.listview_chat, parent, false);
        }

        ChatData chatData = getItem(position);


        TextView textViewName = (TextView) view.findViewById(R.id.nameText);
        textViewName.setText(chatData.getText());
        TextView textViewMessage = (TextView) view.findViewById(R.id.messageText);
        textViewMessage.setText(chatData.getText());
        TextView textViewDate = (TextView) view.findViewById(R.id.dateText);
        textViewDate.setText(Utils.getSendMessageDate(chatData.getSendDate()));

        LinearLayout layout = (LinearLayout) view
                .findViewById(R.id.bubble_layout);
        LinearLayout parent_layout = (LinearLayout) view
                .findViewById(R.id.bubble_layout_parent);

        if (chatData.getUid().equals(myUID)) {
            layout.setBackgroundResource(R.drawable.ic_outcoming);
            textViewName.setVisibility(View.GONE);
            textViewMessage.setTextColor(Color.BLACK);
            textViewDate.setTextColor(context.getResources().getColor(R.color.purple400));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.END;
            textViewDate.setLayoutParams(layoutParams);
            parent_layout.setGravity(Gravity.END);
        } else {
            layout.setBackgroundResource(R.drawable.ic_incoming);
            textViewName.setVisibility(View.VISIBLE);
            textViewName.setText(chatData.getName());
            textViewName.setTextColor(context.getResources().getColor(R.color.purple100));
            textViewMessage.setTextColor(Color.WHITE);
            textViewDate.setTextColor(context.getResources().getColor(R.color.purple100));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.START;
            textViewDate.setLayoutParams(layoutParams);
            parent_layout.setGravity(Gravity.START);
        }
        return view;
    }

    public void add(ChatData chatData) {
        if (messages.contains(chatData)) {
            return;
        }
        messages.add(chatData);
        notifyDataSetChanged();
    }

}
