package com.omni.container.ui.fragments;

import static com.omni.container.ui.states.OrigemItem.PROTOCOLO;

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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.os.BundleCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.omni.container.R;
import com.omni.container.data.AppDatabase;
import com.omni.container.data.dao.ItemDao;
import com.omni.container.data.dao.ItemMedicamentoDao;
import com.omni.container.data.dao.ProtocoloDao;
import com.omni.container.data.dao.ProtocoloItemDao;
import com.omni.container.data.dao.SanitarioDao;
import com.omni.container.data.dao.SanitarioDetDao;
import com.omni.container.data.entities.Item;
import com.omni.container.data.entities.ItemMedicamento;
import com.omni.container.data.entities.Protocolo;
import com.omni.container.data.entities.ProtocoloItem;
import com.omni.container.data.entities.Sanitario;
import com.omni.container.data.entities.SanitarioDet;
import com.omni.container.ui.adapters.ProtocoloAdapter;
import com.omni.container.ui.adapters.ProtocoloItemSelecionadoAdapter;
import com.omni.container.ui.states.OrigemItem;
import com.omni.container.ui.states.ProtocoloItemSelecionadoUiState;
import com.omni.container.ui.states.ProtocoloUiState;

import org.jspecify.annotations.NonNull;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;


public class ManejoSanitarioFragment extends Fragment {
    private static final String TAG = "FRAGMENT_XGP_MANEJO_SANITARIO";
    private static final int THREAD_POOL_SIZE = 4;
    private static final int ID_ANIMAL_AUSENTE = -1;
    private static final int SEM_PROTOCOLO = 0;
    private static final double PESO_AUSENTE = 0.0;
    private static final double DOSAGEM_INICIAL = 0.0;
    private static final char STATUS_INDEFINIDO = ' ';

    public static final String RESULT_KEY_PROTOCOLO_ITENS_SELECIONADOS = "result_protocolo_itens_selecionados";
    public static final String ARG_KEY_PROTOCOLO_ITENS_SELECIONADOS = "arg_protocolo_itens_selecionados";

    public static final String RESULT_KEY_ANIMAL = "result_animal";
    public static final String ARG_KEY_ID_ANIMAL = "arg_id_animal";
    public static final String ARG_KEY_COD_ANIMAL = "arg_cod_animal";
    public static final String ARG_KEY_COD_BOTTOM = "arg_cod_bottom";
    public static final String ARG_KEY_COD_SYS_BOV = "arg_cod_sys_bov";
    public static final String ARG_KEY_PESO = "arg_peso";

    private static final String STATE_KEY_APLICACAO_ITEMS = "state_aplicacao_items";
    private static final String STATE_KEY_PROTOCOLO_SELECIONADO = "state_protocolo_selecionado";
    private static final String STATE_KEY_ID_ANIMAL = "state_id_animal";
    private static final String STATE_KEY_COD_ANIMAL = "state_cod_animal";
    private static final String STATE_KEY_COD_BOTTOM = "state_cod_bottom";
    private static final String STATE_KEY_COD_SYS_BOV = "state_cod_sys_bov";
    private static final String STATE_KEY_PESO = "state_peso";

    private TextView textViewContadorItens;
    private TextView textViewNomeProtocolo;
    private EditText editTextPeso;
    private EditText editTextIdentificacao;
    private EditText editTextObservacao;
    private AutoCompleteTextView autoCompleteProtocolos;
    private MaterialCardView cardItemAvulso;
    private MaterialCardView cardProtocoloSelecionado;
    private LinearLayout cardEmptyState;
    private ImageView imageViewInfoProtocolo;
    private ImageView imageViewRemoverProtocolo;
    private Button buttonSalvar;
    private RecyclerView recyclerViewItens;

    private DbExecutor executor;
    private ProtocoloAdapter protocoloAdapter;
    private ProtocoloItemSelecionadoAdapter itensSelecionadosAdapter;
    private int idAnimal = ID_ANIMAL_AUSENTE;
    private String codAnimal;
    private String codBottom;
    private String codSysBov;
    private double peso = PESO_AUSENTE;
    private ProtocoloUiState protocoloSelecionado;
    private final List<ProtocoloItemSelecionadoUiState> aplicacaoItems = new ArrayList<>();
    private final List<ProtocoloUiState> protocolos = new ArrayList<>();

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
        setupPesoWatcher();
        setupEstadoInicial(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        saveAplicacaoItemsState(outState);
        saveProtocoloSelecionadoState(outState);
        saveAnimalState(outState);
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
        textViewContadorItens = view.findViewById(R.id.text_protocolo_itens_selecionados_count);
        textViewNomeProtocolo = view.findViewById(R.id.text_protocolo_nome);
        editTextPeso = view.findViewById(R.id.edit_peso);
        editTextIdentificacao = view.findViewById(R.id.edit_identificacao);
        editTextObservacao = view.findViewById(R.id.edit_observacao);
        autoCompleteProtocolos = view.findViewById(R.id.edit_protocolo);
        cardItemAvulso = view.findViewById(R.id.card_protocolo_item_avulso);
        cardProtocoloSelecionado = view.findViewById(R.id.card_protocolo_selecionado);
        cardEmptyState = view.findViewById(R.id.empty_state);
        buttonSalvar = view.findViewById(R.id.btn_salvar);
        imageViewInfoProtocolo = view.findViewById(R.id.btn_protocolo_info);
        imageViewRemoverProtocolo = view.findViewById(R.id.btn_protocolo_remover);
        recyclerViewItens = view.findViewById(R.id.recycler_protocolo_itens_selecionados);
    }

