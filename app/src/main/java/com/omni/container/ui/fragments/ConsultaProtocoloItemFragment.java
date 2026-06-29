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
import com.omni.container.data.dao.ItemDao;
import com.omni.container.data.entities.Item;
import com.omni.container.ui.adapters.ItemMedicamentoAdapter;
import com.omni.container.ui.adapters.ItemMedicamentoAdapter.OnProtocoloItemClickListener;
import com.omni.container.ui.states.ItemMedicamentoUiState;
import com.omni.container.ui.states.OrigemItem;

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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConsultaProtocoloItemFragment extends Fragment {

    private static final String TAG = "FRAG_CONSULTA_PROTOCOLO";
    private static final int THREAD_POOL_SIZE = 4;
    private static final int MIN_CARACTERES_BUSCA = 3;

    public static final String RESULT_KEY_PROTOCOLO_ITENS_SELECIONADOS = "result_protocolo_itens_selecionados";
    public static final String ARG_KEY_PROTOCOLO_ITENS_SELECIONADOS = "arg_protocolo_itens_selecionados";

    private static final String STATE_KEY_SELECTED_IDS = "state_selected_ids";

    private EditText editBusca;
    private Button btnConfirmar;
    private TextView textItemsSelecionados;
    private RecyclerView recyclerConsulta;

    private DbExecutor executor;
    private ItemMedicamentoAdapter consultaAdapter;

    private final Map<Integer, Item> originalItemsMap = new LinkedHashMap<>();
    private final List<ItemMedicamentoUiState> displayedItems = new ArrayList<>();
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
        setupEstadoInicial(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        saveSelectedIdsState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executor.close();
        clearViews();
    }


    private void setupExecutor() {
        executor = new DbExecutor(Executors.newFixedThreadPool(THREAD_POOL_SIZE), createMainThreadHandler());
    }

    private void setupViews(@NonNull View view) {
        editBusca = view.findViewById(R.id.edit_busca);
        textItemsSelecionados = view.findViewById(R.id.text_items_selecionados);
        recyclerConsulta = view.findViewById(R.id.recycler_protocolo_itens_consulta);
        btnConfirmar = view.findViewById(R.id.btn_confirmar);
    }

    private void setupAdapters() {
        consultaAdapter = new ItemMedicamentoAdapter(displayedItems, createListenerConsulta());
        ViewUtils.setupVerticalRecyclerView(recyclerConsulta, consultaAdapter, requireContext());
    }

    private void setupListeners() {
        btnConfirmar.setOnClickListener(v -> handleConfirmar());
        editBusca.addTextChangedListener(new SearchTextWatcher(this::refreshDisplayedItems));
    }

    private void setupEstadoInicial(@Nullable Bundle savedInstanceState) {
        restoreState(savedInstanceState);
        bindEstadoAtual();
        fetchItensIfNeeded();
    }

    private OnProtocoloItemClickListener createListenerConsulta() {
        return new OnProtocoloItemClickListener() {
            @Override
            public void onInfoClicked(@NonNull ItemMedicamentoUiState state) {
                showDialogInfoProtocolo(state.getAplicacao());
            }

            @Override
            public void onCheckChanged(@NonNull ItemMedicamentoUiState state, boolean isChecked) {
                handleCheckChanged(state.getId(), isChecked);
            }
        };
    }


    private void bindEstadoAtual() {
        refreshDisplayedItems();
        showContadorSelecionados();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void refreshDisplayedItems() {
        displayedItems.clear();
        displayedItems.addAll(buildDisplayedItems());
        consultaAdapter.notifyDataSetChanged();
    }

    private void updateEstadoItemExibido(int id, boolean isChecked) {
        int index = getIndexOfDisplayedItem(id);
        if (isInvalidPosition(index)) return;
        displayedItems.set(index, displayedItems.get(index).withChecked(isChecked));
        consultaAdapter.notifyItemChanged(index);
    }

    private void showContadorSelecionados() {
        textItemsSelecionados.setText(getString(R.string.format_qtd_itens_selecionados, selectedIds.size()));
    }

    private void showDialogInfoProtocolo(@NonNull String mensagem) {
        InfoAplicacaoFragment.newInstance(mensagem).show(getParentFragmentManager(), InfoAplicacaoFragment.TAG);
    }

    private void showSnackBarErro(@NonNull String mensagem) {
        View view = requireView();
        Snackbar.make(view, mensagem, Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(view.getContext(), android.R.color.holo_red_dark))
                .setTextColor(Color.WHITE)
                .show();
    }


    private void handleFetchItensSuccess(@NonNull List<Item> itens) {
        originalItemsMap.clear();
        for (Item item : itens) {
            originalItemsMap.put(item.getIdItem(), item);
        }
        bindEstadoAtual();
    }

    private void handleFetchItensError(@NonNull Throwable throwable) {
        showSnackBarErro(getString(R.string.erro_carregar_protocolo_itens));
        Log.e(TAG, "Erro ao carregar itens: ", throwable);
    }

    private void handleCheckChanged(int id, boolean isChecked) {
        updateSelectedId(id, isChecked);
        updateEstadoItemExibido(id, isChecked);
        showContadorSelecionados();
    }

    private void handleConfirmar() {
        List<Item> selecionados = buildSelectedEntities();
        if (isEmptyList(selecionados)) {
            showSnackBarErro(getString(R.string.erro_nenhum_item_selecionado));
            return;
        }
        sendResultToParent(selecionados);
        navigateBack();
    }

    private void fetchItensIfNeeded() {
        if (hasItensCarregados()) return;
        executor.execute(requireContext(), AppDatabase::itemDao, ItemDao::getAll,
                this::handleFetchItensSuccess, this::handleFetchItensError);
    }


    private void navigateBack() {
        getParentFragmentManager().popBackStack();
    }

    @NonNull
    private List<ItemMedicamentoUiState> buildDisplayedItems() {
        String termo = getTermoBusca();
        return originalItemsMap.values().stream()
                .map(Mapper::fromItemToUiState)
                .filter(item -> isVisivelNaBusca(item, termo))
                .map(this::buildItemComSelecao)
                .collect(Collectors.toList());
    }

    @NonNull
    private List<Item> buildSelectedEntities() {
        return streamSelectedOriginais().collect(Collectors.toList());
    }

    @NonNull
    private ItemMedicamentoUiState buildItemComSelecao(@NonNull ItemMedicamentoUiState item) {
        return isItemSelecionado(item) ? item.withChecked(true) : item;
    }


    @NonNull
    private Stream<Item> streamSelectedOriginais() {
        return selectedIds.stream()
                .map(originalItemsMap::get)
                .filter(Objects::nonNull);
    }

    private int getIndexOfDisplayedItem(int id) {
        for (int i = 0; i < displayedItems.size(); i++) {
            if (displayedItems.get(i).getId() == id) return i;
        }
        return RecyclerView.NO_POSITION;
    }

    @NonNull
    private String getTermoBusca() {
        return editBusca.getText().toString().trim();
    }

    private void sendResultToParent(@NonNull List<Item> selecionados) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(ARG_KEY_PROTOCOLO_ITENS_SELECIONADOS, new ArrayList<>(selecionados));
        getParentFragmentManager().setFragmentResult(RESULT_KEY_PROTOCOLO_ITENS_SELECIONADOS, bundle);
    }

    private void updateSelectedId(int id, boolean isChecked) {
        if (isChecked) {
            selectedIds.add(id);
            return;
        }
        selectedIds.remove(id);
    }

    private void saveSelectedIdsState(@NonNull Bundle outState) {
        outState.putIntegerArrayList(STATE_KEY_SELECTED_IDS, new ArrayList<>(selectedIds));
    }

    private void restoreState(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) return;
        restoreSelectedIdsState(savedInstanceState);
    }

    private void restoreSelectedIdsState(@NonNull Bundle savedInstanceState) {
        List<Integer> ids = savedInstanceState.getIntegerArrayList(STATE_KEY_SELECTED_IDS);
        if (isEmptyList(ids)) return;
        selectedIds.clear();
        selectedIds.addAll(ids);
    }

    private void clearViews() {
        executor = null;
        editBusca = null;
        textItemsSelecionados = null;
        recyclerConsulta = null;
        btnConfirmar = null;
        consultaAdapter = null;
    }


    private boolean isVisivelNaBusca(@NonNull ItemMedicamentoUiState item, @NonNull String termo) {
        if (!hasTermoValido(termo)) return true;
        return hasTermoNaDescricao(item, termo);
    }

    private boolean hasTermoValido(@NonNull String termo) {
        return termo.length() >= MIN_CARACTERES_BUSCA;
    }

    private boolean hasTermoNaDescricao(@NonNull ItemMedicamentoUiState item, @NonNull String termo) {
        return item.getDescricao().toLowerCase().contains(termo.toLowerCase());
    }

    private boolean isItemSelecionado(@NonNull ItemMedicamentoUiState item) {
        return selectedIds.contains(item.getId());
    }

    private boolean isInvalidPosition(int position) {
        return position == RecyclerView.NO_POSITION;
    }

    private boolean isEmptyList(@Nullable List<?> lista) {
        return lista == null || lista.isEmpty();
    }

    private boolean hasItensCarregados() {
        return !originalItemsMap.isEmpty();
    }


    private Handler createMainThreadHandler() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return Handler.createAsync(Looper.getMainLooper());
        }
        return new Handler(Looper.getMainLooper());
    }


    private static final class ViewUtils {

        static void setupVerticalRecyclerView(@NonNull RecyclerView recyclerView,
                                              @NonNull RecyclerView.Adapter<?> adapter,
                                              @NonNull Context context) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(adapter);
        }
    }


    private static final class Mapper {

        static ItemMedicamentoUiState fromItemToUiState(@NonNull Item item) {
            return new ItemMedicamentoUiState(item.getIdItem(), item.getDescricao(), item.getAplicacao(), OrigemItem.AVULSO, false);
        }
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
            this.onChanged = Objects.requireNonNull(onChanged);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            onChanged.run();
        }
    }


    private static final class DbExecutor implements Closeable {

        private final Handler handler;
        private final ExecutorService executor;
        private volatile boolean cancelled = false;

        DbExecutor(@NonNull ExecutorService executor, @NonNull Handler handler) {
            this.executor = executor;
            this.handler = handler;
        }

        <D, E> void execute(@NonNull Context context, @NonNull Function<AppDatabase, D> daoExtractor, @NonNull Function<D, E> query,
                            @NonNull Consumer<E> onSuccess, @NonNull Consumer<Exception> onError) {
            submit(() -> query.apply(resolveDao(context, daoExtractor)), onSuccess, onError);
        }

        private <T> void submit(@NonNull Callable<T> task, @NonNull Consumer<T> onSuccess,
                                @NonNull Consumer<Exception> onError) {
            executor.submit(() -> runTask(task, onSuccess, onError));
        }

        @NonNull
        private <D> D resolveDao(@NonNull Context context, @NonNull Function<AppDatabase, D> daoExtractor) {
            return daoExtractor.apply(AppDatabase.getDatabase(context.getApplicationContext()));
        }

        private <T> void runTask(@NonNull Callable<T> task,
                                 @NonNull Consumer<T> onSuccess,
                                 @NonNull Consumer<Exception> onError) {
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