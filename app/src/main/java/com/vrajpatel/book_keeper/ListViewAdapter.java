//****************************************************************************************
//* Copyright (c) 2022 Vraj Patel <vrajpatel098@gmail.com>                               *
//*                                                                                      *
//* This program is free software; you can redistribute it and/or modify it under        *
//* the terms of the GNU General Public License as published by the Free Software        *
//* Foundation; either version 3 of the License, or (at your option) any later           *
//* version.                                                                             *
//*                                                                                      *
//* This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
//* WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
//* PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
//*                                                                                      *
//* You should have received a copy of the GNU General Public License along with         *
//* this program.  If not, see <http://www.gnu.org/licenses/>.                           *
//****************************************************************************************/

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

public class ListViewAdapter extends ArrayAdapter<String> {

    private static final String TAG = "ListViewAdapter";
    private final Context mContext;
    private final int mResource;
    private final ArrayList<String> shelfNames;
    private final onDeleteIconPressListener deleteIconPressListener;

    //==============================================================================================
    /**
     * ListViewAdapter:
     *  Constructor for the custom list view adapter class.
     * @param context                   Context of calling class
     * @param resource                  Resource identification
     * @param objects                   Objects that will be used in the list
     * @param deleteIconPressListener   Delete handler
     */
    public ListViewAdapter(@NonNull Context context, int resource, @NonNull ArrayList<String> objects,
                           onDeleteIconPressListener deleteIconPressListener) {
        super(context, resource, objects);
        mContext = context;
        mResource = resource;
        this.shelfNames = objects;
        this.deleteIconPressListener = deleteIconPressListener;
    }
    //==============================================================================================

    /**
     * getView: (overridden method)
     *  Fills in the values corresponding the layout in use for the listview adapter class with
     *   the shelf name/location. The delete icon is only added for user added items.
     * @param position          Position of the list item
     * @param convertView       Convert View
     * @param parent            Parent Layout
     * @return View of the list item.
     */
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
            deleteIcon.setOnClickListener(v -> deleteIconPressListener.deleteItem(position));
        } else {
            deleteIcon.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }
    //==============================================================================================

    /**
     * getCount: (overridden method)
     *  Returns the count of items in the arraylist stored in this class.
     * @return size of arraylist
     */
    @Override
    public int getCount() {
        return shelfNames.size();
    }
    //==============================================================================================

    /**
     * getItem: (overridden method)
     *  Returns the shelf name at a certain position from the list of names.
     * @param position  index of a given shelf name
     * @return Shelf name at selected position.
     */
    @Nullable
    @Override
    public String getItem(int position) {
        return shelfNames.get(position);
    }
    //==============================================================================================

    // Interface used to help facilitate the deleting process
    public interface onDeleteIconPressListener {
        void deleteItem(int position);
    }
}
