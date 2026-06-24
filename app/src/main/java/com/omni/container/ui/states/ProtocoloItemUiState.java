package com.omni.container.ui.states;

public class ProtocoloItemUiState {
    private final String descricao;
    private final String aplicacao;

    public ProtocoloItemUiState(String descricao, String aplicacao) {
        this.descricao = descricao;
        this.aplicacao = aplicacao;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getAplicacao() {
        return aplicacao;
    }
}
