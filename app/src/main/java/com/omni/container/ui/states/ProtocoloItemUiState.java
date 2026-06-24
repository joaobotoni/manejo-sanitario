package com.omni.container.ui.states;

public class ProtocoloItemUiState {
    private final int id;
    private final String descricao;

    public ProtocoloItemUiState(int id, String descricao) {
        this.id = id;
        this.descricao = descricao;
    }

    public int getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }
}
