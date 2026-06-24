package com.omni.container.ui.states;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class ProtocoloUiState implements Parcelable {

    private final int id;
    private final String descricao;
    private final int quantidadeMedicamentos;
    private final String aplicacao;
    private final Date date;

    public ProtocoloUiState(int id, String descricao, int quantidadeMedicamentos, String aplicacao, Date date) {
        this.id = id;
        this.descricao = descricao;
        this.quantidadeMedicamentos = quantidadeMedicamentos;
        this.aplicacao = aplicacao;
        this.date = date;
    }

    protected ProtocoloUiState(Parcel in) {
        id = in.readInt();
        descricao = in.readString();
        quantidadeMedicamentos = in.readInt();
        aplicacao = in.readString();
        date = new Date(in.readLong());
    }

    public static final Creator<ProtocoloUiState> CREATOR = new Creator<ProtocoloUiState>() {
        @Override
        public ProtocoloUiState createFromParcel(Parcel in) {
            return new ProtocoloUiState(in);
        }

        @Override
        public ProtocoloUiState[] newArray(int size) {
            return new ProtocoloUiState[size];
        }
    };

    public int getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getQuantidadeMedicamentos() {
        return quantidadeMedicamentos;
    }

    public String getAplicacao() {
        return aplicacao;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(descricao);
        dest.writeInt(quantidadeMedicamentos);
        dest.writeString(aplicacao);
        dest.writeLong(date != null ? date.getTime() : -1L);
    }
}