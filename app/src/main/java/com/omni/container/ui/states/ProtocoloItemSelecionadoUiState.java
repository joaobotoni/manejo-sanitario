package com.omni.container.ui.states;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ProtocoloItemSelecionadoUiState implements Parcelable {
    private int id;
    private final String descricao;
    private final OrigemItem origem;
    private double quantidadeAplicada;
    private String unDose;
    private String tipoDosagem;
    private Double qtdeDose;
    private Double pesoBase;
    private char status;

    public ProtocoloItemSelecionadoUiState(int id, String descricao, OrigemItem origem, double quantidadeAplicada, char status) {
        this.id = id;
        this.descricao = descricao;
        this.origem = origem;
        this.quantidadeAplicada = quantidadeAplicada;
        this.status = status;
    }

    protected ProtocoloItemSelecionadoUiState(Parcel in) {
        descricao = in.readString();
        origem = OrigemItem.valueOf(in.readString());
        quantidadeAplicada = in.readDouble();
        status = (char) in.readInt();
        tipoDosagem = in.readString();
        unDose = in.readString();
        qtdeDose = (Double) in.readValue(Double.class.getClassLoader());
        pesoBase = (Double) in.readValue(Double.class.getClassLoader());
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

    public String getDescricao() {
        return descricao;
    }

    public OrigemItem getOrigem() {
        return origem;
    }

    public double getQuantidadeAplicada() {
        return quantidadeAplicada;
    }

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipoDosagem() {
        return tipoDosagem;
    }

    public void setTipoDosagem(String tipoDosagem) {
        this.tipoDosagem = tipoDosagem;
    }

    public String getUnDose() {
        return unDose;
    }

    public void setUnDose(String unDose) {
        this.unDose = unDose;
    }

    public Double getQtdeDose() {
        return qtdeDose;
    }

    public void setQtdeDose(Double qtdeDose) {
        this.qtdeDose = qtdeDose;
    }

    public Double getPesoBase() {
        return pesoBase;
    }

    public void setPesoBase(Double pesoBase) {
        this.pesoBase = pesoBase;
    }

    public void setQuantidadeAplicada(double quantidadeAplicada) {
        this.quantidadeAplicada = quantidadeAplicada;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(descricao);
        dest.writeString(origem.name());
        dest.writeDouble(quantidadeAplicada);
        dest.writeInt(status);
        dest.writeString(tipoDosagem);
        dest.writeValue(qtdeDose);
        dest.writeValue(pesoBase);
        dest.writeValue(unDose);
    }

}