package com.omni.container.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.omni.container.R;
import com.omni.container.ui.states.ProtocoloItemUiState;

import java.util.List;

public class ProtocoloItemAdapter extends RecyclerView.Adapter<ProtocoloItemAdapter.ViewHolder> {

    public interface OnProtocoloItemClickListener {
        void onInfoClicked(@NonNull ProtocoloItemUiState state);

        void onCheckChanged(@NonNull ProtocoloItemUiState state, boolean isChecked);
    }

    @NonNull
    private final List<ProtocoloItemUiState> list;

    @Nullable
    private final OnProtocoloItemClickListener clickListener;

    public ProtocoloItemAdapter(
            @NonNull List<ProtocoloItemUiState> list,
            @Nullable OnProtocoloItemClickListener clickListener
    ) {
        this.list = list;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflate(parent), clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private View inflate(@NonNull ViewGroup parent) {
        return LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_view_item_protocolo, parent, false);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textNome;
        private final ImageView btnInfo;
        private final CheckBox checkboxAdicionar;
        @Nullable
        private final OnProtocoloItemClickListener clickListener;
        @Nullable
        private ProtocoloItemUiState currentState;

        ViewHolder(@NonNull View itemView, @Nullable OnProtocoloItemClickListener clickListener) {
            super(itemView);
            this.textNome = itemView.findViewById(R.id.text_nome);
            this.btnInfo = itemView.findViewById(R.id.btn_info);
            this.checkboxAdicionar = itemView.findViewById(R.id.checkbox_adicionar);
            this.clickListener = clickListener;

            btnInfo.setOnClickListener(v -> notifyInfoClicked());
            checkboxAdicionar.setOnCheckedChangeListener((buttonView, isChecked) -> notifyCheckChanged(isChecked));
        }

        void bind(@NonNull ProtocoloItemUiState state) {
            this.currentState = state;
            textNome.setText(state.getDescricao());

            checkboxAdicionar.setOnCheckedChangeListener(null);
            checkboxAdicionar.setChecked(state.isChecked());
            checkboxAdicionar.setOnCheckedChangeListener((buttonView, isChecked) -> notifyCheckChanged(isChecked));
        }

        private void notifyInfoClicked() {
            if (clickListener == null || currentState == null) return;
            clickListener.onInfoClicked(currentState);
        }

        private void notifyCheckChanged(boolean isChecked) {
            if (clickListener == null || currentState == null) return;
            clickListener.onCheckChanged(currentState, isChecked);
        }
    }
}