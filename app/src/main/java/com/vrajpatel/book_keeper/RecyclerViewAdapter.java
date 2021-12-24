/****************************************************************************************
 * Copyright (c) 2021 Vraj Patel <vrajpatel098@gmail.com>                               *
 *                                                                                      *
 * This program is free software; you can redistribute it and/or modify it under        *
 * the terms of the GNU General Public License as published by the Free Software        *
 * Foundation; either version 3 of the License, or (at your option) any later           *
 * version.                                                                             *
 *                                                                                      *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY      *
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A      *
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.             *
 *                                                                                      *
 * You should have received a copy of the GNU General Public License along with         *
 * this program.  If not, see <http://www.gnu.org/licenses/>.                           *
 ****************************************************************************************/

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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> implements Filterable {

    private static final String TAG = "RecyclerViewAdapter";
    private ArrayList<BookModel> books;
    private ArrayList<BookModel> allBooks;   // will hold all the books that are present
    private Context mContext;
    private onDeleteCallListener deleteCallListener;
    private onEditCallListener editCallListener;

    // Search filter booleans
    private boolean read;
    private boolean notRead;

    //====================================- View Holder Class -=====================================
    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        private static final String TAG = "MyViewHolder";
        TextView tvTitleField;         // Item in layout to fill title
        TextView tvAuthorField;        // Item in layout to fill author
        TextView tvLocationField;      // Item in layout to fill location
        ImageButton moreOptions;       // Clickable feature on the recycler view
        ImageView readIcon;            // Item in layout to set check mark
        onDeleteCallListener deleteCallListener2;

        /*-----------------------------------------------------------------------------------
         * MyViewHolder:
         *  This will attach the textViews from the layout to the holder class. It will also
         *   add a onClick listener for each item and buttonImage click for each item.
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

        /*-----------------------------------------------------------------------------------
         * onClick: (For each item in the recycler view)
         *  This will call the onNoteClick function when a click is registered. It will send
         *   the position of the clicked item to the function allowing for things to happend
         *   when a certain item is clicked.
         */
        @Override
        @RequiresApi(api = Build.VERSION_CODES.Q)
        public void onClick(View v) {
            showContextMenu(v);
        }

        /*-----------------------------------------------------------------------------------
         * showContextMenu:
         *  this will allow the menu to be created for each item that is in the list.
         */
        @RequiresApi(api = Build.VERSION_CODES.Q)
        private void showContextMenu(View view) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.inflate(R.menu.item_context_menu);
            popupMenu.setOnMenuItemClickListener(this);
            popupMenu.setForceShowIcon(true);
            popupMenu.show();
        }
        /*-----------------------------------------------------------------------------------
         * onMenuItemClick:
         *  this will register the clicks to certain menu items and perform those tasks.
         *   Upon the delete request, the interface below must be fulfilled inorder to allow
         *   that deletion to happen from the calling Fragment.
         */
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.context_edit:
                    editCallListener.openEditFragment(getAdapterPosition());
                    return true;
                case R.id.context_delete:
                    // remove from Database here
                    deleteCallListener.deleteItem(getAdapterPosition());
                    return true;
                default:
                    return false;
            }
        }
    }
    //==============================================================================================

    //===================================- Recycler View Class -====================================
    /*----------------------------------------------------------------------------------------------
     * RecyclerViewAdapter:
     *  This will take the onClick listener defined in the class that calls this object. The
     *   interface is provided below and should be implemented in the class that wishes to use the
     *   adapter. It will copy the list that needs to be used and the click listener.
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
    /*----------------------------------------------------------------------------------------------
     * onCreateViewHolder:
     *  This will create the view and take in the single row layout that will be used
     *   to populate the recycler view. It will also provide the onClick listener for
     *   the items present on the view.
     */
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_single_item,
                parent, false);
        return new MyViewHolder(view);
    }
    /*----------------------------------------------------------------------------------------------
     * onBindViewHolder:
     *  This will attach the content to the actual view using the provided ViewHolder.
     *   Sets the title and author fields using the arraylist of books.
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
    /*----------------------------------------------------------------------------------------------
     * getItemCount:
     *  This will return the number of recycler view items that need to be displayed.
     *   Must return the number of items otherwise it will only print a select few onto
     *   the screen.
     */
    @Override
    public int getItemCount() {
        return books.size();
    }
    //==============================================================================================
    /*-----------------------------------------------------------------------------------
     * provideFilters:
     *  This will reset/update the filter requirements that are sent in by the search.
     *   The currStr is used to keep the current search item present in the view.
     */
    public void provideFilters(boolean read, boolean notRead, String currStr) {
        this.read = read;
        this.notRead = notRead;
        getFilter().filter(currStr);
    }
    //======================================-Filter Requests-=======================================
    /*-----------------------------------------------------------------------------------
     * getFilter:
     *  This will create the initial filter requests. These filters occur on the back
     *   thread preventing the app from slowing down or lagging.
     */
    @Override
    public Filter getFilter() {
        return searchFilter;
    }
    /*-----------------------------------------------------------------------------------
     * searchFilter:
     *  This is where the filter is performed and provided back to the UI fragment. This
     *   will take the constraint provided from the search result and narrow down
     *   the items present in allBooks list and apply the read/notRead filters.
     */
    private Filter searchFilter = new Filter() {
        // Executing on the background thread
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            ArrayList<BookModel> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(generateSearchResults(""));
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
            books.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
    //==============================================================================================
    /*-----------------------------------------------------------------------------------
     * generateSearchResults:
     *  Helper function will narrow down the search results and apply the filters to
     *   them here. A list with the matching results will be provided back to the UI.
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
