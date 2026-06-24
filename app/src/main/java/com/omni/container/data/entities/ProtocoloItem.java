package com.omni.container.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;


@Entity(tableName = "xgp_protocolo_item", primaryKeys = {"id_protocolo", "id_protocolo_item"})
public class ProtocoloItem {

    @ColumnInfo(name = "id_protocolo")
    private int idProtocolo;

    @ColumnInfo(name = "id_protocolo_item")
    private int idProtocoloItem;

    @ColumnInfo(name = "id_tipo_manejo")
    private Integer idTipoManejo;

    @ColumnInfo(name = "id_item")
    private Integer idItem;

    @ColumnInfo(name = "descricao")
    private String descricao;

    @ColumnInfo(name = "ordem")
    private Integer ordem;

    @ColumnInfo(name = "qtde_dias")
    private Integer qtdeDias;

    @ColumnInfo(name = "prazo_carencia")
    private Integer prazoCarencia;

    public int getIdProtocolo() {
        return idProtocolo;
    }

    public void setIdProtocolo(int idProtocolo) {
        this.idProtocolo = idProtocolo;
    }

    public int getIdProtocoloItem() {
        return idProtocoloItem;
    }

    public void setIdProtocoloItem(int idProtocoloItem) {
        this.idProtocoloItem = idProtocoloItem;
    }

    public Integer getIdTipoManejo() {
        return idTipoManejo;
    }

    public void setIdTipoManejo(Integer idTipoManejo) {
        this.idTipoManejo = idTipoManejo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public Integer getQtdeDias() {
        return qtdeDias;
    }

    public void setQtdeDias(Integer qtdeDias) {
        this.qtdeDias = qtdeDias;
    }

    public Integer getPrazoCarencia() {
        return prazoCarencia;
    }

    public void setPrazoCarencia(Integer prazoCarencia) {
        this.prazoCarencia = prazoCarencia;
    }

    public Integer getIdItem() {
        return idItem;
    }

    public void setIdItem(Integer idItem) {
        this.idItem = idItem;
    }
}