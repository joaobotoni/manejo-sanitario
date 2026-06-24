package com.omni.container.ui.states;

public enum OrigemItem {
    PROTOCOLO('P', "Protocolo"),
    AVULSO('A', "Avulso");
    private final char origem;
    private final String nome;

    OrigemItem(char origem, String nome) {
        this.origem = origem;
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public static OrigemItem fromChar(char origem) {
        for (OrigemItem item : values()) {
            if (item.origem == origem) {
                return item;
            }
        }
        throw new IllegalArgumentException("Origem inválida: " + origem);
    }
}
