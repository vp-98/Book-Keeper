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
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> implements Filterable {

    private final ArrayList<BookModel> books;
    private final ArrayList<BookModel> allBooks;   // will hold all the books that are present
    private final Context mContext;
    private final onDeleteCallListener deleteCallListener;
    private final onEditCallListener editCallListener;

    // Search filter booleans
    private boolean read;
    private boolean notRead;

    //====================================- View Holder Class -=====================================
    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            PopupMenu.OnMenuItemClickListener {

        TextView tvTitleField;         // Item in layout to fill title
        TextView tvAuthorField;        // Item in layout to fill author
        TextView tvLocationField;      // Item in layout to fill location
        ImageButton moreOptions;       // Clickable feature on the recycler view
        ImageView readIcon;            // Item in layout to set check mark

        //==========================================================================================
        /**
         * MyViewHolder:
         *  This will attach the textViews from the layout to the holder class. It will also
         *   add a onClick listener for each item and buttonImage click for each item.
         * @param itemView Constructor for the Item holder
         */
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitleField = itemView.findViewById(R.id.title_textView);
            tvAuthorField = itemView.findViewById(R.id.author_textView);
            tvLocationField = itemView.findViewById(R.id.location_textView);
            moreOptions = itemView.findViewById(R.id.more_options_icon);
            readIcon = itemView.findViewById(R.id.recycler_view_checkIcon);
            moreOptions.setOnClickListener(this);
        }
        //==========================================================================================

        /**
         * onCLick: (overridden method)
         *  Calls the onNoteClick function when a click is registered on the item. Requires Android
         *   10.
         * @param v View of the item.
         */
        @Override
        @RequiresApi(api = Build.VERSION_CODES.Q)
        public void onClick(View v) {
            showContextMenu(v);
        }
        //==========================================================================================

        /**
         * showContextMenu: (overridden method)
         *  Creates and shows the menu for each item that is present in the recycler view. Requires
         *   Android 10.
         * @param view View for the context menu for each item.
         */
        @RequiresApi(api = Build.VERSION_CODES.Q)
        private void showContextMenu(View view) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.inflate(R.menu.item_context_menu);
            popupMenu.setOnMenuItemClickListener(this);
            popupMenu.setForceShowIcon(true);
            popupMenu.show();
        }
        //==========================================================================================

        /**
         * onMenuItemClick: (overridden method)
         *  Registers the clicks to certain menu items and performs the respective tasks.
         * @param item Item listed in the menu.
         * @return True if item is selected, else false.
         */
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.context_edit) {
                editCallListener.openEditFragment(getAdapterPosition());
                return true;
            } else if (item.getItemId() == R.id.context_delete) {
                // remove from Database here
                deleteCallListener.deleteItem(getAdapterPosition());
                return true;
            }
            return false;
        }
    }
    //==============================================================================================

    //===================================- Recycler View Class -====================================

    /**
     * RecyclerViewAdapter:
     *  Constructs custom adapter with all the a list of books, context, and onClickListeners for
     *   certain tasks.
     * @param books                All the books to be populated in the list
     * @param mContext             Context of calling class
     * @param deleteCallListener   Delete handler
     * @param editCallListener     Edit Handler
     */
    public RecyclerViewAdapter(ArrayList<BookModel> books, Context mContext,
                               onDeleteCallListener deleteCallListener,
                               onEditCallListener editCallListener) {
        this.books = books;
        this.allBooks = new ArrayList<>(books);
        this.mContext = mContext;
        this.deleteCallListener = deleteCallListener;
        this.editCallListener = editCallListener;
        read = notRead = true;
    }
    //==============================================================================================

    /**
     * onCreateViewHolder: (overridden method)
     *  Creates the view and takes in a single row layout that will be used for the recycler view.
     * @param parent     Parent View
     * @param viewType   View Type ID
     * @return ViewHolder Item constructed.
     */
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_single_item,
                parent, false);
        return new MyViewHolder(view);
    }
    //==============================================================================================

    /**
     * onBindViewHolder: (overridden method)
     *  Attaches the content to the view using the ViewHolder class. Sets all respective fields in
     *   the layout file with the information from the list of books.
     * @param holder    Item that holds the view
     * @param position  Position of item.
     */
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // Insert the information from each book into the slots
        holder.tvTitleField.setText(books.get(position).getTitle());
        holder.tvAuthorField.setText(books.get(position).getAuthor());
        holder.tvLocationField.setText(books.get(position).getShelfLocation());
        if (books.get(position).getReadStatus()) {
            holder.readIcon.setImageResource(R.drawable.bookview_check_icon);
        } else {
            holder.readIcon.setImageResource(R.drawable.shape_empty_box);
        }
    }
    //==============================================================================================

    /**
     * getItemCount: (overridden method)
     *  This will return the number of items stored in the list that will be displayed in the
     *   recycler view.
     * @return count of books.
     */
    @Override
    public int getItemCount() {
        return books.size();
    }
    //==============================================================================================

    /**
     * provideFilters:
     *  This will reset/update the filter requirements that are sent in by the search fragment.
     * @param read     boolean option reflecting user choice
     * @param notRead  boolean option reflecting user choice
     * @param currStr  current search item in the view.
     */
    public void provideFilters(boolean read, boolean notRead, String currStr) {
        this.read = read;
        this.notRead = notRead;
        getFilter().filter(currStr);
    }
    //==============================================================================================

    //======================================-Filter Requests-=======================================

    /**
     * getFilter:
     *  Create the initial filter request. Filtering will occur on the back thread to prevent
     *   the app from slowing down when filtering large quantities.
     * @return Initial filter request.
     */
    @Override
    public Filter getFilter() {
        return searchFilter;
    }
    //==============================================================================================

    /**
     * searchFilter:
     *  Performs the filtering and provides feedback to the UI fragment (search fragment) after
     *   providing any user provided search constraints.
     */
    private final Filter searchFilter = new Filter() {
        // Executing on the background thread
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<BookModel> filteredList;

            if (constraint == null || constraint.length() == 0) {
                filteredList = new ArrayList<>(generateSearchResults(""));
            } else {
                String filterQuery = constraint.toString().toLowerCase().trim();
                filteredList = generateSearchResults(filterQuery);
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        // Results automatically published
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            books.clear();
            books.addAll((List<BookModel>) results.values);
            notifyDataSetChanged();
        }
    };
    //==============================================================================================

    /**
     * generateSearchResults:
     *  Helper function that narrows down the search results and applies any filter constraints.
     * @param query Search/Filter query entered by user.
     * @return Arraylist of filtered books
     */
    private ArrayList<BookModel> generateSearchResults(String query) {
        ArrayList<BookModel> searchFinds = new ArrayList<>();
        for (BookModel book : allBooks) {
            // Implement with shared preferences here??
            if (book.getTitle().toLowerCase().contains(query) ||
                    book.getAuthor().toLowerCase().contains(query)) {
                if (read && book.getReadStatus()) {
                    searchFinds.add(book);
                }
                if (notRead && !book.getReadStatus()) {
                    searchFinds.add(book);
                }
            }
        }
        return searchFinds;
    }
    //==============================================================================================

    /*-----------------------------------------------------------------------------------
     * onDeleteCallListener: (interface)
     *  Must be implemented in the Fragments/Activities that use this custom adapter.
     *   This will allow for certain actions to happen during an on-click event through
     *   the menu provided to each recycler view item.
     */
    public interface onDeleteCallListener {
        void deleteItem(int position);
    }
    /*-----------------------------------------------------------------------------------
     * onDeleteCallListener: (interface)
     *  Must be implemented in the Fragments/Activities that use this custom adapter.
     *   This will allow for certain actions to happen during an on-click event through
     *   the menu provided to each recycler view item.
     */
    public interface onEditCallListener {
        void openEditFragment(int position);
    }
}
