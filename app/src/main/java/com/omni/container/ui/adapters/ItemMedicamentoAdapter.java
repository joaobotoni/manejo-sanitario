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
import com.omni.container.ui.states.ItemMedicamentoUiState;

import java.util.List;

public class ItemMedicamentoAdapter extends RecyclerView.Adapter<ItemMedicamentoAdapter.ViewHolder> {

    public interface OnProtocoloItemClickListener {
        void onInfoClicked(@NonNull ItemMedicamentoUiState state);
        void onCheckChanged(@NonNull ItemMedicamentoUiState state, boolean isChecked);
    }

    @NonNull
    private final List<ItemMedicamentoUiState> items;

    @Nullable
    private final OnProtocoloItemClickListener listener;

    public ItemMedicamentoAdapter(
            @NonNull List<ItemMedicamentoUiState> items,
            @Nullable OnProtocoloItemClickListener listener
    ) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_view_item_medicamento, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textNome;
        private final ImageView btnInfo;
        private final CheckBox checkboxAdicionar;

        @Nullable
        private final OnProtocoloItemClickListener listener;

        @Nullable
        private ItemMedicamentoUiState currentItem;

        ViewHolder(@NonNull View itemView, @Nullable OnProtocoloItemClickListener listener) {
            super(itemView);
            this.textNome = itemView.findViewById(R.id.text_nome);
            this.btnInfo = itemView.findViewById(R.id.btn_info);
            this.checkboxAdicionar = itemView.findViewById(R.id.checkbox_adicionar);
            this.listener = listener;
            setupClickListeners();
        }

        private void setupClickListeners() {
            checkboxAdicionar.setOnClickListener(v -> handleCheckboxClick());
            btnInfo.setOnClickListener(v -> handleInfoClick());
        }

        void bind(@NonNull ItemMedicamentoUiState item) {
            this.currentItem = item;
            textNome.setText(item.getDescricao());
            checkboxAdicionar.setChecked(item.isChecked());
        }

        private void handleCheckboxClick() {
            if (listener == null || currentItem == null) return;
            boolean isChecked = checkboxAdicionar.isChecked();
            listener.onCheckChanged(currentItem, isChecked);
        }

        private void handleInfoClick() {
            if (listener == null || currentItem == null) return;
            listener.onInfoClicked(currentItem);
        }
    }
}