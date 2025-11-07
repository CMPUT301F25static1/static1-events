package com.static1.fishylottery.view.events;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.static1.fishylottery.R;
import com.static1.fishylottery.model.entities.Profile;
import java.util.List;

public class EntrantAdapter extends RecyclerView.Adapter<EntrantAdapter.EntrantViewHolder> {

    private List<Profile> entrants;
    private ListType listType;

    public enum ListType {
        WAITING,
        INVITED,
        ENROLLED
    }

    public EntrantAdapter(List<Profile> entrants, ListType listType) {
        this.entrants = entrants;
        this.listType = listType;
    }

    public void updateData(List<Profile> newEntrants) {
        this.entrants = newEntrants;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EntrantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_entrant, parent, false);
        return new EntrantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EntrantViewHolder holder, int position) {
        Profile entrant = entrants.get(position);
        holder.bind(entrant, listType);
    }

    @Override
    public int getItemCount() {
        return entrants.size();
    }

    class EntrantViewHolder extends RecyclerView.ViewHolder {
        private TextView textEntrantName;
        private Button btnAction;

        public EntrantViewHolder(@NonNull View itemView) {
            super(itemView);
            textEntrantName = itemView.findViewById(R.id.text_entrant_name);;
        }

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