    private void setupAdapters() {
        setupDropdownProtocolo();
        setupRecyclerViewItens();
    }

    private void setupDropdownProtocolo() {
        protocoloAdapter = new ProtocoloAdapter(requireContext(), new ArrayList<>());
        autoCompleteProtocolos.setAdapter(protocoloAdapter);
        autoCompleteProtocolos.setOnItemClickListener((parent, v, position, id) -> handleProtocoloSelecionado(position));
    }

    private void setupRecyclerViewItens() {
        itensSelecionadosAdapter = new ProtocoloItemSelecionadoAdapter(aplicacaoItems, this::handleItemRemovido, this::handleItemInfoClicado);
        ViewUtils.setupVerticalRecyclerView(recyclerViewItens, itensSelecionadosAdapter, requireContext());
    }

    private void setupListeners() {
        cardItemAvulso.setOnClickListener(v -> navigateToConsultaProtocoloItem());
        imageViewInfoProtocolo.setOnClickListener(v -> handleInfoProtocolo());
        imageViewRemoverProtocolo.setOnClickListener(v -> handleRemoverProtocolo());
        buttonSalvar.setOnClickListener(v -> handleSalvar());
    }

    private void setupPesoWatcher() {
        editTextPeso.addTextChangedListener(new BaseTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handlePesoDigitado();
            }
        });
    }

    private void setupResultListeners() {
        setupListenerItensProtocolo();
        setupListenerAnimal();
    }

    private void setupListenerItensProtocolo() {
        getParentFragmentManager().setFragmentResultListener(
                RESULT_KEY_PROTOCOLO_ITENS_SELECIONADOS, getViewLifecycleOwner(),
                (requestKey, bundle) -> handleResultItensProtocolo(bundle));
    }

    private void setupListenerAnimal() {
        getParentFragmentManager().setFragmentResultListener(
                RESULT_KEY_ANIMAL, getViewLifecycleOwner(),
                (requestKey, bundle) -> handleResultAnimal(bundle));
    }

    private void setupEstadoInicial(@Nullable Bundle savedInstanceState) {
        restoreState(savedInstanceState);
        bindEstadoAtual();
        fetchProtocolosIfNeeded();
    }

    private void bindEstadoAtual() {
        bindProtocolosNoAdapter();
        bindProtocoloSelecionado();
        bindAplicacaoItems();
    }

    private void bindProtocolosNoAdapter() {
        protocoloAdapter.clear();
        protocoloAdapter.addAll(protocolos);
    }

    private void bindProtocoloSelecionado() {
        if (!isProtocoloSelecionado()) return;
        showProtocoloNoDropdown(protocoloSelecionado);
        textViewNomeProtocolo.setText(protocoloSelecionado.getDescricao());
    }

    @SuppressLint("NotifyDataSetChanged")
    private void bindAplicacaoItems() {
        itensSelecionadosAdapter.notifyDataSetChanged();
        showContadorItens();
        updateVisibilidade();
    }

    private void bindDadosAnimal() {
        ViewUtils.setText(editTextIdentificacao, getIdentificacaoAnimal());
        ViewUtils.setText(editTextPeso, getFormattedPeso());
    }

    private void showContadorItens() {
        textViewContadorItens.setText(
                ProtocoloAdapter.formatQuantidadeLabel(requireContext(), aplicacaoItems.size()));
    }

    private void showProtocoloNoDropdown(@NonNull ProtocoloUiState protocolo) {
        autoCompleteProtocolos.setText(protocolo.getDescricao().trim(), false);
    }

    private void showDialogInfoProtocolo(@NonNull String mensagem) {
        InfoAplicacaoMedicamentoFragment.newInstance(getString(R.string.title_info_protocolo), mensagem)
                .show(getParentFragmentManager(), InfoAplicacaoMedicamentoFragment.TAG);
    }

    private void showDialogInfoItemSelecionado(@NonNull String mensagem) {
        InfoAplicacaoMedicamentoFragment.newInstance(getString(R.string.title_info_medicamento), mensagem)
                .show(getParentFragmentManager(), InfoAplicacaoMedicamentoFragment.TAG);
    }

    private void showSnackBarSucesso(@NonNull String mensagem) {
        View view = requireView();
        Snackbar.make(view, mensagem, Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(view.getContext(), android.R.color.holo_green_dark))
                .setTextColor(Color.WHITE)
                .show();
    }

    private void showErrorDialog(@NonNull String mensagem) {
        new MaterialAlertDialogBuilder(requireContext())
                .setIcon(R.drawable.warning_24px)
                .setTitle(R.string.title_erro_dialog)
                .setMessage(mensagem)
                .setPositiveButton(R.string.btn_dialog_confirmar, null)
                .show();
    }

    private void showAvisoDialog(@NonNull String mensagem) {
        new MaterialAlertDialogBuilder(requireContext())
                .setIcon(R.drawable.warning_24px)
                .setTitle(R.string.title_aviso_dialog)
                .setMessage(mensagem)
                .setPositiveButton(R.string.btn_dialog_confirmar, null)
                .show();
    }

    private void showAvisoDuplicadosSeNecessario(int totalCandidatos, int totalAdicionados) {
        int duplicados = getQuantidadeDuplicados(totalCandidatos, totalAdicionados);
        if (!hasDuplicados(duplicados)) return;
        showAvisoDialog(buildMensagemDuplicados(duplicados));
    }

    private void updateVisibilidade() {
        ViewUtils.setVisible(!hasItens(), cardEmptyState);
        ViewUtils.setVisible(hasItens(), recyclerViewItens);
        ViewUtils.setVisible(isProtocoloSelecionado(), cardProtocoloSelecionado);
    }

    private void updateProtocoloSelecionado(@NonNull ProtocoloUiState protocolo) {
        protocoloSelecionado = protocolo;
        bindProtocoloSelecionado();
        updateVisibilidade();
    }

    private void updateDadosAnimal(@NonNull Bundle bundle) {
        idAnimal = bundle.getInt(ARG_KEY_ID_ANIMAL, ID_ANIMAL_AUSENTE);
        codAnimal = bundle.getString(ARG_KEY_COD_ANIMAL);
        codBottom = bundle.getString(ARG_KEY_COD_BOTTOM);
        codSysBov = bundle.getString(ARG_KEY_COD_SYS_BOV);
        peso = bundle.getDouble(ARG_KEY_PESO, PESO_AUSENTE);
        bindDadosAnimal();
        recalcularDosagens();
    }

    private void handleProtocoloSelecionado(int position) {
        ProtocoloUiState protocolo = protocoloAdapter.resolveProtocoloSelecionado(position);
        if (protocolo == null) {
            handleProtocoloEmBrancoSelecionado();
            return;
        }
        updateProtocoloSelecionado(protocolo);
        fetchItensDosProtocoloFromDb(protocolo);
    }

    private void handleProtocoloEmBrancoSelecionado() {
        removeItensDoProtocolo();
        clearProtocoloSelecionado();
        bindAplicacaoItems();
    }

    private void handleRemoverProtocolo() {
        if (!isProtocoloSelecionado()) return;
        removeItensDoProtocolo();
        clearProtocoloSelecionado();
        bindAplicacaoItems();
    }

    private void handleInfoProtocolo() {
        if (!isProtocoloSelecionado()) return;
        showDialogInfoProtocolo(protocoloSelecionado.getAplicacao());
    }

    private void handlePesoDigitado() {
        peso = getPesoInput();
        recalcularDosagens();
    }

    private void handleSalvar() {
        if (!hasAnimalSelecionado()) {
            showErrorDialog(getString(R.string.erro_animal_obrigatorio));
            return;
        }
        if (!isPesoValido()) {
            showErrorDialog(getString(R.string.erro_peso_obrigatorio));
            return;
        }
        if (isEmptyList(aplicacaoItems)) {
            showErrorDialog(getString(R.string.erro_nenhum_item));
            return;
        }
        if (hasItemComStatusIndefinido()) {
            showErrorDialog(getString(R.string.erro_status_nao_definido));
            return;
        }
        saveSanitarioData();
    }

    private void handleResultItensProtocolo(@NonNull Bundle bundle) {
        List<Item> itens = BundleCompat.getParcelableArrayList(bundle, ARG_KEY_PROTOCOLO_ITENS_SELECIONADOS, Item.class);
        if (isEmptyList(itens)) return;
        fetchDosagensDosItens(itens, medicamentos -> handleItensAvulsosProntos(itens, medicamentos));
    }

    private void handleItemRemovido(@NonNull ProtocoloItemSelecionadoUiState item, int position) {
        showContadorItens();
        updateVisibilidade();
        recalcularDosagens();
    }

    private void handleResultAnimal(@NonNull Bundle bundle) {
        if (!isAnimalValido(bundle)) return;
        updateDadosAnimal(bundle);
    }

    private void handleFetchProtocolosSuccess(@NonNull List<Protocolo> listaProtocolos, @NonNull List<ProtocoloItem> listaItens) {
        protocolos.clear();
        protocolos.addAll(Mapper.fromProtocolosToUiStateList(listaProtocolos, listaItens));
        bindProtocolosNoAdapter();
    }

    private void handleFetchItensProtocoloSuccess(@NonNull List<Item> itens) {
        fetchDosagensDosItens(itens, medicamentos -> handleItensProtocoloProntos(itens, medicamentos));
    }

    private void handleItensProtocoloProntos(@NonNull List<Item> itens, @NonNull List<ItemMedicamento> medicamentos) {
        removeItensDoProtocolo();
        List<ProtocoloItemSelecionadoUiState> candidatos = Mapper.fromItensToUiStateAplicacaoList(itens, medicamentos);
        addItensSemDuplicados(candidatos);
        recalcularDosagens();
    }

    private void handleItensAvulsosProntos(@NonNull List<Item> itens, @NonNull List<ItemMedicamento> medicamentos) {
        List<ProtocoloItemSelecionadoUiState> candidatos = Mapper.fromItensToUiStateAvulsoList(itens, medicamentos);
        int posicaoInicial = aplicacaoItems.size();
        int totalAdicionados = addItensSemDuplicados(candidatos);
        itensSelecionadosAdapter.notifyItemRangeInserted(posicaoInicial, totalAdicionados);
        recalcularDosagens();
    }

    private void handleItemInfoClicado(@NonNull ProtocoloItemSelecionadoUiState item) {
        if (!hasText(item.getAplicacao())) return;
        showDialogInfoItemSelecionado(item.getAplicacao());
    }

    private void handleSanitarioSalvo(@NonNull Sanitario sanitario, long idGerado) {
        sanitario.setIdSanitario((int) idGerado);
        saveSanitarioDetToDb(sanitario);
    }

    private void handleSaveSuccess() {
        showSnackBarSucesso(getString(R.string.sucesso_salvar_dados));
    }

    private void handleSaveError(@NonNull Throwable throwable) {
        showErrorDialog(getString(R.string.erro_salvar_dados));
        Log.e(TAG, getString(R.string.erro_salvar_dados) + throwable.getMessage());
    }

    private void handleFetchProtocolosError(@NonNull Throwable throwable) {
        showErrorDialog(getString(R.string.erro_carregar_protocolos));
        Log.e(TAG, getString(R.string.erro_carregar_protocolos) + throwable.getMessage());
    }

    private void handleFetchItensProtocoloError(@NonNull Throwable throwable) {
        showErrorDialog(getString(R.string.erro_carregar_protocolo_itens));
        Log.e(TAG, getString(R.string.erro_carregar_protocolo_itens) + throwable.getMessage());
    }

    private void handleFetchDosagensError(@NonNull Throwable throwable) {
        showErrorDialog(getString(R.string.erro_carregar_dosagens));
        Log.e(TAG, getString(R.string.erro_carregar_dosagens) + throwable.getMessage());
    }

    private int addItensSemDuplicados(@NonNull List<ProtocoloItemSelecionadoUiState> candidatos) {
        List<ProtocoloItemSelecionadoUiState> novosItens = filterItensNaoDuplicados(candidatos);
        aplicacaoItems.addAll(novosItens);
        showAvisoDuplicadosSeNecessario(candidatos.size(), novosItens.size());
        return novosItens.size();
    }

    private void recalcularDosagens() {
        if (!hasItens()) return;
        for (ProtocoloItemSelecionadoUiState item : aplicacaoItems) {
            item.setQuantidadeAplicada(Dosagem.calcular(item, peso));
        }
        bindAplicacaoItems();
    }


    private void saveSanitarioData() {
        saveSanitarioToDb(buildSanitario());
    }

    private void saveSanitarioToDb(@NonNull Sanitario sanitario) {
        executor.execute(requireContext(), AppDatabase::sanitarioDao,
                SanitarioDao::insert, sanitario,
                idGerado -> handleSanitarioSalvo(sanitario, idGerado),
                this::handleSaveError);
    }

    private void saveSanitarioDetToDb(@NonNull Sanitario sanitario) {
        List<SanitarioDet> detalhes = Mapper.fromAplicacaoItemsToSanitarioDetList(aplicacaoItems, sanitario);
        executor.execute(requireContext(), AppDatabase::sanitarioDetDao,
                SanitarioDetDao::insertAll, detalhes,
                this::handleSaveSuccess, this::handleSaveError);
    }


    private void fetchDosagensDosItens(@NonNull List<Item> itens, @NonNull Consumer<List<ItemMedicamento>> onSuccess) {
        List<Integer> ids = itens.stream().map(Item::getIdItem).collect(Collectors.toList());
        executor.execute(requireContext(), AppDatabase::itemMedicamentoDao,
                ItemMedicamentoDao::getMedicamentosByItens, ids,
                onSuccess, this::handleFetchDosagensError);
    }

    private void fetchProtocolosIfNeeded() {
        if (hasProtocolosCarregados()) return;
        fetchProtocolosParaDropdown();
    }

    private void fetchProtocolosParaDropdown() {
        fetchProtocolosFromDb(listaProtocolos ->
                fetchTodosItensProtocoloFromDb(listaItens ->
                        handleFetchProtocolosSuccess(listaProtocolos, listaItens)));
    }

    private void fetchProtocolosFromDb(@NonNull Consumer<List<Protocolo>> onSuccess) {
        executor.execute(requireContext(), AppDatabase::protocoloDao,
                ProtocoloDao::getAll, onSuccess, this::handleFetchProtocolosError);
    }

    private void fetchTodosItensProtocoloFromDb(@NonNull Consumer<List<ProtocoloItem>> onSuccess) {
        executor.execute(requireContext(), AppDatabase::protocoloItemDao,
                ProtocoloItemDao::getAll, onSuccess, this::handleFetchItensProtocoloError);
    }

    private void fetchItensDosProtocoloFromDb(@NonNull ProtocoloUiState protocolo) {
        executor.execute(requireContext(), AppDatabase::itemDao,
                ItemDao::getAllItemsByProtocolo, protocolo.getId(),
                this::handleFetchItensProtocoloSuccess, this::handleFetchItensProtocoloError);
    }

    private void navigateToConsultaProtocoloItem() {
        getParentFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .replace(R.id.fragment_container_view, new ConsultaProtocoloItemFragment())
                .commit();
    }

    @NonNull
    private Sanitario buildSanitario() {
        Sanitario sanitario = new Sanitario();
        sanitario.setIdAnimal(idAnimal);
        sanitario.setIdProtocolo(getIdProtocoloSelecionado());
        sanitario.setGuid(generateGuid());
        sanitario.setData(new Date());
        sanitario.setObservacao(getObservacaoInput());
        return sanitario;
    }

    @NonNull
    private String buildMensagemDuplicados(int duplicados) {
        return getResources().getQuantityString(R.plurals.aviso_itens_ja_adicionados, duplicados, duplicados);
    }

    @NonNull
    private String generateGuid() {
        return UUID.randomUUID().toString();
    }

    @NonNull
    private String getIdentificacaoAnimal() {
        if (hasText(codAnimal)) return codAnimal;
        if (hasText(codBottom)) return codBottom;
        if (hasText(codSysBov)) return codSysBov;
        return "";
    }

    @NonNull
    private String getFormattedPeso() {
        if (!hasPeso()) return "";
        return String.valueOf(peso);
    }

    private double getPesoInput() {
        if (editTextPeso == null || editTextPeso.getText() == null) return PESO_AUSENTE;
        return parsePeso(editTextPeso.getText().toString().trim());
    }

    private double parsePeso(@NonNull String value) {
        if (value.isEmpty()) return PESO_AUSENTE;
        try {
            return Double.parseDouble(value.replace(',', '.'));
        } catch (NumberFormatException e) {
            return PESO_AUSENTE;
        }
    }

    @NonNull
    private String getObservacaoInput() {
        if (editTextObservacao == null || editTextObservacao.getText() == null) return "";
        return editTextObservacao.getText().toString().trim();
    }

    private int getIdProtocoloSelecionado() {
        if (!isProtocoloSelecionado()) return SEM_PROTOCOLO;
        return protocoloSelecionado.getId();
    }

    @Nullable
    private static String getAplicacao(@Nullable Item item) {
        return item == null ? null : item.getAplicacao();
    }

    private int getQuantidadeDuplicados(int totalCandidatos, int totalAdicionados) {
        return totalCandidatos - totalAdicionados;
    }

    @NonNull
    private List<ProtocoloItemSelecionadoUiState> filterItensNaoDuplicados(@NonNull List<ProtocoloItemSelecionadoUiState> candidatos) {
        return candidatos.stream()
                .filter(item -> !isItemAdicionado(item))
                .collect(Collectors.toList());
    }


    private void clearProtocoloSelecionado() {
        protocoloSelecionado = null;
        autoCompleteProtocolos.setText("", false);
        autoCompleteProtocolos.clearFocus();
        textViewNomeProtocolo.setText(R.string.protocolo_nome_vazio);
    }

    private void clearViews() {
        executor = null;
        textViewContadorItens = null;
        textViewNomeProtocolo = null;
        editTextPeso = null;
        editTextIdentificacao = null;
        editTextObservacao = null;
        autoCompleteProtocolos = null;
        cardItemAvulso = null;
        cardProtocoloSelecionado = null;
        cardEmptyState = null;
        buttonSalvar = null;
        imageViewInfoProtocolo = null;
        imageViewRemoverProtocolo = null;
        recyclerViewItens = null;
        protocoloAdapter = null;
        itensSelecionadosAdapter = null;
    }

    private void removeItensDoProtocolo() {
        aplicacaoItems.removeIf(item -> item.getOrigem() == PROTOCOLO);
    }

    private void saveAplicacaoItemsState(@NonNull Bundle outState) {
        outState.putParcelableArrayList(STATE_KEY_APLICACAO_ITEMS, new ArrayList<>(aplicacaoItems));
    }

    private void saveProtocoloSelecionadoState(@NonNull Bundle outState) {
        outState.putParcelable(STATE_KEY_PROTOCOLO_SELECIONADO, protocoloSelecionado);
    }

    private void saveAnimalState(@NonNull Bundle outState) {
        outState.putInt(STATE_KEY_ID_ANIMAL, idAnimal);
        outState.putDouble(STATE_KEY_PESO, peso);
        outState.putString(STATE_KEY_COD_ANIMAL, codAnimal);
        outState.putString(STATE_KEY_COD_BOTTOM, codBottom);
        outState.putString(STATE_KEY_COD_SYS_BOV, codSysBov);
    }

    private void restoreState(@Nullable Bundle savedInstanceState) {
        if (isInvalid(savedInstanceState)) return;
        protocoloSelecionado = BundleCompat.getParcelable(savedInstanceState, STATE_KEY_PROTOCOLO_SELECIONADO, ProtocoloUiState.class);
        restoreAplicacaoItemsState(savedInstanceState);
        restoreAnimalState(savedInstanceState);
    }

    private void restoreAplicacaoItemsState(@NonNull Bundle savedInstanceState) {
        List<ProtocoloItemSelecionadoUiState> itens = BundleCompat.getParcelableArrayList(
                savedInstanceState, STATE_KEY_APLICACAO_ITEMS, ProtocoloItemSelecionadoUiState.class);
        if (isEmptyList(itens)) return;
        aplicacaoItems.clear();
        aplicacaoItems.addAll(itens);
    }

    private void restoreAnimalState(@NonNull Bundle savedInstanceState) {
        idAnimal = savedInstanceState.getInt(STATE_KEY_ID_ANIMAL, ID_ANIMAL_AUSENTE);
        peso = savedInstanceState.getDouble(STATE_KEY_PESO, PESO_AUSENTE);
        codAnimal = savedInstanceState.getString(STATE_KEY_COD_ANIMAL);
        codBottom = savedInstanceState.getString(STATE_KEY_COD_BOTTOM);
        codSysBov = savedInstanceState.getString(STATE_KEY_COD_SYS_BOV);
    }

    private boolean isProtocoloSelecionado() {
        return protocoloSelecionado != null;
    }

    private boolean isAnimalValido(@NonNull Bundle bundle) {
        return hasIdAnimalNoBundle(bundle) && hasCodigoAnimalNoBundle(bundle) && hasPesoNoBundle(bundle);
    }

    private boolean isPesoValido() {
        return getPesoInput() > PESO_AUSENTE;
    }

    private boolean isInvalid(@Nullable Object value) {
        return value == null;
    }

    private boolean isEmptyList(@Nullable List<?> lista) {
        return lista == null || lista.isEmpty();
    }

    private boolean isItemAdicionado(@NonNull ProtocoloItemSelecionadoUiState item) {
        return aplicacaoItems.stream().anyMatch(existente -> existente.getId() == item.getId());
    }

    private boolean isStatusIndefinido(@NonNull ProtocoloItemSelecionadoUiState item) {
        return item.getStatus() == STATUS_INDEFINIDO;
    }

    private boolean hasItens() {
        return !aplicacaoItems.isEmpty();
    }

    private boolean hasProtocolosCarregados() {
        return !protocolos.isEmpty();
    }
    private boolean hasItemComStatusIndefinido() {
        return aplicacaoItems.stream().anyMatch(this::isStatusIndefinido);
    }

    private boolean hasAnimalSelecionado() {
        return idAnimal > ID_ANIMAL_AUSENTE;
    }

    private boolean hasPeso() {
        return peso > PESO_AUSENTE;
    }

    private boolean hasCodigoAnimalNoBundle(@NonNull Bundle bundle) {
        return hasText(bundle.getString(ARG_KEY_COD_ANIMAL))
                || hasText(bundle.getString(ARG_KEY_COD_BOTTOM))
                || hasText(bundle.getString(ARG_KEY_COD_SYS_BOV));
    }

    private boolean hasIdAnimalNoBundle(@NonNull Bundle bundle) {
        return bundle.getInt(ARG_KEY_ID_ANIMAL, ID_ANIMAL_AUSENTE) > ID_ANIMAL_AUSENTE;
    }

    private boolean hasPesoNoBundle(@NonNull Bundle bundle) {
        return bundle.getDouble(ARG_KEY_PESO, PESO_AUSENTE) > PESO_AUSENTE;
    }

    private boolean hasText(@Nullable String value) {
        return value != null && !value.trim().isEmpty();
    }

    private boolean hasDuplicados(int duplicados) {
        return duplicados > 0;
    }


    private Handler createMainThreadHandler() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return Handler.createAsync(Looper.getMainLooper());
        }
        return new Handler(Looper.getMainLooper());
    }

    public abstract static class BaseTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

        @Override
        public void afterTextChanged(Editable s) { }
    }


    private static final class ViewUtils {

        static void setupVerticalRecyclerView(@NonNull RecyclerView recyclerView, RecyclerView.@NonNull Adapter<?> adapter, @NonNull Context context) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(adapter);
        }

        static void setText(@Nullable TextView view, @Nullable CharSequence text) {
            if (view == null) return;
            view.setText(text);
        }

        static void setVisible(boolean visible, @NonNull View... views) {
            int state = visible ? View.VISIBLE : View.GONE;
            for (View v : views) {
                v.setVisibility(state);
            }
        }
    }


    private static final class Mapper {

        static List<ProtocoloUiState> fromProtocolosToUiStateList(@NonNull List<Protocolo> protocolos, @NonNull List<ProtocoloItem> itens) {
            Map<Integer, Integer> contagemPorProtocolo = countItensPorProtocolo(itens);
            return protocolos.stream()
                    .map(protocolo -> fromProtocoloToUiState(protocolo, getQuantidadeItens(contagemPorProtocolo, protocolo)))
                    .collect(Collectors.toList());
        }

        static List<ProtocoloItemSelecionadoUiState> fromItensToUiStateAvulsoList(@NonNull List<Item> itens, @NonNull List<ItemMedicamento> medicamentos) {
            return fromItensToUiStateList(itens, OrigemItem.AVULSO, medicamentos);
        }

        static List<ProtocoloItemSelecionadoUiState> fromItensToUiStateAplicacaoList(@NonNull List<Item> itens, @NonNull List<ItemMedicamento> medicamentos) {
            return fromItensToUiStateList(itens, PROTOCOLO, medicamentos);
        }

        static List<SanitarioDet> fromAplicacaoItemsToSanitarioDetList(@NonNull List<ProtocoloItemSelecionadoUiState> items, @NonNull Sanitario sanitario) {
            List<SanitarioDet> result = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                result.add(fromAplicacaoItemToSanitarioDet(items.get(i), sanitario, i + 1));
            }
            return result;
        }

        private static List<ProtocoloItemSelecionadoUiState> fromItensToUiStateList(@NonNull List<Item> itens, @NonNull OrigemItem origem,
                                                                                    @NonNull List<ItemMedicamento> medicamentos) {
            Map<Integer, ItemMedicamento> dosagemPorItem = indexarPorItem(medicamentos);
            return itens.stream()
                    .map(item -> fromItemToUiState(item, origem, dosagemPorItem.get(item.getIdItem())))
                    .collect(Collectors.toList());
        }

        private static ProtocoloItemSelecionadoUiState fromItemToUiState(@NonNull Item item, @NonNull OrigemItem origem,
                                                                         @Nullable ItemMedicamento medicamento) {
            return new ProtocoloItemSelecionadoUiState(
                    item.getIdItem(), item.getDescricao(),
                    origem, STATUS_INDEFINIDO,
                    getTipoDosagem(medicamento),
                    getQtdeDose(medicamento),
                    getPesoBase(medicamento),
                    getUnDose(medicamento),
                    getAplicacao(item),
                    DOSAGEM_INICIAL);
        }

        private static Map<Integer, ItemMedicamento> indexarPorItem(@NonNull List<ItemMedicamento> medicamentos) {
            return medicamentos.stream()
                    .collect(Collectors.toMap(ItemMedicamento::getIdItem, medicamento -> medicamento));
        }

        @Nullable
        private static String getTipoDosagem(@Nullable ItemMedicamento medicamento) {
            return medicamento == null ? null : medicamento.getTipoDosagem();
        }

        @Nullable
        private static Double getQtdeDose(@Nullable ItemMedicamento medicamento) {
            return medicamento == null ? null : medicamento.getQtdeDose();
        }

        @Nullable
        private static Double getPesoBase(@Nullable ItemMedicamento medicamento) {
            return medicamento == null ? null : medicamento.getPesoBase();
        }

        @Nullable
        private static String getUnDose(@Nullable ItemMedicamento medicamento) {
            return medicamento == null ? null : medicamento.getUnDose();
        }

        private static ProtocoloUiState fromProtocoloToUiState(@NonNull Protocolo protocolo, int quantidadeItems) {
            return new ProtocoloUiState(protocolo.getIdProtocolo(), protocolo.getDescricao(),
                    quantidadeItems, protocolo.getAplicacao(), new Date());
        }

        private static SanitarioDet fromAplicacaoItemToSanitarioDet(@NonNull ProtocoloItemSelecionadoUiState item, @NonNull Sanitario sanitario, int sequence) {
            return new SanitarioDet(sanitario.getIdSanitario(), sequence, item.getId(), item.getQuantidadeAplicada(), item.getStatus());
        }

        private static Map<Integer, Integer> countItensPorProtocolo(@NonNull List<ProtocoloItem> itens) {
            return itens.stream().collect(Collectors.groupingBy(ProtocoloItem::getIdProtocolo, Collectors.summingInt(item -> 1)));
        }

        private static int getQuantidadeItens(@NonNull Map<Integer, Integer> contagem, @NonNull Protocolo protocolo) {
            Integer quantidade = contagem.get(protocolo.getIdProtocolo());
            return quantidade == null ? 0 : quantidade;
        }
    }


    private static final class Dosagem {
        private static final char TIPO_POR_ANIMAL = 'A';

        static double calcular(@NonNull ProtocoloItemSelecionadoUiState item, double pesoAnimal) {
            if (isPorAnimal(item)) return getQtdeDose(item);
            return calcularPorPeso(item, pesoAnimal);
        }

        private static double calcularPorPeso(@NonNull ProtocoloItemSelecionadoUiState item, double pesoAnimal) {
            double pesoBase = getPesoBase(item);
            if (!isCalculavel(pesoAnimal, pesoBase)) return DOSAGEM_INICIAL;
            return (pesoAnimal / pesoBase) * getQtdeDose(item);
        }

        private static boolean isPorAnimal(@NonNull ProtocoloItemSelecionadoUiState item) {
            return hasTipoDosagem(item) && item.getTipoDosagem().charAt(0) == TIPO_POR_ANIMAL;
        }

        private static boolean isCalculavel(double pesoAnimal, double pesoBase) {
            return pesoAnimal > PESO_AUSENTE && pesoBase > 0;
        }

        private static boolean hasTipoDosagem(@NonNull ProtocoloItemSelecionadoUiState item) {
            return item.getTipoDosagem() != null && !item.getTipoDosagem().isEmpty();
        }

        private static double getQtdeDose(@NonNull ProtocoloItemSelecionadoUiState item) {
            return item.getQtdeDose() == null ? DOSAGEM_INICIAL : item.getQtdeDose();
        }

        private static double getPesoBase(@NonNull ProtocoloItemSelecionadoUiState item) {
            return item.getPesoBase() == null ? 0 : item.getPesoBase();
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

        <D, P, E> void execute(@NonNull Context context, @NonNull Function<AppDatabase, D> daoExtractor, @NonNull BiFunction<D, P, E> query, @NonNull P param,
                               @NonNull Consumer<E> onSuccess, @NonNull Consumer<Exception> onError) {
            submit(() -> query.apply(resolveDao(context, daoExtractor), param), onSuccess, onError);
        }

        <D, P> void execute(@NonNull Context context, @NonNull Function<AppDatabase, D> daoExtractor, @NonNull BiConsumer<D, P> query, @NonNull P param,
                            @NonNull Runnable onSuccess, @NonNull Consumer<Exception> onError) {
            submit(() -> query.accept(resolveDao(context, daoExtractor), param), onSuccess, onError);
        }

        private <T> void submit(@NonNull Callable<T> task, @NonNull Consumer<T> onSuccess, @NonNull Consumer<Exception> onError) {
            executor.submit(() -> runTask(task, onSuccess, onError));
        }

        private void submit(@NonNull Runnable task, @NonNull Runnable onSuccess, @NonNull Consumer<Exception> onError) {
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

        private void runTask(@NonNull Runnable task, @NonNull Runnable onSuccess, @NonNull Consumer<Exception> onError) {
            runTask(toCallable(task), ignored -> onSuccess.run(), onError);
        }

        @NonNull
        private static Callable<Void> toCallable(@NonNull Runnable task) {
            return () -> {
                task.run();
                return null;
            };
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