
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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.PluralsRes;
import androidx.core.content.ContextCompat;
import androidx.core.os.BundleCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.omni.container.R;
import com.omni.container.data.AppDatabase;
import com.omni.container.data.dao.ProtocoloDao;
import com.omni.container.data.dao.ProtocoloItemDao;
import com.omni.container.data.entities.Protocolo;
import com.omni.container.data.entities.ProtocoloItem;
import com.omni.container.ui.adapters.ProtocoloAdapter;
import com.omni.container.ui.adapters.ProtocoloItemSelecionadoAdapter;
import com.omni.container.ui.states.OrigemItem;
import com.omni.container.ui.states.ProtocoloItemSelecionadoUiState;
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

public class ManejoSanitarioFragment extends Fragment {
    private static final String TAG = "FRAGMENT_XGP_MANEJO_SANITARIO";
    private static final int THREAD_POOL_SIZE = 4;
    private static final double DOSAGEM_INICIAL = 0.0;
    private static final double PESO_AUSENTE = 0.0;
    private static final char STATUS_NAO_APLICADO = 'N';

    // Key Result Listener e Key Bundles (ConsultaProtocoloItemFragment)
    public static final String RESULT_KEY_PROTOCOLO_ITENS_SELECIONADOS = "result_protocolo_itens_selecionados";
    public static final String ARG_KEY_PROTOCOLO_ITENS_SELECIONADOS = "arg_protocolo_itens_selecionados";


    // Key Result Listener e Key Bundles (AnimalFragment)
    public static final String RESULT_KEY_ANIMAL = "result_animal";
    public static final String ARG_KEY_COD_ANIMAL = "arg_cod_animal";
    public static final String ARG_KEY_COD_BOTTOM = "arg_cod_bottom";
    public static final String ARG_KEY_COD_SYS_BOV = "arg_cod_sys_bov";
    public static final String ARG_KEY_PESO = "arg_peso";

    //
    private static final String STATE_KEY_APLICACAO_ITEMS = "state_aplicacao_items";
    private static final String STATE_KEY_PROTOCOLO_SELECIONADO = "state_protocolo_selecionado";

    // Views
    private TextView textViewProtocoloItensSelecionados;
    private TextView textProtocoloNome;
    private EditText editTextPeso;
    private EditText editTextIdentificacao;
    private AutoCompleteTextView autoCompleteTextViewProtocolos;
    private MaterialCardView cardProtocoloItemAvulso;
    private MaterialCardView cardProtocoloSelecionado;
    private LinearLayout cardEmptyState;
    private ImageView imageViewInfo;
    private ImageView imageViewRemover;
    private Button buttonSalvar;
    private RecyclerView recyclerViewProtocoloItensSelecionados;

    // Components
    private Executor executor;
    private ProtocoloAdapter protocoloAdapter;
    private ProtocoloItemSelecionadoAdapter protocoloItemSelecionadoAdapter;

