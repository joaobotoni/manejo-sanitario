package com.omni.container.data.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "xgp_item")
public class Item implements Parcelable {
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id_item")
    private int idItem;
    @ColumnInfo(name = "cod_item")
    private String codItem;
    private String descricao;
    private String ativo;
    @ColumnInfo(name = "id_tipo_item")
    private int idTipoItem;

    private String aplicacao;

    public Item() {
    }

    protected Item(Parcel in) {
        idItem = in.readInt();
        codItem = in.readString();
        descricao = in.readString();
        ativo = in.readString();
        idTipoItem = in.readInt();
        aplicacao = in.readString();
    }

    public static final Creator<Item> CREATOR = new Creator<>() {
        @Override
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    public int getIdItem() {
        return idItem;
    }

    public void setIdItem(int idItem) {
        this.idItem = idItem;
    }

    public String getCodItem() {
        return codItem;
    }

    public void setCodItem(String codItem) {
        this.codItem = codItem;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getIdTipoItem() {
        return idTipoItem;
    }

    public void setIdTipoItem(int idTipoItem) {
        this.idTipoItem = idTipoItem;
    }

    public String getAtivo() {
        return ativo;
    }

    public void setAtivo(String ativo) {
        this.ativo = ativo;
    }

    public String getAplicacao() {
        return aplicacao;
    }

    public void setAplicacao(String aplicacao) {
        this.aplicacao = aplicacao;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(idItem);
        dest.writeString(codItem);
        dest.writeString(descricao);
        dest.writeString(ativo);
        dest.writeInt(idTipoItem);
        dest.writeString(aplicacao);
    }
}