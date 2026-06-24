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

import java.util.List;

public class ProtocoloAdapter extends ArrayAdapter<ProtocoloUiState> {

    private final Filter passthroughFilter = createPassthroughFilter();

    public ProtocoloAdapter(@NonNull Context context, @NonNull List<ProtocoloUiState> list) {
        super(context, R.layout.recycler_view_xgp_protocolo, list);
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
        ProtocoloUiState state = getItem(position);
        if (state != null) bind(view, state);
        return view;
    }

    private View inflate(@NonNull ViewGroup parent) {
        return LayoutInflater.from(getContext())
                .inflate(R.layout.recycler_view_xgp_protocolo, parent, false);
    }

    private void bind(@NonNull View view, @NonNull ProtocoloUiState state) {
        TextView textNome = view.findViewById(R.id.text_nome_protocolo);
        TextView textQtd = view.findViewById(R.id.text_qtd_protocolo);
        textNome.setText(state.getDescricao());
        textQtd.setText(quantidadeMedicamentosLabel(state.getQuantidadeMedicamentos()));
    }

    private String quantidadeMedicamentosLabel(int quantidade) {
        return getContext().getResources()
                .getQuantityString(R.plurals.medicamentos_count, quantidade, quantidade);
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
        };
    }
}