    // State
    private ProtocoloUiState protocoloSelecionado;
    private String codAnimal;
    private String codBottom;
    private String codSysBov;
    private double peso;
    private final List<ProtocoloItemSelecionadoUiState> aplicacaoItems = new ArrayList<>();
    private final List<ProtocoloUiState> protocols = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_xgp_manejo_sanitario, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupExecutor();
        setupViews(view);
        setupAdapters();
        setupListeners();
        setupResultListeners();
        setupInitialState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        saveAplicacaoItemsState(outState);
        saveProtocoloSelecionadoState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        executor.close();
        clearViews();
    }

    // Inicializações e Configurações
    private void setupExecutor() {
        executor = new Executor(createExecutorDeThreads(), createHandlerDaMainThread());
    }

    private void setupViews(@NonNull View view) {
        textViewProtocoloItensSelecionados = view.findViewById(R.id.text_protocolo_itens_selecionados_count);
        editTextIdentificacao = view.findViewById(R.id.edit_identificacao);
        textProtocoloNome = view.findViewById(R.id.text_protocolo_nome);
        editTextPeso = view.findViewById(R.id.edit_peso);
        autoCompleteTextViewProtocolos = view.findViewById(R.id.edit_protocolo);
        cardProtocoloItemAvulso = view.findViewById(R.id.card_protocolo_item_avulso);
        cardProtocoloSelecionado = view.findViewById(R.id.card_protocolo_selecionado);
        cardEmptyState = view.findViewById(R.id.empty_state);
        buttonSalvar = view.findViewById(R.id.btn_salvar);
        imageViewInfo = view.findViewById(R.id.btn_protocolo_info);
        imageViewRemover = view.findViewById(R.id.btn_protocolo_remover);
        recyclerViewProtocoloItensSelecionados = view.findViewById(R.id.recycler_protocolo_itens_selecionados);
    }

    private void setupAdapters() {
        setupProtocoloDropdown();
        setupAplicacaoRecyclerView();
    }

    private void setupProtocoloDropdown() {
        protocoloAdapter = new ProtocoloAdapter(requireContext(), new ArrayList<>());
        autoCompleteTextViewProtocolos.setAdapter(protocoloAdapter);
        autoCompleteTextViewProtocolos.setOnItemClickListener((parent, v, position, id) -> handleProtocoloSelected(position));
    }

    private void setupAplicacaoRecyclerView() {
        protocoloItemSelecionadoAdapter = new ProtocoloItemSelecionadoAdapter(aplicacaoItems, this::handleItemRemoved);
        ViewUtils.setupVerticalRecyclerView(recyclerViewProtocoloItensSelecionados, protocoloItemSelecionadoAdapter, requireContext());
    }

    private void setupListeners() {
        cardProtocoloItemAvulso.setOnClickListener(v -> navigateToConsultaProtocoloItem());
        imageViewInfo.setOnClickListener(v -> showProtocoloInfoDialog());
        imageViewRemover.setOnClickListener(v -> handleProtocoloRemoved());
        buttonSalvar.setOnClickListener(v -> handleSaveAction());
    }

    private void setupResultListeners() {
        setupProtocoloItensResultListener();
        setupAnimalResultListener();
    }

    private void setupProtocoloItensResultListener() {
        getParentFragmentManager().setFragmentResultListener(RESULT_KEY_PROTOCOLO_ITENS_SELECIONADOS, getViewLifecycleOwner(),
                (requestKey, bundle) -> handleProtocoloItensResult(bundle));
    }

    private void setupAnimalResultListener() {
        getParentFragmentManager().setFragmentResultListener(RESULT_KEY_ANIMAL, getViewLifecycleOwner(),
                (requestKey, bundle) -> handleAnimalResult(bundle));
    }

    private void setupInitialState(@Nullable Bundle savedInstanceState) {
        restoreState(savedInstanceState);
        bindCurrentState();
        fetchProtocolosIfNeeded();
    }

    // Vinculação de Dados a Interface
    private void bindProtocolosToAdapter() {
        protocoloAdapter.clear();
        protocoloAdapter.addAll(protocols);
    }

    private void bindProtocoloSelecionadoName(@NonNull String nomeProtocolo) {
        textProtocoloNome.setText(nomeProtocolo);
    }

    private void bindNewItemToAdapter() {
        protocoloItemSelecionadoAdapter.notifyItemInserted(aplicacaoItems.size() - 1);
        showItemsCount();
    }

    private void bindCurrentState() {
        bindProtocolosToAdapter();
        bindProtocoloSelecionado();
        bindAplicacaoItems();
    }

    private void bindProtocoloSelecionado() {
        if (isInvalid(protocoloSelecionado)) return;
        showProtocoloInDropdown(protocoloSelecionado);
        bindProtocoloSelecionadoName(protocoloSelecionado.getDescricao());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void bindAplicacaoItems() {
        protocoloItemSelecionadoAdapter.notifyDataSetChanged();
        showItemsCount();
        updateViewsVisibility();
    }

    private void bindAnimalHints() {
        ViewUtils.setHint(editTextIdentificacao, getAnimalIdentification());
        ViewUtils.setHint(editTextPeso, getFormattedPeso());
    }

    // Exibição de Transições e Contadores
    private void showItemsCount() {
        ViewUtils.formatTextoPlural(textViewProtocoloItensSelecionados, requireContext(), R.plurals.protocolo_itens_selecionados_count, aplicacaoItems.size());
    }

    private void showProtocoloInfoDialog() {
        new InfoAplicacaoFragment().show(getParentFragmentManager(), InfoAplicacaoFragment.TAG);
    }

    private void showProtocoloInDropdown(@NonNull ProtocoloUiState protocolo) {
        autoCompleteTextViewProtocolos.setText(protocolo.getDescricao().trim(), false);
    }

    private void showErrorSnackBar(@NonNull String message) {
        View view = requireView();
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(view.getContext(), android.R.color.holo_red_dark))
                .setTextColor(Color.WHITE)
                .show();
    }

    // Processamento de Eventos e Callbacks
    private void handleProtocoloSelected(int position) {
        ProtocoloUiState protocolo = getProtocoloAtPosition(position);
        if (isInvalid(protocolo)) return;
        updateProtocoloSelecionado(protocolo);
        fetchItemsForProtocoloFromDb(protocolo);
    }

    private void handleSaveAction() {
        if (isPesoEmpty()) return;
        // Lógica de salvar
    }

    @SuppressLint("NotifyDataSetChanged")
    private void handleProtocoloRemoved() {
        if (!isProtocoloSelected()) return;
        removeProtocoloItems();
        clearProtocoloSelecionado();
        protocoloItemSelecionadoAdapter.notifyDataSetChanged();
        showItemsCount();
        updateViewsVisibility();
    }

    private void handleProtocoloItensResult(@NonNull Bundle bundle) {
        List<ProtocoloItem> itens = getProtocoloItemsFromBundle(bundle);
        if (isEmptyList(itens)) return;
        for (ProtocoloItem item : itens) {
            addProtocoloItemAvulso(Mapper.fromProtocoloItemToUiStateAvulso(item));
        }
        updateViewsVisibility();
    }

    private void handleItemRemoved(@NonNull ProtocoloItemSelecionadoUiState item, int position) {
        showItemsCount();
        updateViewsVisibility();
    }

    private void handleAnimalResult(@NonNull Bundle bundle) {
        if (!isValidAnimal(bundle)) return;
        updateAnimalData(bundle);
    }

    private void handleProtocolosFetchSuccess(@NonNull List<Protocolo> protocolos, @NonNull List<ProtocoloItem> itens) {
        protocols.clear();
        protocols.addAll(Mapper.fromProtocolosToUiStateList(protocolos, itens));
        bindProtocolosToAdapter();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void handleProtocoloItemsFetchSuccess(@NonNull List<ProtocoloItem> protocoloItems) {
        removeProtocoloItems();
        aplicacaoItems.addAll(Mapper.fromItensToUiStateAplicacaoList(protocoloItems));
        protocoloItemSelecionadoAdapter.notifyDataSetChanged();
        showItemsCount();
        updateViewsVisibility();
    }

    private void handleProtocolosFetchError(@NonNull Throwable throwable) {
        showErrorSnackBar(getString(R.string.erro_carregar_protocolos));
        Log.d(TAG, getString(R.string.erro_carregar_protocolos) + throwable.getMessage());
    }

    private void handleProtocoloItemsFetchError(@NonNull Throwable throwable) {
        showErrorSnackBar(getString(R.string.erro_carregar_protocolo_itens));
        Log.d(TAG, getString(R.string.erro_carregar_protocolo_itens) + throwable.getMessage());
    }


    private void removeProtocoloItems() {
        aplicacaoItems.removeIf(item -> item.getOrigem() == PROTOCOLO);
    }

    private void updateViewsVisibility() {
        boolean hasItems = !aplicacaoItems.isEmpty();
        boolean hasProtocolo = isProtocoloSelected();
        ViewUtils.setVisible(!hasItems, cardEmptyState);
        ViewUtils.setVisible(hasItems, recyclerViewProtocoloItensSelecionados);
        ViewUtils.setVisible(hasProtocolo, cardProtocoloSelecionado);
    }

    private void updateProtocoloSelecionado(@NonNull ProtocoloUiState protocolo) {
        protocoloSelecionado = protocolo;
        bindProtocoloSelecionado();
        updateViewsVisibility();
    }

    private void updateAnimalData(@NonNull Bundle bundle) {
        codAnimal = getCodAnimalFromBundle(bundle);
        codBottom = getCodBottomFromBundle(bundle);
        codSysBov = getCodSysBovFromBundle(bundle);
        peso = getPesoFromBundle(bundle);
        bindAnimalHints();
    }

    private void clearProtocoloSelecionado() {
        protocoloSelecionado = null;
        autoCompleteTextViewProtocolos.setText(getString(R.string.hint_protocolo_vacinacao), false);
        textProtocoloNome.setText(R.string.protocolo_nome_vazio);
    }

    private void addProtocoloItemAvulso(@NonNull ProtocoloItemSelecionadoUiState state) {
        aplicacaoItems.add(state);
        bindNewItemToAdapter();
    }

    private void navigateToConsultaProtocoloItem() {
        getParentFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .replace(R.id.fragment_container_view, new ConsultaProtocoloItemFragment())
                .commit();
    }

    private void clearViews() {
        executor = null;
        textViewProtocoloItensSelecionados = null;
        textProtocoloNome = null;
        editTextPeso = null;
        autoCompleteTextViewProtocolos = null;
        cardProtocoloItemAvulso = null;
        cardProtocoloSelecionado = null;
        cardEmptyState = null;
        buttonSalvar = null;
        imageViewInfo = null;
        imageViewRemover = null;
        recyclerViewProtocoloItensSelecionados = null;
        protocoloAdapter = null;
        protocoloItemSelecionadoAdapter = null;
        editTextIdentificacao = null;
    }

    // Buscas Assíncronas de Dados
    private void fetchProtocolosIfNeeded() {
        if (hasLoadedProtocolos()) return;
        fetchProtocolosForDropdown();
    }

    private void fetchProtocolosForDropdown() {
        fetchProtocolosFromDb(protocolos -> fetchAllProtocoloItemsFromDb(itens ->
                handleProtocolosFetchSuccess(protocolos, itens)));
    }

    private void fetchProtocolosFromDb(@NonNull Consumer<List<Protocolo>> onSuccess) {
        executor.execute(requireContext(), AppDatabase::protocoloDao, ProtocoloDao::getAll, onSuccess, this::handleProtocolosFetchError);
    }

    private void fetchAllProtocoloItemsFromDb(@NonNull Consumer<List<ProtocoloItem>> onSuccess) {
        executor.execute(requireContext(), AppDatabase::protocoloItemDao, ProtocoloItemDao::getAll, onSuccess, this::handleProtocoloItemsFetchError);
    }

    private void fetchItemsForProtocoloFromDb(@NonNull ProtocoloUiState protocolo) {
        executor.execute(requireContext(), AppDatabase::protocoloItemDao, ProtocoloItemDao::getAllByIdProtocolo, protocolo.getId(),
                this::handleProtocoloItemsFetchSuccess, this::handleProtocoloItemsFetchError);
    }


    // Recuperação Síncrona de Dados
    @Nullable
    private ProtocoloUiState getProtocoloAtPosition(int position) {
        if (isInvalidPosition(position)) return null;
        return protocoloAdapter.getItem(position);
    }

    @Nullable
    private List<ProtocoloItem> getProtocoloItemsFromBundle(@NonNull Bundle bundle) {
        return BundleCompat.getParcelableArrayList(bundle, ARG_KEY_PROTOCOLO_ITENS_SELECIONADOS, ProtocoloItem.class);
    }

    @NonNull
    private String getAnimalIdentification() {
        if (hasText(codAnimal)) return codAnimal;
        if (hasText(codBottom)) return codBottom;
        if (hasText(codSysBov)) return codSysBov;
        return "";
    }

    @NonNull
    private String getFormattedPeso() {
        return String.valueOf(peso);
    }

    @Nullable
    private String getCodAnimalFromBundle(@NonNull Bundle bundle) {
        return bundle.getString(ARG_KEY_COD_ANIMAL);
    }

    @Nullable
    private String getCodBottomFromBundle(@NonNull Bundle bundle) {
        return bundle.getString(ARG_KEY_COD_BOTTOM);
    }

    @Nullable
    private String getCodSysBovFromBundle(@NonNull Bundle bundle) {
        return bundle.getString(ARG_KEY_COD_SYS_BOV);
    }

    private double getPesoFromBundle(@NonNull Bundle bundle) {
        return bundle.getDouble(ARG_KEY_PESO, PESO_AUSENTE);
    }

    // Validações e Booleanos
    private boolean isProtocoloSelected() {
        return protocoloSelecionado != null;
    }

    private boolean isPesoEmpty() {
        return editTextPeso == null || editTextPeso.getText() == null || editTextPeso.getText().toString().trim().isEmpty();
    }

    private boolean isInvalid(@Nullable Object value) {
        return value == null;
    }

    private boolean isEmptyList(@Nullable List<?> lista) {
        return lista == null || lista.isEmpty();
    }

    private boolean isInvalidPosition(int position) {
        return position == RecyclerView.NO_POSITION;
    }

    private boolean isValidAnimal(@NonNull Bundle bundle) {
        return hasAnyAnimalCode(bundle) && hasPeso(bundle);
    }

    private boolean hasAnyAnimalCode(@NonNull Bundle bundle) {
        return hasText(bundle, ARG_KEY_COD_ANIMAL)
                || hasText(bundle, ARG_KEY_COD_BOTTOM)
                || hasText(bundle, ARG_KEY_COD_SYS_BOV);
    }

    private boolean hasPeso(@NonNull Bundle bundle) {
        return getPesoFromBundle(bundle) > PESO_AUSENTE;
    }

    private boolean hasText(@NonNull Bundle bundle, @NonNull String key) {
        return hasText(bundle.getString(key));
    }

    private boolean hasText(@Nullable String value) {
        return value != null && !value.trim().isEmpty();
    }
    private boolean hasLoadedProtocolos() {
        return !protocols.isEmpty();
    }


    // Salvamento de estado e restauracao
    private void saveAplicacaoItemsState(@NonNull Bundle outState) {
        outState.putParcelableArrayList(STATE_KEY_APLICACAO_ITEMS, new ArrayList<>(aplicacaoItems));
    }

    private void saveProtocoloSelecionadoState(@NonNull Bundle outState) {
        outState.putParcelable(STATE_KEY_PROTOCOLO_SELECIONADO, protocoloSelecionado);
    }

    private void restoreState(@Nullable Bundle savedInstanceState) {
        if (isInvalid(savedInstanceState)) return;
        protocoloSelecionado = BundleCompat.getParcelable(savedInstanceState, STATE_KEY_PROTOCOLO_SELECIONADO, ProtocoloUiState.class);
        restoreAplicacaoItemsState(savedInstanceState);
    }

    private void restoreAplicacaoItemsState(@NonNull Bundle savedInstanceState) {
        List<ProtocoloItemSelecionadoUiState> items = BundleCompat
                .getParcelableArrayList(savedInstanceState, STATE_KEY_APLICACAO_ITEMS, ProtocoloItemSelecionadoUiState.class);
        if (isEmptyList(items)) return;
        aplicacaoItems.clear();
        aplicacaoItems.addAll(items);
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

        static void formatTextoPlural(@NonNull TextView view, @NonNull Context context, @PluralsRes int resId, @Nullable Integer quantity) {
            if (quantity == null) {
                view.setText("");
                return;
            }
            view.setText(context.getResources().getQuantityString(resId, quantity, quantity));
        }

        static void setHint(@Nullable TextView view, @Nullable CharSequence hint) {
            if (view == null) return;
            view.setHint(hint);
        }

        static void setVisible(boolean visible, @NonNull View... views) {
            int state = visible ? View.VISIBLE : View.GONE;
            for (View v : views) {
                if (v != null) v.setVisibility(state);
            }
        }
    }

    private static final class Mapper {
        static List<ProtocoloUiState> fromProtocolosToUiStateList(@NonNull List<Protocolo> protocolos, @NonNull List<ProtocoloItem> itens) {
            Map<Integer, Integer> contagemPorProtocolo = countItensPorProtocolo(itens);
            return protocolos.stream()
                    .map(protocolo -> fromProtocoloToUiState(protocolo, getQuantidadeDeItens(contagemPorProtocolo, protocolo)))
                    .collect(Collectors.toList());
        }

        static List<ProtocoloItemSelecionadoUiState> fromItensToUiStateAplicacaoList(@NonNull List<ProtocoloItem> itens) {
            return itens.stream()
                    .map(Mapper::fromItemToUiStateAplicacao)
                    .collect(Collectors.toList());
        }

        private static Map<Integer, Integer> countItensPorProtocolo(@NonNull List<ProtocoloItem> itens) {
            return itens.stream()
                    .collect(Collectors.groupingBy(ProtocoloItem::getIdProtocolo, Collectors.summingInt(item -> 1)));
        }

        private static int getQuantidadeDeItens(@NonNull Map<Integer, Integer> contagem, @NonNull Protocolo protocolo) {
            Integer quantidade = contagem.get(protocolo.getIdProtocolo());
            return quantidade == null ? 0 : quantidade;
        }

        private static ProtocoloUiState fromProtocoloToUiState(@NonNull Protocolo protocolo, int quantidadeItems) {
            return new ProtocoloUiState(protocolo.getIdProtocolo(), protocolo.getDescricao(), quantidadeItems, protocolo.getAplicacao(), new Date());
        }

        private static ProtocoloItemSelecionadoUiState fromItemToUiStateAplicacao(@NonNull ProtocoloItem item) {
            return new ProtocoloItemSelecionadoUiState(item.getDescricao(), PROTOCOLO, DOSAGEM_INICIAL, STATUS_NAO_APLICADO);
        }

        static ProtocoloItemSelecionadoUiState fromProtocoloItemToUiStateAvulso(@NonNull ProtocoloItem item) {
            return new ProtocoloItemSelecionadoUiState(item.getDescricao(), OrigemItem.AVULSO, DOSAGEM_INICIAL, STATUS_NAO_APLICADO);
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

        <D, E> void execute(@NonNull Context context, @NonNull Function<AppDatabase, D> daoExtractor,
                            @NonNull Function<D, E> query, @NonNull Consumer<E> onSuccess, @NonNull Consumer<Exception> onError) {
            submit(() -> query.apply(resolveDao(context, daoExtractor)), onSuccess, onError);
        }

        <D, P, E> void execute(@NonNull Context context, @NonNull Function<AppDatabase, D> daoExtractor,
                               @NonNull BiFunction<D, P, E> query, @NonNull P param, @NonNull Consumer<E> onSuccess, @NonNull Consumer<Exception> onError) {
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
