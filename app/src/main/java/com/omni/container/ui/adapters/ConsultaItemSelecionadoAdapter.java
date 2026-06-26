package com.omni.container.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.omni.container.R;
import com.omni.container.ui.states.ItemMedicamentoUiState;

import java.util.List;

public class ConsultaItemSelecionadoAdapter extends RecyclerView.Adapter<ConsultaItemSelecionadoAdapter.ViewHolder> {
    public interface OnItemRemovidoListener {
        void onItemRemovido(@NonNull ItemMedicamentoUiState item);
    }
    @NonNull
    private final List<ItemMedicamentoUiState> items;

    @Nullable
    private final OnItemRemovidoListener listener;

    public ConsultaItemSelecionadoAdapter(@NonNull List<ItemMedicamentoUiState> items, @Nullable OnItemRemovidoListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflate(parent), listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private View inflate(@NonNull ViewGroup parent) {
        return LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_view_consulta_item_medicamento_selecionado, parent, false);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textNomeMedicamento;
        private final ImageView btnRemoverMedicamento;
        @Nullable
        private final OnItemRemovidoListener listener;
        @Nullable
        private ItemMedicamentoUiState currentState;

        ViewHolder(@NonNull View itemView, @Nullable OnItemRemovidoListener listener) {
            super(itemView);
            this.textNomeMedicamento = itemView.findViewById(R.id.text_nome_medicamento);
            this.btnRemoverMedicamento = itemView.findViewById(R.id.btn_remover_medicamento);
            this.listener = listener;
            btnRemoverMedicamento.setOnClickListener(v -> notifyItemRemovido());
        }

        void bind(@NonNull ItemMedicamentoUiState state) {
            this.currentState = state;
            textNomeMedicamento.setText(state.getDescricao());
        }

        private void notifyItemRemovido() {
            if (listener == null || currentState == null) return;
            listener.onItemRemovido(currentState);
        }
    }
}
