package com.omni.container.ui.states;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;

public class ProtocoloItemSelecionadoUiState implements Parcelable {

    private final int id;
    private final String descricao;
    private final OrigemItem origem;
    private char status;
    private final String tipoDosagem;
    private final Double qtdeDose;
    private final Double pesoBase;
    private final String unDose;
    private double quantidadeAplicada;

    public ProtocoloItemSelecionadoUiState(int id, @NonNull String descricao, @NonNull OrigemItem origem,
                                           char status, @Nullable String tipoDosagem, @Nullable Double qtdeDose,
                                           @Nullable Double pesoBase, @Nullable String unDose, double quantidadeAplicada) {
        this.id = id;
        this.descricao = descricao;
        this.origem = origem;
        this.status = status;
        this.tipoDosagem = tipoDosagem;
        this.qtdeDose = qtdeDose;
        this.pesoBase = pesoBase;
        this.unDose = unDose;
        this.quantidadeAplicada = quantidadeAplicada;
    }

    protected ProtocoloItemSelecionadoUiState(@NonNull Parcel in) {
        id = in.readInt();
        descricao = in.readString();
        origem = OrigemItem.values()[in.readInt()];
        status = (char) in.readInt();
        tipoDosagem = in.readString();
        qtdeDose = (Double) in.readValue(Double.class.getClassLoader());
        pesoBase = (Double) in.readValue(Double.class.getClassLoader());
        unDose = in.readString();
        quantidadeAplicada = in.readDouble();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(descricao);
        dest.writeInt(origem.ordinal());
        dest.writeInt(status);
        dest.writeString(tipoDosagem);
        dest.writeValue(qtdeDose);
        dest.writeValue(pesoBase);
        dest.writeString(unDose);
        dest.writeDouble(quantidadeAplicada);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ProtocoloItemSelecionadoUiState> CREATOR = new Creator<>() {
        @Override
        public ProtocoloItemSelecionadoUiState createFromParcel(Parcel in) {
            return new ProtocoloItemSelecionadoUiState(in);
        }

        @Override
        public ProtocoloItemSelecionadoUiState[] newArray(int size) {
            return new ProtocoloItemSelecionadoUiState[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public OrigemItem getOrigem() {
        return origem;
    }

    public char getStatus() {
        return status;
    }

    public String getTipoDosagem() {
        return tipoDosagem;
    }

    public Double getQtdeDose() {
        return qtdeDose;
    }

    public Double getPesoBase() {
        return pesoBase;
    }

    public String getUnDose() {
        return unDose;
    }

    public double getQuantidadeAplicada() {
        return quantidadeAplicada;
    }

    public void setQuantidadeAplicada(double quantidadeAplicada) {
        this.quantidadeAplicada = quantidadeAplicada;
    }

    public void setStatus(char status) {
        this.status = status;
    }
}