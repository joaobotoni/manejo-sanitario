package com.omni.container.ui.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.omni.container.R;
import com.omni.container.data.AppDatabase;
import com.omni.container.data.dao.ProtocoloItemDao;
import com.omni.container.data.entities.ProtocoloItem;
import com.omni.container.ui.adapters.ProtocoloItemAdapter;
import com.omni.container.ui.adapters.ProtocoloItemAdapter.OnProtocoloItemClickListener;
import com.omni.container.ui.states.ProtocoloItemUiState;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ConsultaProtocoloItemFragment extends Fragment {
    private static final String TAG = "CONSULTA_PROTOCOLO_ITEM_FRAGMENT";
    private static final int THREAD_POOL_SIZE = 4;
    private static final int MIN_CARACTERES_BUSCA = 3;
    private Executor executor;
    private EditText editBusca;
    private Button btnConfirmar;
    private RecyclerView recyclerProtocoloItensConsulta;
    private ProtocoloItemAdapter adapter;

    private final List<ProtocoloItemUiState> items = new ArrayList<>();
    private final List<ProtocoloItemUiState> todosItens = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_consulta_item_protocolo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        createExecutor();
        bindViews(view);
        setupAdapter();
        setupBusca();
        setupClickListeners();
        fetchItems();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executor.close();
        releaseViews();
    }

    private void createExecutor() {
        executor = new Executor(createExecutorDeThreads(), createHandlerDaMainThread());
    }

    private void bindViews(@NonNull View view) {
        editBusca = view.findViewById(R.id.edit_busca);
        recyclerProtocoloItensConsulta = view.findViewById(R.id.recycler_protocolo_itens_consulta);
        btnConfirmar = view.findViewById(R.id.btn_confirmar);
    }

    private void setupAdapter() {
        adapter = new ProtocoloItemAdapter(items, createProtocoloItemClickListener());
        setupVerticalRecyclerView(recyclerProtocoloItensConsulta, adapter, requireContext());
    }

    private void setupBusca() {
        editBusca.addTextChangedListener(new SearchTextWatcher(this::handleBusca));
    }

    private void setupClickListeners() {
        btnConfirmar.setOnClickListener(v -> navegarParaTraz());
    }

    private OnProtocoloItemClickListener createProtocoloItemClickListener() {
        return new OnProtocoloItemClickListener() {

            @Override
            public void onInfoClicked(@NonNull ProtocoloItemUiState state) {

            }

            @Override
            public void onCheckChanged(@NonNull ProtocoloItemUiState state, boolean isChecked) {

            }
        };
    }

    private void handleInfoClicked(@NonNull ProtocoloItemUiState state) {
        // TODO: abrir info do protocolo item
    }

    private void handleAddClicked(@NonNull ProtocoloItemUiState state) {
        // TODO: adicionar protocolo item à seleção
    }

    private void handleBusca() {
        String termo = getTermoBusca();
        showItems(hasMinimoCaracteres(termo) ? filtrarPorTermo(termo) : todosItens);
    }

    private boolean hasMinimoCaracteres(@NonNull String termo) {
        return termo.length() >= MIN_CARACTERES_BUSCA;
    }

    private String getTermoBusca() {
        return editBusca.getText().toString().trim();
    }

    private List<ProtocoloItemUiState> filtrarPorTermo(@NonNull String termo) {
        return todosItens.stream()
                .filter(item -> contemTermo(item, termo))
                .collect(Collectors.toList());
    }

    private boolean contemTermo(@NonNull ProtocoloItemUiState item, @NonNull String termo) {
        return item.getDescricao().toLowerCase().contains(termo.toLowerCase());
    }

    private void fetchItems() {
        executor.execute(requireContext(), AppDatabase::protocoloItemDao, ProtocoloItemDao::getAll, this::handleItems, this::handleErroAoBuscarItems);
    }

    private void handleItems(@NonNull List<ProtocoloItem> protocoloItems) {
        updateTodosItens(Mapper.fromItensToUiStateList(protocoloItems));
    }

    private void updateTodosItens(@NonNull List<ProtocoloItemUiState> novos) {
        todosItens.clear();
        todosItens.addAll(novos);
        showItems(todosItens);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showItems(@NonNull List<ProtocoloItemUiState> list) {
        items.clear();
        items.addAll(list);
        adapter.notifyDataSetChanged();
    }

    private void handleErroAoBuscarItems(@NonNull Throwable throwable) {
        showSnackBarErro(getString(R.string.erro_carregar_protocolo_itens));
        Log.d(TAG, getString(R.string.erro_carregar_protocolo_itens) + throwable.getMessage());
    }

    private void showSnackBarErro(@NonNull String message) {
        View view = requireView();
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(view.getContext(), android.R.color.holo_red_dark))
                .setTextColor(Color.WHITE)
                .show();
    }

    private void navegarParaTraz() {
        getParentFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .replace(R.id.fragment_container_view, new ManejoSanitarioFragment())
                .commit();
    }

    private void releaseViews() {
        editBusca = null;
        recyclerProtocoloItensConsulta = null;
        btnConfirmar = null;
        adapter = null;
    }

    public static void setupVerticalRecyclerView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.Adapter<?> adapter, @NonNull Context context) {
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
    }

    private ExecutorService createExecutorDeThreads() {
        return Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    private Handler createHandlerDaMainThread() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return Handler.createAsync(Looper.getMainLooper());
        }
        return new Handler(Looper.getMainLooper());
    }

    public abstract static class BaseTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    public static final class SearchTextWatcher extends BaseTextWatcher {
        private final Runnable onChanged;

        public SearchTextWatcher(@NonNull Runnable onChanged) {
            this.onChanged = Objects.requireNonNull(onChanged, "onChanged must not be null");
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            onChanged.run();
        }
    }

    private static final class Mapper {

        static List<ProtocoloItemUiState> fromItensToUiStateList(@NonNull List<ProtocoloItem> itens) {
            return itens.stream()
                    .map(Mapper::fromProtocoloItemToUiState)
                    .collect(Collectors.toList());
        }

        private static ProtocoloItemUiState fromProtocoloItemToUiState(@NonNull ProtocoloItem protocoloItem) {
            return new ProtocoloItemUiState(protocoloItem.getIdProtocoloItem(), protocoloItem.getDescricao(), false);
        }
    }

    private static final class Executor implements Closeable {
        private final Handler handler;
        private final ExecutorService executor;
        private volatile boolean cancelled = false;

        Executor(@NonNull ExecutorService executor, @NonNull Handler handler) {
            this.executor = executor;
            this.handler = handler;
        }

        <D, E> void execute(@NonNull Context context, @NonNull Function<AppDatabase, D> daoExtractor, @NonNull Function<D, E> query, @NonNull Consumer<E> onSuccess, @NonNull Consumer<Exception> onError) {
            submit(() -> query.apply(resolveDao(context, daoExtractor)), onSuccess, onError);
        }

        <D, P, E> void execute(@NonNull Context context, @NonNull Function<AppDatabase, D> daoExtractor, @NonNull BiFunction<D, P, E> query, @NonNull P param, @NonNull Consumer<E> onSuccess, @NonNull Consumer<Exception> onError) {
            submit(() -> query.apply(resolveDao(context, daoExtractor), param), onSuccess, onError);
        }

        private <T> void submit(@NonNull Callable<T> task, @NonNull Consumer<T> onSuccess, @NonNull Consumer<Exception> onError) {
            executor.submit(() -> runTask(task, onSuccess, onError));
        }

        @NonNull
        private <D> D resolveDao(@NonNull Context context, @NonNull Function<AppDatabase, D> daoExtractor) {
            return daoExtractor.apply(AppDatabase.getDatabase(context.getApplicationContext()));
        }

        private <T> void runTask(@NonNull Callable<T> task, @NonNull Consumer<T> onSuccess, @NonNull Consumer<Exception> onError) {
            try {
                T result = task.call();
                post(() -> onSuccess.accept(result));
            } catch (Exception e) {
                post(() -> onError.accept(e));
            }
        }

        private void post(@NonNull Runnable action) {
            if (isCancelled()) return;
            handler.post(() -> runIfActive(action));
        }

        private void runIfActive(@NonNull Runnable action) {
            if (isCancelled()) return;
            action.run();
        }

        private boolean isCancelled() {
            return cancelled;
        }

        @Override
        public synchronized void close() {
            cancelled = true;
            executor.shutdown();
        }
    }
}