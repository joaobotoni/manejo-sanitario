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

    public static final String RESULT_KEY_PROTOCOLO_ITENS_SELECIONADOS = "result_protocolo_itens_selecionados";
    public static final String ARG_KEY_PROTOCOLO_ITENS_SELECIONADOS = "arg_protocolo_itens_selecionados";
    public static final String RESULT_KEY_ANIMAL = "result_animal";
    public static final String ARG_KEY_COD_ANIMAL = "arg_cod_animal";
    public static final String ARG_KEY_COD_BOTTOM = "arg_cod_bottom";
    public static final String ARG_KEY_COD_SYS_BOV = "arg_cod_sys_bov";
    public static final String ARG_KEY_PESO = "arg_peso";
    private static final String STATE_KEY_APLICACAO_ITEMS = "state_aplicacao_items";
    private static final String STATE_KEY_PROTOCOLO_SELECIONADO = "state_protocolo_selecionado";

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
    private Executor executor;
    private ProtocoloAdapter protocoloAdapter;
    private ProtocoloUiState protocoloSelecionado;
    private ProtocoloItemSelecionadoAdapter protocoloItemSelecionadoAdapter;
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
        createExecutor();
        bindViews(view);
        setupAdapters();
        setupClickListeners();
        setupFragmentResultListener();
        initState(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        saveAplicacaoItems(outState);
        saveProtocoloSelecionado(outState);
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

    private void setupAdapters() {
        setupProtocoloDropdown();
        setupAplicacaoRecyclerView();
    }

    private void setupProtocoloDropdown() {
        protocoloAdapter = new ProtocoloAdapter(requireContext(), new ArrayList<>());
        autoCompleteTextViewProtocolos.setAdapter(protocoloAdapter);
        autoCompleteTextViewProtocolos.setOnItemClickListener((parent, v, position, id) -> configureSelecaoDeProtocolo(position));
    }

    private void bindProtocolosNoAdapter() {
        protocoloAdapter.clear();
        protocoloAdapter.addAll(protocols);
    }

    private void setupFragmentResultListener() {
        setupProtocoloItensResultListener();
        setupAnimalResultListener();
    }

    private void setupProtocoloItensResultListener() {
        getParentFragmentManager().setFragmentResultListener(RESULT_KEY_PROTOCOLO_ITENS_SELECIONADOS, getViewLifecycleOwner(),
                (requestKey, bundle) -> handleProtocoloItensRecebidos(bundle));
    }

    private void setupAnimalResultListener() {
        getParentFragmentManager().setFragmentResultListener(RESULT_KEY_ANIMAL, getViewLifecycleOwner(),
                (requestKey, bundle) -> handleAnimalRecebido(bundle));
    }

    private void setupAplicacaoRecyclerView() {
        protocoloItemSelecionadoAdapter = new ProtocoloItemSelecionadoAdapter(aplicacaoItems, this::handleItemRemovido);
        setupVerticalRecyclerView(recyclerViewProtocoloItensSelecionados, protocoloItemSelecionadoAdapter, requireContext());
    }

    private void setupClickListeners() {
        cardProtocoloItemAvulso.setOnClickListener(v -> navegarParaConsultaProtocoloItem());
        imageViewInfo.setOnClickListener(v -> showInfoAplicacaoProtocolo());
        imageViewRemover.setOnClickListener(v -> configureRemocaoDeProtocolo());
        buttonSalvar.setOnClickListener(v -> configureAcaoDeSalvar());
    }

    private void bindViews(@NonNull View view) {
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

    public static void setupVerticalRecyclerView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.Adapter<?> adapter, @NonNull Context context) {
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
    }

    private void bindTextCardProtocoloSelecionado(@NonNull String nomeProtocolo) {
        textProtocoloNome.setText(nomeProtocolo);
    }

    private void bindAdicaoDeItemNoAdapter() {
        protocoloItemSelecionadoAdapter.notifyItemInserted(aplicacaoItems.size() - 1);
        showContadorItems();
    }

    private void bindRemocaoDeItemNoAdapter(int index) {
        protocoloItemSelecionadoAdapter.notifyItemRemoved(index);
        showContadorItems();
    }

    private void showContadorItems() {
        formatTextoPlural(textViewProtocoloItensSelecionados, requireContext(), R.plurals.protocolo_itens_selecionados_count, aplicacaoItems.size());
    }

    private void showInfoAplicacaoProtocolo() {
        new InfoAplicacaoFragment().show(getParentFragmentManager(), InfoAplicacaoFragment.TAG);
    }

    private void showProtocoloNoDropdown(@NonNull ProtocoloUiState protocolo) {
        autoCompleteTextViewProtocolos.setText(protocolo.getDescricao().trim(), false);
    }

    private void showSnackBarErro(@NonNull String message) {
        View view = requireView();
        Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(view.getContext(), android.R.color.holo_red_dark))
                .setTextColor(Color.WHITE)
                .show();
    }

    private void showNovosProtocolosDropdown(@NonNull List<ProtocoloUiState> novosProtocolos) {
        protocols.clear();
        protocols.addAll(novosProtocolos);
        bindProtocolosNoAdapter();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showNovosItemsSelecionados(@NonNull List<ProtocoloItemSelecionadoUiState> novosItems) {
        removeProtocoloItems();
        aplicacaoItems.addAll(novosItems);
        protocoloItemSelecionadoAdapter.notifyDataSetChanged();
        showContadorItems();
        applyVisibilidadeDeItens();
    }

    private void removeProtocoloItems() {
        aplicacaoItems.removeIf(item -> item.getOrigem() == PROTOCOLO);
    }

    private void applyVisibilidadeDeItens() {
        boolean hasItems = !aplicacaoItems.isEmpty();
        boolean hasProtocolo = isProtocoloSelecionado();
        setVisible(!hasItems, cardEmptyState);
        setVisible(hasItems, recyclerViewProtocoloItensSelecionados);
        setVisible(hasProtocolo, cardProtocoloSelecionado);
    }

    private boolean isProtocoloSelecionado() {
        return protocoloSelecionado != null;
    }

    private void navegarParaConsultaProtocoloItem() {
        getParentFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .replace(R.id.fragment_container_view, new ConsultaProtocoloItemFragment())
                .commit();
    }

    private void configureSelecaoDeProtocolo(int position) {
        ProtocoloUiState protocolo = getProtocoloNaPosicao(position);
        if (isInvalido(protocolo)) return;
        updateProtocoloSelecionado(protocolo);
        fetchItemsDoProtocolo(protocolo);
    }

    private void updateProtocoloSelecionado(@NonNull ProtocoloUiState protocolo) {
        protocoloSelecionado = protocolo;
        bindProtocoloSelecionado();
        applyVisibilidadeDeItens();
    }

    private void configureAcaoDeSalvar() {
        if (isPesoVazio()) return;
    }

    private void configureRemocaoDeProtocolo() {
        if (!isProtocoloSelecionado()) return;
        removeProtocoloItems();
        clearSelecaoDeProtocolo();
        protocoloItemSelecionadoAdapter.notifyDataSetChanged();
        showContadorItems();
        applyVisibilidadeDeItens();
    }

    private void attachProtocoloItemSelecionadoAvulso(@NonNull ProtocoloItemSelecionadoUiState state) {
        aplicacaoItems.add(state);
        bindAdicaoDeItemNoAdapter();
    }

    private void attachRemocaoDeProtocoloItemSelecionadoAvulso(@NonNull ProtocoloItemSelecionadoUiState state) {
        int index = aplicacaoItems.indexOf(state);
        if (isPosicaoInvalida(index)) return;
        aplicacaoItems.remove(index);
        bindRemocaoDeItemNoAdapter(index);
    }

    private void handleProtocoloItensRecebidos(@NonNull Bundle bundle) {
        List<ProtocoloItem> itens = getProtocoloItensSelecionadosDoBundle(bundle);
        if (isListaVazia(itens)) return;
        for (ProtocoloItem item : itens) {
            attachProtocoloItemSelecionadoAvulso(Mapper.fromProtocoloItemToUiStateAvulso(item));
        }
        applyVisibilidadeDeItens();
    }

    private void handleItemRemovido(@NonNull ProtocoloItemSelecionadoUiState item, int position) {
        showContadorItems();
        applyVisibilidadeDeItens();
    }

    @Nullable
    private List<ProtocoloItem> getProtocoloItensSelecionadosDoBundle(@NonNull Bundle bundle) {
        return BundleCompat.getParcelableArrayList(bundle, ARG_KEY_PROTOCOLO_ITENS_SELECIONADOS, ProtocoloItem.class);
    }

    private void handleAnimalRecebido(@NonNull Bundle bundle) {
        if (!isAnimalValido(bundle)) return;
        updateAnimalIdentificado(bundle);
    }

    private void updateAnimalIdentificado(@NonNull Bundle bundle) {
        codAnimal = getCodAnimal(bundle);
        codBottom = getCodBottom(bundle);
        codSysBov = getCodSysBov(bundle);
        peso = getPeso(bundle);
        bindHintsDoAnimal();
    }

    private void bindHintsDoAnimal() {
        setHint(editTextIdentificacao, getIdentificacaoAnimal());
        setHint(editTextPeso, getPesoFormatado());
    }

    @NonNull
    private String getIdentificacaoAnimal() {
        if (hasTexto(codAnimal)) return codAnimal;
        if (hasTexto(codBottom)) return codBottom;
        if (hasTexto(codSysBov)) return codSysBov;
        return "";
    }

    @NonNull
    private String getPesoFormatado() {
        return String.valueOf(peso);
    }

    private boolean isAnimalValido(@NonNull Bundle bundle) {
        return hasAlgumCodigoAnimal(bundle) && hasPeso(bundle);
    }

    private boolean hasAlgumCodigoAnimal(@NonNull Bundle bundle) {
        return hasTexto(bundle, ARG_KEY_COD_ANIMAL)
                || hasTexto(bundle, ARG_KEY_COD_BOTTOM)
                || hasTexto(bundle, ARG_KEY_COD_SYS_BOV);
    }

    private boolean hasPeso(@NonNull Bundle bundle) {
        return getPeso(bundle) > PESO_AUSENTE;
    }

    private boolean hasTexto(@NonNull Bundle bundle, @NonNull String key) {
        return hasTexto(bundle.getString(key));
    }

    private boolean hasTexto(@Nullable String value) {
        return value != null && !value.trim().isEmpty();
    }

    @Nullable
    private String getCodAnimal(@NonNull Bundle bundle) {
        return bundle.getString(ARG_KEY_COD_ANIMAL);
    }

    @Nullable
    private String getCodBottom(@NonNull Bundle bundle) {
        return bundle.getString(ARG_KEY_COD_BOTTOM);
    }

    @Nullable
    private String getCodSysBov(@NonNull Bundle bundle) {
        return bundle.getString(ARG_KEY_COD_SYS_BOV);
    }

    private double getPeso(@NonNull Bundle bundle) {
        return bundle.getDouble(ARG_KEY_PESO, PESO_AUSENTE);
    }

    private void clearSelecaoDeProtocolo() {
        protocoloSelecionado = null;
        autoCompleteTextViewProtocolos.setText(getString(R.string.hint_protocolo_vacinacao), false);
        textProtocoloNome.setText(R.string.protocolo_nome_vazio);
    }

    private void releaseViews() {
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

    private void saveAplicacaoItems(@NonNull Bundle outState) {
        outState.putParcelableArrayList(STATE_KEY_APLICACAO_ITEMS, new ArrayList<>(aplicacaoItems));
    }

    private void saveProtocoloSelecionado(@NonNull Bundle outState) {
        outState.putParcelable(STATE_KEY_PROTOCOLO_SELECIONADO, protocoloSelecionado);
    }

    private void initState(@Nullable Bundle savedInstanceState) {
        restoreInstanceState(savedInstanceState);
        bindEstadoAtual();
        fetchProtocolosSeNecessario();
    }

    private void restoreInstanceState(@Nullable Bundle savedInstanceState) {
        if (isInvalido(savedInstanceState)) return;
        protocoloSelecionado = BundleCompat.getParcelable(savedInstanceState, STATE_KEY_PROTOCOLO_SELECIONADO, ProtocoloUiState.class);
        restoreAplicacaoItens(savedInstanceState);
    }

    private void restoreAplicacaoItens(@NonNull Bundle savedInstanceState) {
        List<ProtocoloItemSelecionadoUiState> items = BundleCompat.getParcelableArrayList(savedInstanceState, STATE_KEY_APLICACAO_ITEMS, ProtocoloItemSelecionadoUiState.class);
        if (isListaVazia(items)) return;
        aplicacaoItems.clear();
        aplicacaoItems.addAll(items);
    }

    private void bindEstadoAtual() {
        bindProtocolosNoAdapter();
        bindProtocoloSelecionado();
        bindItensDeAplicacao();
    }

    private void bindProtocoloSelecionado() {
        if (isInvalido(protocoloSelecionado)) return;
        showProtocoloNoDropdown(protocoloSelecionado);
        bindTextCardProtocoloSelecionado(protocoloSelecionado.getDescricao());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void bindItensDeAplicacao() {
        protocoloItemSelecionadoAdapter.notifyDataSetChanged();
        showContadorItems();
        applyVisibilidadeDeItens();
    }

    private void fetchProtocolosSeNecessario() {
        if (hasProtocolosCarregados()) return;
        fetchProtocolosParaDropdown();
    }

    private boolean hasProtocolosCarregados() {
        return !protocols.isEmpty();
    }

    private void fetchProtocolosParaDropdown() {
        fetchProtocolosNoBanco(protocolos -> fetchAllItemsDosProtocolosNoBanco(itens -> handleProtocolosEncontrados(protocolos, itens)));
    }

    private void fetchProtocolosNoBanco(@NonNull Consumer<List<Protocolo>> onSuccess) {
        executor.execute(requireContext(), AppDatabase::protocoloDao, ProtocoloDao::getAll, onSuccess, this::handleErroAoBuscarProtocolos);
    }

    private void fetchAllItemsDosProtocolosNoBanco(@NonNull Consumer<List<ProtocoloItem>> onSuccess) {
        executor.execute(requireContext(), AppDatabase::protocoloItemDao, ProtocoloItemDao::getAll, onSuccess, this::handleErroAoBuscarProtocoloItens);
    }

    private void fetchItemsDoProtocolo(@NonNull ProtocoloUiState protocolo) {
        executor.execute(requireContext(), AppDatabase::protocoloItemDao, ProtocoloItemDao::getAllByIdProtocolo, protocolo.getId(), this::handleItemsDoProtocoloEncontrados, this::handleErroAoBuscarProtocoloItens);
    }

    private void handleProtocolosEncontrados(@NonNull List<Protocolo> protocolos, @NonNull List<ProtocoloItem> itens) {
        showNovosProtocolosDropdown(Mapper.fromProtocolosToUiStateList(protocolos, itens));
    }

    private void handleItemsDoProtocoloEncontrados(@NonNull List<ProtocoloItem> protocoloItems) {
        showNovosItemsSelecionados(Mapper.fromItensToUiStateAplicacaoList(protocoloItems));
    }

    private void handleErroAoBuscarProtocolos(@NonNull Throwable throwable) {
        showSnackBarErro(getString(R.string.erro_carregar_protocolos));
        Log.d(TAG, getString(R.string.erro_carregar_protocolos) + throwable.getMessage());
    }

    private void handleErroAoBuscarProtocoloItens(@NonNull Throwable throwable) {
        showSnackBarErro(getString(R.string.erro_carregar_protocolo_itens));
        Log.d(TAG, getString(R.string.erro_carregar_protocolo_itens) + throwable.getMessage());
    }

    @Nullable
    private ProtocoloUiState getProtocoloNaPosicao(int position) {
        if (isPosicaoInvalida(position)) return null;
        return protocoloAdapter.getItem(position);
    }

    private boolean isInvalido(@Nullable Object value) {
        return value == null;
    }

    private boolean isListaVazia(@Nullable List<?> lista) {
        return lista == null || lista.isEmpty();
    }

    private boolean isPosicaoInvalida(int position) {
        return position == RecyclerView.NO_POSITION;
    }

    private boolean isPesoVazio() {
        return editTextPeso == null || editTextPeso.getText() == null || editTextPeso.getText().toString().trim().isEmpty();
    }

    public static void formatTextoPlural(@NonNull TextView view, @NonNull Context context, @PluralsRes int resId, @Nullable Integer quantity) {
        if (quantity == null) {
            view.setText("");
            return;
        }
        view.setText(context.getResources().getQuantityString(resId, quantity, quantity));
    }

    public static void setHint(@Nullable TextView view, @Nullable CharSequence hint) {
        if (view == null) return;
        view.setHint(hint);
    }

    public static void setVisible(boolean visible, @NonNull View... views) {
        int state = toVisibilityState(visible);
        for (View v : views) applyVisibility(v, state);
    }

    private static int toVisibilityState(boolean visible) {
        return visible ? View.VISIBLE : View.GONE;
    }

    private static void applyVisibility(@Nullable View view, int state) {
        if (view != null) view.setVisibility(state);
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