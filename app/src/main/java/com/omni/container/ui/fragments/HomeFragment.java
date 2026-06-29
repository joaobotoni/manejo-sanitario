package com.omni.container.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.omni.container.R;

public class HomeFragment extends Fragment {
    private static final String TAG = HomeFragment.class.getSimpleName();
    public static final String RESULT_KEY_ANIMAL = "result_animal";
    public static final String ARG_KEY_ID_ANIMAL = "arg_id_animal";
    public static final String ARG_KEY_COD_BOTTOM = "arg_cod_bottom";
    public static final String ARG_KEY_PESO = "arg_peso";

    private EditText inputIdAnimal;
    private EditText inputCodBottom;
    private EditText inputPeso;
    private Button buttonEnviar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);
        bindViews(view);
        setClickListeners();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        releaseViews();
    }

    private void bindViews(View view) {
        inputIdAnimal = view.findViewById(R.id.etCampo1);
        inputCodBottom = view.findViewById(R.id.etCampo2);
        inputPeso = view.findViewById(R.id.etCampo3);
        buttonEnviar = view.findViewById(R.id.btnSalvar);
    }

    private void releaseViews() {
        inputIdAnimal = null;
        inputCodBottom = null;
        inputPeso = null;
        buttonEnviar = null;
    }

    private void setClickListeners() {
        buttonEnviar.setOnClickListener(v -> sendResult());
    }

    private void sendResult() {
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_KEY_ID_ANIMAL, parseInt(getText(inputIdAnimal)));
        bundle.putString(ARG_KEY_COD_BOTTOM, getText(inputCodBottom));
        bundle.putDouble(ARG_KEY_PESO, parseDouble(getText(inputPeso)));
        getParentFragmentManager().setFragmentResult(RESULT_KEY_ANIMAL, bundle);
        getParentFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.fragment_container_view, new ManejoSanitarioFragment())
                .commit();
    }

    private String getText(EditText editText) {
        if (editText == null) {
            return "";
        }

        return editText.getText().toString().trim();
    }

    private int parseInt(String value) {

        if (isEmpty(value)) {
            return -1;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Erro ao converter inteiro.", e);
            return -1;
        }
    }

    private double parseDouble(String value) {
        if (isEmpty(value)) {
            return -1D;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Erro ao converter double.", e);
            return -1D;
        }
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }
}