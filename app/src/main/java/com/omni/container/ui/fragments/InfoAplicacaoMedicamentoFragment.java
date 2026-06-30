package com.omni.container.ui.fragments;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.omni.container.R;

public class InfoAplicacaoMedicamentoFragment extends DialogFragment {
    public static final String TAG = "InfoAplicacaoFragment";
    private static final String ARG_KEY_TITULO = "arg_titulo";
    private static final String ARG_KEY_MENSAGEM = "arg_mensagem";

    public InfoAplicacaoMedicamentoFragment() {
    }

    @NonNull
    public static InfoAplicacaoMedicamentoFragment newInstance(@Nullable String titulo, @Nullable String mensagem) {
        InfoAplicacaoMedicamentoFragment fragment = new InfoAplicacaoMedicamentoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_KEY_TITULO, titulo);
        args.putString(ARG_KEY_MENSAGEM, mensagem);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new MaterialAlertDialogBuilder(requireContext())
                .setIcon(R.drawable.info_24px)
                .setTitle(getTitulo())
                .setMessage(getMensagem())
                .setPositiveButton(getString(R.string.text_info_medicamento_acao),
                        (dialog, which) -> dialog.dismiss()).create();
    }

    @Nullable
    private String getTitulo() {
        Bundle args = getArguments();
        if (args == null) return getString(R.string.title_info_medicamento);
        return args.getString(ARG_KEY_TITULO, getString(R.string.title_info_medicamento));
    }

    @Nullable
    private String getMensagem() {
        Bundle args = getArguments();
        if (args == null) return null;
        return args.getString(ARG_KEY_MENSAGEM);
    }
}