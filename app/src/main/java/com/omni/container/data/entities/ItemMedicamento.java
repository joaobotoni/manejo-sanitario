package com.omni.container.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "xgp_item_medicamento")
public class ItemMedicamento {

    @PrimaryKey
    @ColumnInfo(name = "id_item")
    private int idItem;

    @ColumnInfo(name = "principio_ativo")
    private String principioAtivo;

    @ColumnInfo(name = "carencia_abate")
    private Integer carenciaAbate;

    @ColumnInfo(name = "carencia_leite")
    private Integer carenciaLeite;

    @ColumnInfo(name = "tipo_dosagem")
    private String tipoDosagem;

    @ColumnInfo(name = "qtde_dose")
    private Double qtdeDose;

    @ColumnInfo(name = "un_dose")
    private String unDose;

    @ColumnInfo(name = "peso_base")
    private Double pesoBase;

    @ColumnInfo(name = "periodo_tratamento")
    private Integer periodoTratamento;

    @ColumnInfo(name = "observacao_uso")
    private String observacaoUso;

    @ColumnInfo(name = "data_created")
    private Long dataCreated;

    @ColumnInfo(name = "usuario_changed")
    private String usuarioChanged;

    @ColumnInfo(name = "data_changed")
    private Long dataChanged;

    @ColumnInfo(name = "usuario_created")
    private String usuarioCreated;

    public int getIdItem() {
        return idItem;
    }

    public void setIdItem(int idItem) {
        this.idItem = idItem;
    }

    public String getPrincipioAtivo() {
        return principioAtivo;
    }

    public void setPrincipioAtivo(String principioAtivo) {
        this.principioAtivo = principioAtivo;
    }

    public Integer getCarenciaAbate() {
        return carenciaAbate;
    }

    public void setCarenciaAbate(Integer carenciaAbate) {
        this.carenciaAbate = carenciaAbate;
    }

    public Integer getCarenciaLeite() {
        return carenciaLeite;
    }

    public void setCarenciaLeite(Integer carenciaLeite) {
        this.carenciaLeite = carenciaLeite;
    }

    public String getTipoDosagem() {
        return tipoDosagem;
    }

    public void setTipoDosagem(String tipoDosagem) {
        this.tipoDosagem = tipoDosagem;
    }

    public Double getQtdeDose() {
        return qtdeDose;
    }

    public void setQtdeDose(Double qtdeDose) {
        this.qtdeDose = qtdeDose;
    }

    public String getUnDose() {
        return unDose;
    }

    public void setUnDose(String unDose) {
        this.unDose = unDose;
    }

    public Double getPesoBase() {
        return pesoBase;
    }

    public void setPesoBase(Double pesoBase) {
        this.pesoBase = pesoBase;
    }

    public Integer getPeriodoTratamento() {
        return periodoTratamento;
    }

    public void setPeriodoTratamento(Integer periodoTratamento) {
        this.periodoTratamento = periodoTratamento;
    }

    public String getObservacaoUso() {
        return observacaoUso;
    }

    public void setObservacaoUso(String observacaoUso) {
        this.observacaoUso = observacaoUso;
    }

    public Long getDataCreated() {
        return dataCreated;
    }

    public void setDataCreated(Long dataCreated) {
        this.dataCreated = dataCreated;
    }

    public String getUsuarioChanged() {
        return usuarioChanged;
    }

    public void setUsuarioChanged(String usuarioChanged) {
        this.usuarioChanged = usuarioChanged;
    }

    public Long getDataChanged() {
        return dataChanged;
    }

    public void setDataChanged(Long dataChanged) {
        this.dataChanged = dataChanged;
    }

    public String getUsuarioCreated() {
        return usuarioCreated;
    }

    public void setUsuarioCreated(String usuarioCreated) {
        this.usuarioCreated = usuarioCreated;
    }
}