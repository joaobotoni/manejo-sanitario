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
import com.omni.container.ui.adapters.ItemMedicamentoAdapter;
import com.omni.container.ui.adapters.ItemMedicamentoAdapter.OnProtocoloItemClickListener;
import com.omni.container.ui.states.OrigemItem;
import com.omni.container.ui.states.ItemMedicamentoUiState;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
    private static final String TAG = "FRAG_CONSULTA_PROTOCOLO";
    private static final int THREAD_POOL_SIZE = 4;
    private static final int MIN_CARACTERES_BUSCA = 3;
    private static final String STATE_KEY_SELECTED_IDS = "state_selected_ids";

    public static final String RESULT_KEY_PROTOCOLO_ITENS_SELECIONADOS = "result_protocolo_itens_selecionados";
    public static final String ARG_KEY_PROTOCOLO_ITENS_SELECIONADOS = "arg_protocolo_itens_selecionados";

    // Views
    private EditText editBusca;
    private Button btnConfirmar;
    private TextView textItemsSelecionados;
    private RecyclerView recyclerConsulta;
    private RecyclerView recyclerSelecionados;

    private Executor executor;
    private ItemMedicamentoAdapter consultaAdapter;
    private ConsultaItemSelecionadoAdapter selecionadosAdapter;

    private final Map<Integer, ProtocoloItem> originalItemsMap = new LinkedHashMap<>();
    private final List<ItemMedicamentoUiState> masterItems = new ArrayList<>();
    private final List<ItemMedicamentoUiState> displayedItems = new ArrayList<>();
    private final List<ItemMedicamentoUiState> selectedItems = new ArrayList<>();
    private final Set<Integer> selectedIds = new LinkedHashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_consulta_item_protocolo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupExecutor();
        setupViews(view);
        setupAdapters();
        setupListeners();
        setupInitialState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putIntArray(STATE_KEY_SELECTED_IDS, CollectionUtils.toIntArray(selectedIds));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executor.close();
        clearViews();
    }


    private void setupExecutor() {
        executor = new Executor(createExecutorDeThreads(), createHandlerDaMainThread());
    }

    private void setupViews(@NonNull View view) {
        editBusca = view.findViewById(R.id.edit_busca);
        textItemsSelecionados = view.findViewById(R.id.text_items_selecionados);
        recyclerConsulta = view.findViewById(R.id.recycler_protocolo_itens_consulta);
        recyclerSelecionados = view.findViewById(R.id.recycler_items_selecionados);
        btnConfirmar = view.findViewById(R.id.btn_confirmar);
    }

    private void setupAdapters() {
        consultaAdapter = new ItemMedicamentoAdapter(displayedItems, createConsultaListener());
        ViewUtils.setupVerticalRecyclerView(recyclerConsulta, consultaAdapter, requireContext());

        selecionadosAdapter = new ConsultaItemSelecionadoAdapter(selectedItems, this::handleItemRemovido);
        ViewUtils.setupVerticalRecyclerView(recyclerSelecionados, selecionadosAdapter, requireContext());
    }

    private void setupListeners() {
        btnConfirmar.setOnClickListener(v -> handleConfirmar());
        editBusca.addTextChangedListener(new SearchTextWatcher(this::refreshDisplayedItems));
    }

    private void setupInitialState(@Nullable Bundle savedInstanceState) {
        restoreSelectedIdsState(savedInstanceState);
        bindCurrentState();
        fetchItemsIfNeeded();
    }

    private void fetchItemsIfNeeded() {
        if (!masterItems.isEmpty()) return;

        executor.execute(requireContext(),
                AppDatabase::protocoloItemDao,
                ProtocoloItemDao::getAll,
                this::handleItemsFetchSuccess,
                this::handleItemsFetchError);
    }

    private void handleItemsFetchSuccess(@NonNull List<ProtocoloItem> protocoloItems) {
        originalItemsMap.clear();
        masterItems.clear();

        for (ProtocoloItem item : protocoloItems) {
            originalItemsMap.put(item.getIdProtocoloItem(), item);
            masterItems.add(Mapper.fromProtocoloItemToUiState(item));
        }

        bindCurrentState();
    }

    private void handleItemsFetchError(@NonNull Throwable throwable) {
        showErrorSnackBar(getString(R.string.erro_carregar_protocolo_itens));
        Log.e(TAG, "Erro ao carregar itens: ", throwable);
    }

    private void bindCurrentState() {
        refreshDisplayedItems();
        refreshSelectedItems();
        updateSelectedCounter();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshDisplayedItems() {
        displayedItems.clear();
        String termoBusca = getTermoBusca();
        boolean validarFiltro = termoBusca.length() >= MIN_CARACTERES_BUSCA;

        for (ItemMedicamentoUiState item : masterItems) {
            if (!validarFiltro || matchesTermo(item, termoBusca)) {
                displayedItems.add(projetarSelecaoParaView(item));
            }
        }
        consultaAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshSelectedItems() {
        selectedItems.clear();
        for (Integer id : selectedIds) {
            ProtocoloItem original = originalItemsMap.get(id);
            if (original != null) {
                selectedItems.add(Mapper.fromProtocoloItemToUiState(original).withChecked(true));
            }
        }
        selecionadosAdapter.notifyDataSetChanged();
    }

    private void updateSelectedCounter() {
        if (textItemsSelecionados != null) {
            textItemsSelecionados.setText(getString(R.string.format_qtd_itens_selecionados, selectedIds.size()));
        }
    }


    private OnProtocoloItemClickListener createConsultaListener() {
        return new OnProtocoloItemClickListener() {
            @Override
            public void onInfoClicked(@NonNull ItemMedicamentoUiState state) {
                // TODO: Abrir info do item (Ex: InfoAplicacaoFragment)
            }

            @Override
            public void onCheckChanged(@NonNull ItemMedicamentoUiState state, boolean isChecked) {
                handleCheckChanged(state.getId(), isChecked);
            }
        };
    }

    private void handleItemRemovido(@NonNull ItemMedicamentoUiState state) {
        handleCheckChanged(state.getId(), false);
    }

    private void handleCheckChanged(int id, boolean isChecked) {
        if (isChecked) {
            selectedIds.add(id);
        } else {
            selectedIds.remove(id);
        }
        updateDisplayedItemState(id, isChecked);
        refreshSelectedItems();
        updateSelectedCounter();
    }

    private void handleConfirmar() {
        List<ProtocoloItem> selecionados = buildSelectedEntities();
        if (selecionados.isEmpty()) return;

        sendResultToParent(selecionados);
        navigateBack();
    }

    private void updateDisplayedItemState(int id, boolean isChecked) {
        int index = getIndexOfDisplayedItem(id);
        if (index == RecyclerView.NO_POSITION) return;

        ItemMedicamentoUiState updatedItem = displayedItems.get(index).withChecked(isChecked);
        displayedItems.set(index, updatedItem);
        consultaAdapter.notifyItemChanged(index);
    }

    @NonNull
    private List<ProtocoloItem> buildSelectedEntities() {
        List<ProtocoloItem> result = new ArrayList<>(selectedIds.size());
        for (Integer id : selectedIds) {
            ProtocoloItem item = originalItemsMap.get(id);
            if (item != null) {
                item.setSelected(true);
                result.add(item);
            }
        }
        return result;
    }

    private ItemMedicamentoUiState projetarSelecaoParaView(@NonNull ItemMedicamentoUiState item) {
        return selectedIds.contains(item.getId()) ? item.withChecked(true) : item;
    }

    private int getIndexOfDisplayedItem(int id) {
        for (int i = 0; i < displayedItems.size(); i++) {
            if (displayedItems.get(i).getId() == id) return i;
        }
        return RecyclerView.NO_POSITION;
    }

    private boolean matchesTermo(@NonNull ItemMedicamentoUiState item, @NonNull String termo) {
        return item.getDescricao().toLowerCase().contains(termo.toLowerCase());
    }

    @NonNull
    private String getTermoBusca() {
        return editBusca.getText().toString().trim();
    }

    private void sendResultToParent(@NonNull List<ProtocoloItem> selecionados) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(ARG_KEY_PROTOCOLO_ITENS_SELECIONADOS, new ArrayList<>(selecionados));
        getParentFragmentManager().setFragmentResult(RESULT_KEY_PROTOCOLO_ITENS_SELECIONADOS, bundle);
    }

    private void navigateBack() {
        getParentFragmentManager().popBackStack();
    }

    private void restoreSelectedIdsState(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) return;
        int[] ids = savedInstanceState.getIntArray(STATE_KEY_SELECTED_IDS);
        if (ids == null) return;
        selectedIds.clear();
        for (int id : ids) selectedIds.add(id);
    }

    private void showErrorSnackBar(@NonNull String message) {
        View view = requireView();
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(view.getContext(), android.R.color.holo_red_dark))
                .setTextColor(Color.WHITE)
                .show();
    }

    private void clearViews() {
        editBusca = null;
        textItemsSelecionados = null;
        recyclerConsulta = null;
        recyclerSelecionados = null;
        btnConfirmar = null;
        consultaAdapter = null;
        selecionadosAdapter = null;
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


    private static final class ViewUtils {
        static void setupVerticalRecyclerView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.Adapter<?> adapter, @NonNull Context context) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(adapter);
        }
    }

    private static final class CollectionUtils {
        static int[] toIntArray(@NonNull Set<Integer> ids) {
            int[] array = new int[ids.size()];
            int i = 0;
            for (Integer id : ids) array[i++] = id;
            return array;
        }
    }

    public abstract static class BaseTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
    }

    public static final class SearchTextWatcher extends BaseTextWatcher {
        private final Runnable onChanged;

        public SearchTextWatcher(@NonNull Runnable onChanged) {
            this.onChanged = Objects.requireNonNull(onChanged);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            onChanged.run();
        }
    }

    private static final class Mapper {
        static ItemMedicamentoUiState fromProtocoloItemToUiState(@NonNull ProtocoloItem item) {
            return new ItemMedicamentoUiState(item.getIdProtocoloItem(), item.getDescricao(), OrigemItem.AVULSO, false);
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

        private boolean isCancelled() { return cancelled; }

        @Override
        public synchronized void close() {
            cancelled = true;
            executor.shutdown();
        }
    }
}