package com.omni.container.data.entities;


import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity(tableName = "xgp_item")
public class Item {
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
}