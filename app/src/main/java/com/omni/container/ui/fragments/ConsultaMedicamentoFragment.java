package com.omni.container.ui.fragments;

import static com.omni.container.ui.states.OrigemItem.PROTOCOLO;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.omni.container.data.entities.Protocolo;
import com.omni.container.data.entities.ProtocoloItem;
import com.omni.container.ui.adapters.ProtocoloItemAdapter;
import com.omni.container.ui.adapters.ProtocoloItemAplicacaoAdapter;
import com.omni.container.ui.states.ProtocoloItemAplicacaoUiState;
import com.omni.container.ui.states.ProtocoloItemUiState;
import com.omni.container.ui.states.ProtocoloUiState;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;


public class ConsultaMedicamentoFragment extends Fragment {
    private static final String TAG = "CONSULTA_MEDICAMENTO_FRAGMENT";
    private static final int THREAD_POOL_SIZE = 4;
    private EditText editBusca;
    private Button btnConfirmar;
    private RecyclerView recyclerMedicamentosConsulta;
    private ProtocoloItemAdapter adapter;
    private List<ProtocoloItemUiState> items = new ArrayList<>();
    private Executor executor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_consulta_medicamento, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        createExecutor();
        bindViews(view);
        fetchItems();
        setupAdapter();
        setupClickListeners();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executor.close();
        releaseViews();
    }

    private void bindViews(@NonNull View view) {
        editBusca = view.findViewById(R.id.edit_busca);
        recyclerMedicamentosConsulta = view.findViewById(R.id.recycler_medicamentos_consulta);
        btnConfirmar = view.findViewById(R.id.btn_confirmar);
    }

    private void createExecutor() {
        executor = new Executor(createExecutorDeThreads(), createHandlerDaMainThread());
    }


    private void releaseViews() {
        editBusca = null;
        recyclerMedicamentosConsulta = null;
    }

    private void showSnackBarErro(@NonNull String message) {
        View view = requireView();
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(view.getContext(), android.R.color.holo_red_dark))
                .setTextColor(Color.WHITE)
                .show();
    }


    @SuppressLint("NotifyDataSetChanged")
    private void showItems(@NonNull List<ProtocoloItemUiState> list) {
        items.clear();
        items.addAll(list);
        adapter.notifyDataSetChanged();
    }

    private void setupClickListeners() {
        btnConfirmar.setOnClickListener(v -> navegarParaTraz());
    }

    private void setupAplicacaoRecyclerView() {
        adapter = new ProtocoloItemAdapter(items, null);
        setupVerticalRecyclerView(recyclerMedicamentosConsulta, adapter, requireContext());
    }

    private void navegarParaTraz() {
        getParentFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .replace(R.id.fragment_container_view, new ManejoSanitarioFragment())
                .commit();
    }

    private void setupAdapter() {
        setupAplicacaoRecyclerView();
    }

    public static void setupVerticalRecyclerView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.Adapter<?> adapter, @NonNull Context context) {
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
    }

    private void fetchItems() {
        executor.execute(requireContext(), AppDatabase::protocoloItemDao, ProtocoloItemDao::getAll, this::handleItems, this::handleErroAoBuscarItems);
    }

    private void handleItems(@NonNull List<ProtocoloItem> protocoloItems) {
        showItems(Mapper.fromItensToUiStateList(protocoloItems));
    }

    private void handleErroAoBuscarItems(@NonNull Throwable throwable) {
        showSnackBarErro(getString(R.string.erro_carregar_items));
        Log.d(TAG, getString(R.string.erro_carregar_items_protocolo) + throwable.getMessage());
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

    private static final class Mapper {

        static List<ProtocoloItemUiState> fromItensToUiStateList(@NonNull List<ProtocoloItem> itens) {
            return itens.stream()
                    .map(Mapper::fromProtocoloItemToUiState)
                    .collect(Collectors.toList());
        }

        private static ProtocoloItemUiState fromProtocoloItemToUiState(@NonNull ProtocoloItem protocoloItem) {
            return new ProtocoloItemUiState(protocoloItem.getIdProtocoloItem(), protocoloItem.getDescricao());
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
