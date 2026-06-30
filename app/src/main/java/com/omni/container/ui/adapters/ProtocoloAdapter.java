package com.omni.container.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.omni.container.R;
import com.omni.container.ui.states.ProtocoloUiState;

import java.util.Collection;
import java.util.List;

public class ProtocoloAdapter extends ArrayAdapter<ProtocoloUiState> {
    private final Filter passthroughFilter = createPassthroughFilter();

    public ProtocoloAdapter(@NonNull Context context, @NonNull List<ProtocoloUiState> list) {
        super(context, R.layout.list_view_protocolo, list);
    }

    public static boolean isItemEmBranco(@Nullable ProtocoloUiState state) {
        return state == null || state == ProtocoloUiState.EM_BRANCO;
    }

    @Override
    public void addAll(@NonNull Collection<? extends ProtocoloUiState> collection) {
        super.addAll(collection);
        garantirItemEmBrancoNoTopo();
    }

    private void garantirItemEmBrancoNoTopo() {
        if (!isPrimeiroItemEmBranco()) insert(ProtocoloUiState.EM_BRANCO, 0);
    }

    private boolean isPrimeiroItemEmBranco() {
        return !isEmpty() && isItemEmBranco(getItem(0));
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return bindView(position, convertView, parent);
    }

    @NonNull
    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return bindView(position, convertView, parent);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return passthroughFilter;
    }

    @NonNull
    private View bindView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView != null ? convertView : inflate(parent);
        bind(view, getItem(position));
        return view;
    }

    private View inflate(@NonNull ViewGroup parent) {
        return LayoutInflater.from(getContext())
                .inflate(R.layout.list_view_protocolo, parent, false);
    }

    private void bind(@NonNull View view, @Nullable ProtocoloUiState state) {
        TextView textNome = view.findViewById(R.id.text_nome_protocolo);
        TextView textQtd = view.findViewById(R.id.text_qtd_protocolo);
        textNome.setText(getDescricaoLabel(state));
        textQtd.setText(getQuantidadeMedicamentosLabel(state));
    }

    @NonNull
    private String getDescricaoLabel(@Nullable ProtocoloUiState state) {
        if (isItemEmBranco(state)) return " ";
        return state.getDescricao();
    }

    @NonNull
    private String getQuantidadeMedicamentosLabel(@Nullable ProtocoloUiState state) {
        if (isItemEmBranco(state)) return " ";
        return getContext().getResources()
                .getQuantityString(R.plurals.protocolo_itens_selecionados_count,
                        state.getQuantidadeMedicamentos(), state.getQuantidadeMedicamentos());
    }

    private Filter createPassthroughFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                results.count = getCount();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                notifyDataSetChanged();
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                if (resultValue == null) return " ";
                return ((ProtocoloUiState) resultValue).getDescricao();
            }
        };
    }
}