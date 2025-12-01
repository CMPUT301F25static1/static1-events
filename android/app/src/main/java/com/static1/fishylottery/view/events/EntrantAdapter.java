package com.static1.fishylottery.view.events;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Profile;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying a list of event entrants in a RecyclerView.
 * Supports different list types (waiting, invited, enrolled) with conditional action buttons.
 */
public class EntrantAdapter extends RecyclerView.Adapter<EntrantAdapter.EntrantViewHolder> {

    /** List of entrant profiles to display. */
    private List<Profile> entrants;
    /** Type of the entrant list (waiting, invited, or enrolled). */
    private ListType listType;

    public void getItemId(List<String> profiles) {
    }

    /**
     * Enum defining the types of entrant lists.
     */
    public enum ListType {
        WAITING,
        INVITED,
        ENROLLED
    }

    /**
     * Constructor for the EntrantAdapter.
     * Note: This constructor appears incomplete as it does not initialize fields properly.
     *
     * @param entrants the context (incorrectly typed, should be Context)
     * @param listType the list of profiles (incorrectly typed, should be ListType)
     */
    public EntrantAdapter(Context entrants, ArrayList<Profile> listType) {

    }

    /**
     * Updates the adapter with a new list of entrants.
     *
     * @param newEntrants the new list of entrant profiles
     */
    public void updateData(List<Profile> newEntrants) {
        this.entrants = newEntrants;
        notifyDataSetChanged();
    }

    /**
     * Creates a new ViewHolder for an entrant item.
     *
     * @param parent   the parent ViewGroup
     * @param viewType the view type
     * @return a new EntrantViewHolder
     */
    @NonNull
    @Override
    public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entrant, parent, false);
        return new EntrantViewHolder(view);
    }

    /**
     * Binds the entrant data to the ViewHolder at the specified position.
     *
     * @param holder   the ViewHolder to bind
     * @param position the position of the entrant
     */
    @Override
    public void onBindViewHolder(@NonNull EntrantViewHolder holder, int position) {
        Profile entrant = entrants.get(position);
        holder.bind(entrant, listType);
    }
    /**
     * Returns the total number of entrants in the adapter.
     *
     * @return the number of entrants
     */
    @Override
    public int getItemCount() {
        return entrants.size();
    }

    /**
     * ViewHolder for displaying an entrant item in the RecyclerView.
     */
    class EntrantViewHolder extends RecyclerView.ViewHolder {
        /** TextView for the entrant's name. */
        private TextView textEntrantName;
        /** Button for performing actions on the entrant. */
        private Button btnAction;

        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            textEntrantName = itemView.findViewById(R.id.text_entrant_name);;
        }

        /**
         * Binds the entrant data to the ViewHolder's views.
         *
         * @param entrant  the entrant profile to bind
         * @param listType the type of the entrant list
         */
        public void bind(Profile entrant, ListType listType) {
            textEntrantName.setText(entrant.getFullName());

            switch (listType) {
                case INVITED:
                    btnAction.setVisibility(View.VISIBLE);
                    btnAction.setText("Remove");
                    btnAction.setOnClickListener(v -> {
                        // Handle removal from invited list (US 02.06.04)
                        // This would call the ViewModel to remove the entrant
                    });
                    break;
                case WAITING:
                case ENROLLED:
                default:
                    btnAction.setVisibility(View.GONE);
                    break;
            }
        }
    }
}