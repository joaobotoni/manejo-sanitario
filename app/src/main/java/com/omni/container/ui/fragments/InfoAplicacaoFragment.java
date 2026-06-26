package com.omni.container.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.omni.container.R;


public class InfoAplicacaoFragment extends DialogFragment {
    public static final String TAG = "InfoAplicacaoFragment";
    private static final String ARG_KEY_APLICACAO = "arg_aplicacao";

    public InfoAplicacaoFragment() {
    }

    @NonNull
    public static InfoAplicacaoFragment newInstance(@Nullable String aplicacao) {
        InfoAplicacaoFragment fragment = new InfoAplicacaoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_KEY_APLICACAO, aplicacao);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireContext())
                .setIcon(R.drawable.info_24px)
                .setMessage(getAplicacao())
                .create();
    }

    @Nullable
    private String getAplicacao() {
        Bundle args = getArguments();
        if (args == null) return null;
        return args.getString(ARG_KEY_APLICACAO);
    }
}