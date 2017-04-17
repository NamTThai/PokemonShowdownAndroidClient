package com.pokemonshowdown.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.pokemonshowdown.R;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Integer> avatarIds;

    public ImageAdapter(Context context, ArrayList<Integer> avatarIds) {
        this.context = context;
        this.avatarIds = avatarIds;
    }

    @Override
    public int getCount() {
        return avatarIds.size();
    }

    @Override
    public Object getItem(int position) {
        return avatarIds.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.listwidget_avatar, null);
        }

        ImageView imgIcon = (ImageView) convertView.findViewById(R.id.avatar_icon);
        imgIcon.setImageResource(avatarIds.get(position));

        return convertView;
    }
}
