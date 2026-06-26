package com.omni.container.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

@Entity(tableName = "xgp_sanitario_det", primaryKeys = {"id_sanitario", "id_sanitario_det"})
public class SanitarioDet {
    @ColumnInfo(name = "id_sanitario")
    private int idSanitario;
    @ColumnInfo(name = "id_sanitario_det")
    private int idSanitarioDet;
    @ColumnInfo(name = "id_animal")
    private int idItem;
    private double qtde;
    private char status;

    public SanitarioDet() {
    }

    @Ignore
    public SanitarioDet(int idSanitario, int idSanitarioDet, int idItem, double qtde, char status) {
        this.idSanitario = idSanitario;
        this.idSanitarioDet = idSanitarioDet;
        this.idItem = idItem;
        this.qtde = qtde;
        this.status = status;
    }

    public int getIdSanitario() {
        return idSanitario;
    }

    public void setIdSanitario(int idSanitario) {
        this.idSanitario = idSanitario;
    }

    public int getIdSanitarioDet() {
        return idSanitarioDet;
    }

    public void setIdSanitarioDet(int idSanitarioDet) {
        this.idSanitarioDet = idSanitarioDet;
    }

    public int getIdItem() {
        return idItem;
    }

    public void setIdItem(int idItem) {
        this.idItem = idItem;
    }

    public double getQtde() {
        return qtde;
    }

    public void setQtde(Integer qtde) {
        this.qtde = qtde;
    }

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
    }
}
