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

import java.util.Collections;
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
        sortCheckedToTop();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_item_medicamento, parent, false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void handleInfoClick(int position) {
        if (isInvalidPosition(position) || listener == null) return;
        listener.onInfoClicked(items.get(position));
    }

    private void handleCheckChange(int position, boolean isChecked) {
        if (isInvalidPosition(position)) return;
        ItemMedicamentoUiState updated = items.get(position).withChecked(isChecked);
        items.set(position, updated);
        notifyCheckChanged(updated, isChecked);
        moveToSortedPosition(position, isChecked);
    }

    private void notifyCheckChanged(@NonNull ItemMedicamentoUiState item, boolean isChecked) {
        if (listener == null) return;
        listener.onCheckChanged(item, isChecked);
    }

    private void moveToSortedPosition(int from, boolean isChecked) {
        int to = getSortedTarget(isChecked);
        if (from == to) return;
        items.add(to, items.remove(from));
        notifyItemMoved(from, to);
    }

    private int getSortedTarget(boolean isChecked) {
        int checkedCount = countChecked();
        return isChecked ? checkedCount - 1 : checkedCount;
    }

    private int countChecked() {
        int count = 0;
        for (ItemMedicamentoUiState item : items) {
            if (item.isChecked()) count++;
        }
        return count;
    }

    private void sortCheckedToTop() {
        items.sort((a, b) -> Boolean.compare(b.isChecked(), a.isChecked()));
    }

    private boolean isInvalidPosition(int position) {
        return position < 0 || position >= items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textNome;
        private final ImageView btnInfo;
        private final CheckBox checkboxAdicionar;

        @NonNull
        private final ItemMedicamentoAdapter adapter;

        ViewHolder(@NonNull View itemView, @NonNull ItemMedicamentoAdapter adapter) {
            super(itemView);
            this.textNome = itemView.findViewById(R.id.text_nome);
            this.btnInfo = itemView.findViewById(R.id.btn_info);
            this.checkboxAdicionar = itemView.findViewById(R.id.checkbox_adicionar);
            this.adapter = adapter;
            setupClickListeners();
        }

        private void setupClickListeners() {
            checkboxAdicionar.setOnClickListener(v -> handleCheckboxClick());
            btnInfo.setOnClickListener(v -> handleInfoClick());
        }

        void bind(@NonNull ItemMedicamentoUiState item) {
            textNome.setText(item.getDescricao());
            checkboxAdicionar.setChecked(item.isChecked());
        }

        private void handleCheckboxClick() {
            int position = getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;
            adapter.handleCheckChange(position, checkboxAdicionar.isChecked());
        }

        private void handleInfoClick() {
            int position = getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;
            adapter.handleInfoClick(position);
        }
    }
}