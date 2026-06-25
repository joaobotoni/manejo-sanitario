package com.omni.container.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.checkbox.MaterialCheckBox;
import com.omni.container.R;
import com.omni.container.ui.states.OrigemItem;
import com.omni.container.ui.states.ProtocoloItemSelecionadoUiState;

import java.util.List;

public class ProtocoloItemSelecionadoAdapter extends RecyclerView.Adapter<ProtocoloItemSelecionadoAdapter.ViewHolder> {
    private static final char STATUS_APLICADO = 'S';
    private static final char STATUS_NAO_APLICADO = 'N';
    @NonNull
    private final List<ProtocoloItemSelecionadoUiState> list;

    public ProtocoloItemSelecionadoAdapter(@NonNull List<ProtocoloItemSelecionadoUiState> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflate(parent));
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
        return LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_item_protocolo_selecionado, parent, false);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView textNomeMedicamento;
        private final TextView textOrigemMedicamento;
        private final TextView textDose;
        private final ImageView btnRemoverMedicamento;
        private final MaterialCheckBox checkAplicado;
        private final MaterialCheckBox checkNaoAplicado;
        private ProtocoloItemSelecionadoUiState currentState;
        private boolean binding;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textNomeMedicamento = itemView.findViewById(R.id.text_nome_medicamento);
            textOrigemMedicamento = itemView.findViewById(R.id.text_origem_medicamento);
            textDose = itemView.findViewById(R.id.text_dose);
            btnRemoverMedicamento = itemView.findViewById(R.id.btn_remover_medicamento);
            checkAplicado = itemView.findViewById(R.id.check_aplicado);
            checkNaoAplicado = itemView.findViewById(R.id.check_nao_aplicado);

            checkAplicado.setOnCheckedChangeListener((v, isChecked) -> onAplicadoChanged(isChecked));
            checkNaoAplicado.setOnCheckedChangeListener((v, isChecked) -> onNaoAplicadoChanged(isChecked));
            btnRemoverMedicamento.setOnClickListener(v -> removeCurrentItem());
        }

        void bind(@NonNull ProtocoloItemSelecionadoUiState state) {
            this.currentState = state;
            bindTexts(state);
            bindStatus(state);
            bindVisibilidadeRemover(state);
        }

        private void bindTexts(@NonNull ProtocoloItemSelecionadoUiState state) {
            textNomeMedicamento.setText(state.getDescricao());
            textOrigemMedicamento.setText(state.getOrigem().getNome());
            textDose.setText(String.valueOf(state.getQuantidadeAplicada()));
        }

        private void bindStatus(@NonNull ProtocoloItemSelecionadoUiState state) {
            binding = true;
            checkAplicado.setChecked(state.getStatus() == STATUS_APLICADO);
            checkNaoAplicado.setChecked(state.getStatus() == STATUS_NAO_APLICADO);
            binding = false;
        }

        private void bindVisibilidadeRemover(@NonNull ProtocoloItemSelecionadoUiState state) {
            boolean isProtocolo = state.getOrigem() == OrigemItem.PROTOCOLO;
            btnRemoverMedicamento.setVisibility(isProtocolo ? View.GONE : View.VISIBLE);
        }

        private void onAplicadoChanged(boolean isChecked) {
            if (binding || !isChecked) return;
            checkNaoAplicado.setChecked(false);
            currentState.setStatus(STATUS_APLICADO);
        }

        private void onNaoAplicadoChanged(boolean isChecked) {
            if (binding || !isChecked) return;
            checkAplicado.setChecked(false);
            currentState.setStatus(STATUS_NAO_APLICADO);
        }

        private void removeCurrentItem() {
            int position = getBindingAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;
            list.remove(position);
            notifyItemRemoved(position);
        }
    }
}