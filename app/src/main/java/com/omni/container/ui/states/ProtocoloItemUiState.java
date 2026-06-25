package com.omni.container.ui.states;

public class ProtocoloItemUiState {
    private final int id;
    private final String descricao;
    private final boolean isChecked;

    public ProtocoloItemUiState(int id, String descricao, boolean isChecked) {
        this.id = id;
        this.descricao = descricao;
        this.isChecked = isChecked;
    }

    public int getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isChecked() {
        return isChecked;
    }
}
