package com.omni.container.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.omni.container.R;

public class InfoAplicacaoFragment extends DialogFragment {
    public static String TAG = "InfoAplicacaoFragment";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new AlertDialog.Builder(requireContext())
                .setIcon(R.drawable.info_24px)
                .setMessage("")
                .setPositiveButton("", (dialog, which) -> {})
                .create();
    }
}
