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
import com.omni.container.ui.adapters.ProtocoloItemAplicacaoAdapter;
import com.omni.container.ui.states.ProtocoloItemAplicacaoUiState;
import com.omni.container.ui.states.ProtocoloUiState;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ManejoSanitarioFragment extends Fragment {
    private static final String TAG = "FRAGMENT_XGP_MANEJO_SANITARIO";
    private static final int THREAD_POOL_SIZE = 4;
    private static final double DOSAGEM_INICIAL = 0.0;
    private static final char STATUS_NAO_APLICADO = 'N';
    private TextView textViewMedicamentos;
    private TextView textProtocoloNome;
    private EditText editTextPeso;
    private AutoCompleteTextView autoCompleteTextViewProtocolos;
    private MaterialCardView cardMedicamentoAvulso;
    private MaterialCardView cardProtocoloSelecionado;
    private LinearLayout cardEmptyState;
    private ImageView imageViewInfo;
    private ImageView imageViewRemover;
    private Button buttonSalvar;
    private RecyclerView recyclerMedicamentos;
    private Executor executor;
    private ProtocoloAdapter protocoloAdapter;
    private ProtocoloItemAplicacaoAdapter protocoloItemAplicacaoAdapter;
    private final List<ProtocoloItemAplicacaoUiState> aplicacaoItems = new ArrayList<>();
    private final List<ProtocoloUiState> protocolos = new ArrayList<>();

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
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
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
        findByProtocolosParaDropdown();
    }

    private void setupAplicacaoRecyclerView() {
        aplicacaoItems.clear();
        protocoloItemAplicacaoAdapter = new ProtocoloItemAplicacaoAdapter(aplicacaoItems);
        setupVerticalRecyclerView(recyclerMedicamentos, protocoloItemAplicacaoAdapter, requireContext());
        showContadorMedicamentos();
    }

    private void setupClickListeners() {
        cardMedicamentoAvulso.setOnClickListener(v -> navegarParaConsultaMedicamento());
        imageViewInfo.setOnClickListener(v -> showInfoAplicacaoMedicamento());
        imageViewRemover.setOnClickListener(v -> configureRemocaoDeProtocolo());
        buttonSalvar.setOnClickListener(v -> configureAcaoDeSalvar());
    }

    private void bindViews(@NonNull View view) {
        textViewMedicamentos = view.findViewById(R.id.text_medicamentos_count);
        textProtocoloNome = view.findViewById(R.id.text_protocolo_nome);
        editTextPeso = view.findViewById(R.id.edit_peso);
        autoCompleteTextViewProtocolos = view.findViewById(R.id.edit_protocolo);
        cardMedicamentoAvulso = view.findViewById(R.id.card_medicamento_avulso);
        cardProtocoloSelecionado = view.findViewById(R.id.card_protocolo_selecionado);
        cardEmptyState = view.findViewById(R.id.empty_state);
        buttonSalvar = view.findViewById(R.id.btn_salvar);
        imageViewInfo = view.findViewById(R.id.btn_protocolo_info);
        imageViewRemover = view.findViewById(R.id.btn_protocolo_remover);
        recyclerMedicamentos = view.findViewById(R.id.recycler_medicamentos);
    }

    public static void setupVerticalRecyclerView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.Adapter<?> adapter, @NonNull Context context) {
        setupRecyclerView(recyclerView, adapter, context);
    }

    private static void setupRecyclerView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.Adapter<?> adapter, @NonNull Context context) {
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);
    }

    private void bindTextCardProtocoloSelecionado(@NonNull String nomeProtocolo) {
        textProtocoloNome.setText(nomeProtocolo);
    }

    private void bindAdicaoDeItemNoAdapter() {
        protocoloItemAplicacaoAdapter.notifyItemInserted(aplicacaoItems.size() - 1);
        showContadorMedicamentos();
    }

    private void bindRemocaoDeItemNoAdapter(int index) {
        protocoloItemAplicacaoAdapter.notifyItemRemoved(index);
        showContadorMedicamentos();
    }

    private void showContadorMedicamentos() {
        formatTextoPlural(textViewMedicamentos, requireContext(), R.plurals.medicamentos_count, aplicacaoItems.size());
    }

    private void showInfoAplicacaoMedicamento() {
        new InfoAplicacaoFragment().show(getParentFragmentManager(), InfoAplicacaoFragment.TAG);
    }

    private void showProtocoloNoDropdown(@NonNull ProtocoloUiState protocolo) {
        autoCompleteTextViewProtocolos.setText(protocolo.getDescricao().trim(), false);
    }

    private void showSnackBarErro(@NonNull String message) {
        View view = requireView();
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).setBackgroundTint(ContextCompat.getColor(view.getContext(), android.R.color.holo_red_dark)).setTextColor(Color.WHITE).show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showNovosProtocolosDropdown(@NonNull List<ProtocoloUiState> novosProtocolos) {
        protocolos.clear();
        protocolos.addAll(novosProtocolos);
        protocoloAdapter.notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showNovosItemsDeAplicacao(@NonNull List<ProtocoloItemAplicacaoUiState> novosItems) {
        aplicacaoItems.clear();
        aplicacaoItems.addAll(novosItems);
        protocoloItemAplicacaoAdapter.notifyDataSetChanged();
        showContadorMedicamentos();
        applyVisibilidadeDeItens();
    }

    private void applyVisibilidadeDeItens() {
        boolean hasItems = !aplicacaoItems.isEmpty();
        setVisible(!hasItems, cardEmptyState);
        setVisible(hasItems, recyclerMedicamentos);
        setVisible(hasItems, cardProtocoloSelecionado);
    }

    private void navegarParaConsultaMedicamento() {
        getParentFragmentManager().beginTransaction().setReorderingAllowed(true).addToBackStack(null).replace(R.id.fragment_container_view, new ConsultaMedicamentoFragment()).commit();
    }

    private void configureSelecaoDeProtocolo(int position) {
        ProtocoloUiState protocolo = findByPosicaoNoAdapter(position);
        if (isInvalido(protocolo)) return;
        showProtocoloNoDropdown(protocolo);
        bindTextCardProtocoloSelecionado(protocolo.getDescricao());
        findByItemsDoProtocolo(protocolo);
    }

    private void configureAcaoDeSalvar() {
        if (isPesoVazio()) return;
        // TODO: persistir
    }

    private void configureRemocaoDeProtocolo() {
        if (!existeItemDeProtocolo()) return;
        limparItemsDeAplicacao();
        limparSelecaoDeProtocolo();
        applyVisibilidadeDeItens();
    }

    private void attachMedicamentoAvulso(@NonNull ProtocoloItemAplicacaoUiState state) {
        aplicacaoItems.add(state);
        bindAdicaoDeItemNoAdapter();
    }

    private void attachRemocaoDeMedicamentoAvulso(@NonNull ProtocoloItemAplicacaoUiState state) {
        int index = aplicacaoItems.indexOf(state);
        if (isPosicaoInvalida(index)) return;
        aplicacaoItems.remove(index);
        bindRemocaoDeItemNoAdapter(index);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void limparItemsDeAplicacao() {
        aplicacaoItems.clear();
        protocoloItemAplicacaoAdapter.notifyDataSetChanged();
        showContadorMedicamentos();
    }

    private void limparSelecaoDeProtocolo() {
        autoCompleteTextViewProtocolos.setText(getString(R.string.hint_protocolo_vacinacao), false);
        textProtocoloNome.setText(R.string.protocolo_nome_vazio);
    }

    private void releaseViews() {
        executor = null;
        textViewMedicamentos = null;
        editTextPeso = null;
        autoCompleteTextViewProtocolos = null;
        cardMedicamentoAvulso = null;
        buttonSalvar = null;
        imageViewInfo = null;
        imageViewRemover = null;
        recyclerMedicamentos = null;
        protocoloAdapter = null;
        cardEmptyState = null;
        cardProtocoloSelecionado = null;
        protocoloItemAplicacaoAdapter = null;
    }

    private void findByProtocolosParaDropdown() {
        findByProtocolosNoBanco(protocolos -> findAllItemsDosProtocolosNoBanco(itens -> handleProtocolosEncontrados(protocolos, itens)));
    }

    private void findByProtocolosNoBanco(@NonNull Consumer<List<Protocolo>> onSuccess) {
        executor.execute(requireContext(), AppDatabase::protocoloDao, ProtocoloDao::getAll, onSuccess, this::handleErroAoBuscarProtocolos);
    }

    private void findAllItemsDosProtocolosNoBanco(@NonNull Consumer<List<ProtocoloItem>> onSuccess) {
        executor.execute(requireContext(), AppDatabase::protocoloItemDao, ProtocoloItemDao::getAll, onSuccess, this::handleErroAoBuscarItemsProtocolo);
    }

    private void findByTodosItemsDosProtocolos(@NonNull Consumer<List<ProtocoloItem>> onSuccess, int idProtocolo) {
        executor.execute(requireContext(), AppDatabase::protocoloItemDao, ProtocoloItemDao::getAllByIdProtocolo, idProtocolo, onSuccess, this::handleErroAoBuscarItemsProtocolo);
    }

    private void findByItemsDoProtocolo(@NonNull ProtocoloUiState protocolo) {
        findByTodosItemsDosProtocolos(this::handleItemsDoProtocoloEncontrados, protocolo.getId());
    }

    private void handleProtocolosEncontrados(@NonNull List<Protocolo> protocolos, @NonNull List<ProtocoloItem> itens) {
        List<ProtocoloUiState> uiStates = Mapper.fromProtocolosToUiStateList(protocolos, itens);
        showNovosProtocolosDropdown(uiStates);
    }

    private void handleItemsDoProtocoloEncontrados(@NonNull List<ProtocoloItem> protocoloItems) {
        List<ProtocoloItemAplicacaoUiState> uiStates = Mapper.fromItensToUiStateAplicacaoList(protocoloItems);
        showNovosItemsDeAplicacao(uiStates);
    }

    private ProtocoloUiState findByPosicaoNoAdapter(int position) {
        if (isPosicaoInvalida(position)) return null;
        return protocoloAdapter.getItem(position);
    }

    private void handleErroAoBuscarProtocolos(@NonNull Throwable throwable) {
        showSnackBarErro(getString(R.string.erro_carregar_protocolos));
        Log.d(TAG, getString(R.string.erro_carregar_protocolos) + throwable.getMessage());
    }

    private void handleErroAoBuscarItemsProtocolo(@NonNull Throwable throwable) {
        showSnackBarErro(getString(R.string.erro_carregar_items_protocolo));
        Log.d(TAG, getString(R.string.erro_carregar_items_protocolo) + throwable.getMessage());
    }

    private boolean existeItemDeProtocolo() {
        return aplicacaoItems.stream().anyMatch(this::isItemDeProtocolo);
    }

    private boolean isItemDeProtocolo(@NonNull ProtocoloItemAplicacaoUiState item) {
        return item.getOrigem() == PROTOCOLO;
    }

    private boolean isInvalido(@Nullable Object value) {
        return value == null;
    }

    private boolean isPosicaoInvalida(int position) {
        return position == RecyclerView.NO_POSITION;
    }

    private boolean isPesoVazio() {
        return editTextPeso == null || editTextPeso.getText() == null || editTextPeso.getText().toString().trim().isEmpty();
    }

    public static boolean isEmpty(@Nullable TextView view) {
        return view == null || parseTextoDaView(view).isEmpty();
    }

    public static void formatTextoPlural(@NonNull TextView view, @NonNull Context context, @PluralsRes int resId, @Nullable Integer quantity) {
        if (quantity == null) {
            view.setText("");
            return;
        }
        view.setText(context.getResources().getQuantityString(resId, quantity, quantity));
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

    @NonNull
    public static String parseTextoDaView(@Nullable TextView view) {
        if (view == null || view.getText() == null) return "";
        return view.getText().toString().trim();
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
            Map<Integer, List<ProtocoloItem>> indice = indexarItensPorProtocolo(itens);
            List<ProtocoloUiState> resultado = new ArrayList<>();
            for (Protocolo protocolo : protocolos) {
                resultado.add(fromProtocoloToUiState(protocolo, countItensPorProtocolo(indice, protocolo)));
            }
            return resultado;
        }

        static List<ProtocoloItemAplicacaoUiState> fromItensToUiStateAplicacaoList(@NonNull List<ProtocoloItem> itens) {
            List<ProtocoloItemAplicacaoUiState> resultado = new ArrayList<>();
            for (ProtocoloItem item : itens) {
                resultado.add(fromItemToUiStateAplicacao(item));
            }
            return resultado;
        }

        private static ProtocoloUiState fromProtocoloToUiState(@NonNull Protocolo protocolo, int quantidadeItems) {
            return new ProtocoloUiState(protocolo.getIdProtocolo(), protocolo.getDescricao(), quantidadeItems, protocolo.getAplicacao(), new Date());
        }

        private static ProtocoloItemAplicacaoUiState fromItemToUiStateAplicacao(@NonNull ProtocoloItem item) {
            return new ProtocoloItemAplicacaoUiState(item.getDescricao(), PROTOCOLO, DOSAGEM_INICIAL, STATUS_NAO_APLICADO);
        }

        private static Map<Integer, List<ProtocoloItem>> indexarItensPorProtocolo(@NonNull List<ProtocoloItem> itens) {
            Map<Integer, List<ProtocoloItem>> indice = new HashMap<>();
            for (ProtocoloItem item : itens) {
                indice.computeIfAbsent(item.getIdProtocolo(), ArrayList::new).add(item);
            }
            return indice;
        }

        private static List<ProtocoloItem> findItemsByProtocoloNoMapa(@NonNull Map<Integer, List<ProtocoloItem>> indice, @NonNull Protocolo protocolo) {
            return indice.getOrDefault(protocolo.getIdProtocolo(), new ArrayList<>());
        }

        private static int countItensPorProtocolo(@NonNull Map<Integer, List<ProtocoloItem>> indice, @NonNull Protocolo protocolo) {
            return findItemsByProtocoloNoMapa(indice, protocolo).size();
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

        public <T> void execute(@NonNull Callable<T> task, @NonNull Consumer<T> onSuccess, @NonNull Consumer<Exception> onError) {
            executor.submit(() -> runTask(task, onSuccess, onError));
        }

        public <D, E> void execute(@NonNull Context context, @NonNull Function<AppDatabase, D> daoExtractor, @NonNull Function<D, E> query, @NonNull Consumer<E> onSuccess, @NonNull Consumer<Exception> onError) {
            executor.submit(() -> runWithDao(context, daoExtractor, query, onSuccess, onError));
        }

        public <D, P, E> void execute(@NonNull Context context, @NonNull Function<AppDatabase, D> daoExtractor, @NonNull BiFunction<D, P, E> query, @NonNull P param, @NonNull Consumer<E> onSuccess, @NonNull Consumer<Exception> onError) {
            executor.submit(() -> runWithDaoAndParam(context, daoExtractor, query, param, onSuccess, onError));
        }

        private <T> void runTask(@NonNull Callable<T> task, @NonNull Consumer<T> onSuccess, @NonNull Consumer<Exception> onError) {
            try {
                T result = task.call();
                post(() -> onSuccess.accept(result));
            } catch (Exception e) {
                post(() -> onError.accept(e));
            }
        }

        private <D, E> void runWithDao(@NonNull Context context, @NonNull Function<AppDatabase, D> daoExtractor, @NonNull Function<D, E> query, @NonNull Consumer<E> onSuccess, @NonNull Consumer<Exception> onError) {
            try (Data<D> data = Data.of(daoExtractor)) {
                E result = query.apply(data.get(context));
                post(() -> onSuccess.accept(result));
            } catch (Exception e) {
                post(() -> onError.accept(e));
            }
        }

        private <D, P, E> void runWithDaoAndParam(@NonNull Context context, @NonNull Function<AppDatabase, D> daoExtractor, @NonNull BiFunction<D, P, E> query, @NonNull P param, @NonNull Consumer<E> onSuccess, @NonNull Consumer<Exception> onError) {
            try (Data<D> data = Data.of(daoExtractor)) {
                E result = query.apply(data.get(context), param);
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

    private static class Data<T> implements Closeable {

        private volatile AppDatabase database;
        private Function<AppDatabase, T> extractor;
        private volatile boolean closed = false;

        private Data(@NonNull Function<AppDatabase, T> extractor) {
            this.extractor = extractor;
        }

        static <T> Data<T> of(@NonNull Function<AppDatabase, T> extractor) {
            return new Data<>(extractor);
        }

        private synchronized T get(@NonNull Context context) {
            if (closed) {
                throw new IllegalStateException("Recurso já foi fechado.");
            }
            if (database == null) {
                database = AppDatabase.getDatabase(context.getApplicationContext());
            }
            return extractor.apply(database);
        }

        @Override
        public synchronized void close() {
            closed = true;
            database = null;
            extractor = null;
        }
    }
}