package com.vrajpatel.book_keeper;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ListViewAdapter extends ArrayAdapter<String> {
    private static final String TAG = "ListViewAdapter";
    private Context mContext;
    private int mResource;
    private ArrayList<String> shelfNames;
    private onDeleteIconPressListener deleteIconPressListener;

    public ListViewAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> objects,
                           onDeleteIconPressListener deleteIconPressListener) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        this.shelfNames = objects;
        this.deleteIconPressListener = deleteIconPressListener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        String location = getItem(position);

        LayoutInflater inflater = LayoutInflater.from(mContext);
        convertView = inflater.inflate(mResource, parent, false);

        TextView tvLocation = convertView.findViewById(R.id.listview_shelf_name);
        tvLocation.setText(location);
        ImageButton deleteIcon = convertView.findViewById(R.id.listview_shelf_delete);
        Log.e(TAG, "getView: Adding: " + location);
        if (!location.equals("Default")) {
            deleteIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteIconPressListener.deleteItem(position);
                }
            });
        } else {
            deleteIcon.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return shelfNames.size();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return shelfNames.get(position);
    }

    public interface onDeleteIconPressListener {
        void deleteItem(int position);
    }

}
