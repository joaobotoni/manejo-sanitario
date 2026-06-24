package com.omni.container.ui.states;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;


public class ProtocoloItemAplicacaoUiState implements Parcelable {

    private final String descricao;
    private final OrigemItem origem;
    private final double quantidadeAplicada;
    private char status;

    public ProtocoloItemAplicacaoUiState(String descricao, OrigemItem origem, double quantidadeAplicada, char status) {
        this.descricao = descricao;
        this.origem = origem;
        this.quantidadeAplicada = quantidadeAplicada;
        this.status = status;
    }

    protected ProtocoloItemAplicacaoUiState(Parcel in) {
        descricao = in.readString();
        origem = OrigemItem.valueOf(in.readString());
        quantidadeAplicada = in.readDouble();
        status = (char) in.readInt();
    }

    public static final Creator<ProtocoloItemAplicacaoUiState> CREATOR = new Creator<ProtocoloItemAplicacaoUiState>() {
        @Override
        public ProtocoloItemAplicacaoUiState createFromParcel(Parcel in) {
            return new ProtocoloItemAplicacaoUiState(in);
        }

        @Override
        public ProtocoloItemAplicacaoUiState[] newArray(int size) {
            return new ProtocoloItemAplicacaoUiState[size];
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

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(descricao);
        dest.writeString(origem.name());
        dest.writeDouble(quantidadeAplicada);
        dest.writeInt(status);
    }
}