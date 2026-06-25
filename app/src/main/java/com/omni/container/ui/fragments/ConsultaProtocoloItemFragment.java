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
import android.widget.TextView;

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
import com.omni.container.ui.adapters.ConsultaItemSelecionadoAdapter;
import com.omni.container.ui.adapters.ProtocoloItemAdapter;
import com.omni.container.ui.adapters.ProtocoloItemAdapter.OnProtocoloItemClickListener;
import com.omni.container.ui.states.OrigemItem;
import com.omni.container.ui.states.ProtocoloItemUiState;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ConsultaProtocoloItemFragment extends Fragment {
    private static final String TAG = "CONSULTA_PROTOCOLO_ITEM_FRAGMENT";
    private static final int THREAD_POOL_SIZE = 4;
    private static final int MIN_CARACTERES_BUSCA = 3;
    private static final String STATE_KEY_CHECKED_IDS = "state_checked_ids";

    public static final String RESULT_KEY_PROTOCOLO_ITENS_SELECIONADOS = "result_protocolo_itens_selecionados";
    public static final String ARG_KEY_PROTOCOLO_ITENS_SELECIONADOS = "arg_protocolo_itens_selecionados";

    private static final Set<Integer> pendingCheckedIds = new HashSet<>();

    private Executor executor;
    private EditText editBusca;
    private Button btnConfirmar;
    private TextView textItemsSelecionados;
    private RecyclerView recyclerProtocoloItensConsulta;
    private RecyclerView recyclerItemsSelecionados;
    private ProtocoloItemAdapter adapter;
    private ConsultaItemSelecionadoAdapter selectedAdapter;

    private final List<ProtocoloItemUiState> allItems = new ArrayList<>();
    private final List<ProtocoloItemUiState> displayedItems = new ArrayList<>();
    private final List<ProtocoloItemUiState> selectedItems = new ArrayList<>();
    private final Map<Integer, ProtocoloItem> originalItemMap = new HashMap<>();
    private boolean confirmed;

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
        setupAdapters();
        setupBusca();
        setupClickListeners();
        initState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray(STATE_KEY_CHECKED_IDS, getCheckedIds());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (!confirmed) {
            cachePendingSelection();
        }
        executor.close();
        releaseViews();
    }

    private int[] getCheckedIds() {
        int[] ids = new int[selectedItems.size()];
        for (int i = 0; i < selectedItems.size(); i++) {
            ids[i] = selectedItems.get(i).getId();
        }
        return ids;
    }

    private void createExecutor() {
        executor = new Executor(createExecutorDeThreads(), createHandlerDaMainThread());
    }

    private void bindViews(@NonNull View view) {
        editBusca = view.findViewById(R.id.edit_busca);
        textItemsSelecionados = view.findViewById(R.id.text_items_selecionados);
        recyclerProtocoloItensConsulta = view.findViewById(R.id.recycler_protocolo_itens_consulta);
        recyclerItemsSelecionados = view.findViewById(R.id.recycler_items_selecionados);
        btnConfirmar = view.findViewById(R.id.btn_confirmar);
    }

    private void initState(@Nullable Bundle savedInstanceState) {
        restoreCheckedIds(savedInstanceState);
        pendingCheckedIds.addAll(restoredCachedIds);
        fetchItemsSeNecessario();
    }

    private final Set<Integer> restoredCachedIds = new HashSet<>();

    private void restoreCheckedIds(@Nullable Bundle savedInstanceState) {
        restoredCachedIds.clear();
        if (savedInstanceState != null) {
            int[] ids = savedInstanceState.getIntArray(STATE_KEY_CHECKED_IDS);
            if (ids != null) {
                for (int id : ids) restoredCachedIds.add(id);
                pendingCheckedIds.clear();
            }
        }
        restoredCachedIds.addAll(pendingCheckedIds);
    }

    private void cachePendingSelection() {
        pendingCheckedIds.clear();
        for (ProtocoloItemUiState item : selectedItems) {
            pendingCheckedIds.add(item.getId());
        }
    }

    private void clearPendingSelection() {
        pendingCheckedIds.clear();
    }

    private void setupAdapters() {
        setupBottomAdapter();
        setupTopAdapter();
    }

    private void setupBottomAdapter() {
        adapter = new ProtocoloItemAdapter(displayedItems, createProtocoloItemClickListener());
        setupVerticalRecyclerView(recyclerProtocoloItensConsulta, adapter, requireContext());
    }

    private void setupTopAdapter() {
        selectedAdapter = new ConsultaItemSelecionadoAdapter(selectedItems, this::handleItemRemovido);
        setupVerticalRecyclerView(recyclerItemsSelecionados, selectedAdapter, requireContext());
    }

    private void setupBusca() {
        editBusca.addTextChangedListener(new SearchTextWatcher(this::handleBusca));
    }

    private void setupClickListeners() {
        btnConfirmar.setOnClickListener(v -> handleConfirmar());
    }

    private OnProtocoloItemClickListener createProtocoloItemClickListener() {
        return new OnProtocoloItemClickListener() {

            @Override
            public void onInfoClicked(@NonNull ProtocoloItemUiState state) {
            }

            @Override
            public void onCheckChanged(@NonNull ProtocoloItemUiState state, boolean isChecked) {
                handleCheckChanged(state, isChecked);
            }
        };
    }

    private void handleCheckChanged(@NonNull ProtocoloItemUiState state, boolean isChecked) {
        ProtocoloItemUiState updated = state.withChecked(isChecked);
        if (!replaceInList(allItems, state, updated)) return;
        replaceInList(displayedItems, state, updated);
        if (isChecked) {
            attachToSelected(updated);
        } else {
            detachFromSelected(state);
        }
        updateCounter();
    }

    private boolean replaceInList(@NonNull List<ProtocoloItemUiState> list, @NonNull ProtocoloItemUiState oldItem, @NonNull ProtocoloItemUiState newItem) {
        int index = list.indexOf(oldItem);
        if (isPosicaoInvalida(index)) return false;
        list.set(index, newItem);
        return true;
    }

    private void attachToSelected(@NonNull ProtocoloItemUiState item) {
        selectedItems.add(item);
        selectedAdapter.notifyItemInserted(selectedItems.size() - 1);
    }

    private void detachFromSelected(@NonNull ProtocoloItemUiState item) {
        int position = selectedItems.indexOf(item);
        if (isPosicaoInvalida(position)) return;
        selectedItems.remove(position);
        selectedAdapter.notifyItemRemoved(position);
    }

    private void handleItemRemovido(@NonNull ProtocoloItemUiState state) {
        ProtocoloItemUiState updated = state.withChecked(false);
        if (!replaceInList(allItems, state, updated)) return;
        replaceInList(displayedItems, state, updated);
        detachFromSelected(state);
        updateCounter();
    }

    private void handleConfirmar() {
        List<ProtocoloItem> selecionados = buildSelectedProtocoloItems();
        if (selecionados.isEmpty()) return;
        confirmed = true;
        clearPendingSelection();
        sendResult(selecionados);
        resetState();
        navegarParaTraz();
    }

    @NonNull
    private List<ProtocoloItem> buildSelectedProtocoloItems() {
        List<ProtocoloItem> selected = new ArrayList<>();
        for (ProtocoloItemUiState state : allItems) {
            if (state.isChecked()) {
                ProtocoloItem original = originalItemMap.get(state.getId());
                if (original != null) {
                    original.setSelected(true);
                    selected.add(original);
                }
            }
        }
        return selected;
    }

    private void sendResult(@NonNull List<ProtocoloItem> selecionados) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(ARG_KEY_PROTOCOLO_ITENS_SELECIONADOS, new ArrayList<>(selecionados));
        getParentFragmentManager().setFragmentResult(RESULT_KEY_PROTOCOLO_ITENS_SELECIONADOS, bundle);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void resetState() {
        allItems.clear();
        originalItemMap.clear();
        selectedItems.clear();
        displayedItems.clear();
        adapter.notifyDataSetChanged();
        selectedAdapter.notifyDataSetChanged();
        updateCounter();
    }

    private void navegarParaTraz() {
        getParentFragmentManager().popBackStack();
    }

    private void handleBusca() {
        rebuildDisplayedItems();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void rebuildDisplayedItems() {
        displayedItems.clear();
        String termo = getTermoBusca();
        if (hasMinimoCaracteres(termo)) {
            for (ProtocoloItemUiState item : allItems) {
                if (contemTermo(item, termo)) displayedItems.add(item);
            }
        } else {
            displayedItems.addAll(allItems);
        }
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void rebuildSelectedItems() {
        selectedItems.clear();
        for (ProtocoloItemUiState item : allItems) {
            if (item.isChecked()) selectedItems.add(item);
        }
        selectedAdapter.notifyDataSetChanged();
    }

    private boolean hasMinimoCaracteres(@NonNull String termo) {
        return termo.length() >= MIN_CARACTERES_BUSCA;
    }

    @NonNull
    private String getTermoBusca() {
        return editBusca.getText().toString().trim();
    }

    private boolean contemTermo(@NonNull ProtocoloItemUiState item, @NonNull String termo) {
        return item.getDescricao().toLowerCase().contains(termo.toLowerCase());
    }

    private void fetchItemsSeNecessario() {
        if (hasItemsCarregados()) return;
        fetchItems();
    }

    private boolean hasItemsCarregados() {
        return !allItems.isEmpty();
    }

    private void fetchItems() {
        executor.execute(requireContext(), AppDatabase::protocoloItemDao, ProtocoloItemDao::getAll, this::handleItems, this::handleErroAoBuscarItems);
    }

    private void handleItems(@NonNull List<ProtocoloItem> protocoloItems) {
        originalItemMap.clear();
        for (ProtocoloItem item : protocoloItems) {
            originalItemMap.put(item.getIdProtocoloItem(), item);
        }
        updateAllItems(Mapper.fromItensToUiStateList(protocoloItems));
    }

    private void updateAllItems(@NonNull List<ProtocoloItemUiState> novos) {
        allItems.clear();
        allItems.addAll(novos);
        applyCachedSelection();
        rebuildDisplayedItems();
        rebuildSelectedItems();
        updateCounter();
    }

    private void applyCachedSelection() {
        if (restoredCachedIds.isEmpty()) return;
        for (int i = 0; i < allItems.size(); i++) {
            if (restoredCachedIds.contains(allItems.get(i).getId())) {
                allItems.set(i, allItems.get(i).withChecked(true));
            }
        }
        restoredCachedIds.clear();
    }

    private void updateCounter() {
        if (textItemsSelecionados == null) return;
        textItemsSelecionados.setText(getString(R.string.format_qtd_itens_selecionados, selectedItems.size()));
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

    private void releaseViews() {
        editBusca = null;
        textItemsSelecionados = null;
        recyclerProtocoloItensConsulta = null;
        recyclerItemsSelecionados = null;
        btnConfirmar = null;
        adapter = null;
        selectedAdapter = null;
    }

    private boolean isPosicaoInvalida(int position) {
        return position == RecyclerView.NO_POSITION || position < 0;
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
            List<ProtocoloItemUiState> result = new ArrayList<>(itens.size());
            for (ProtocoloItem item : itens) {
                result.add(fromProtocoloItemToUiState(item));
            }
            return result;
        }

        private static ProtocoloItemUiState fromProtocoloItemToUiState(@NonNull ProtocoloItem protocoloItem) {
            return new ProtocoloItemUiState(
                    protocoloItem.getIdProtocoloItem(),
                    protocoloItem.getDescricao(),
                    OrigemItem.AVULSO,
                    false
            );
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