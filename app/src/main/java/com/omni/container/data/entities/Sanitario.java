package com.omni.container.data.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "xgp_sanitario")
public class Sanitario {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_sanitario")
    private int idSanitario;

    @ColumnInfo(name = "id_animal")
    private int idAnimal;

    @ColumnInfo(name = "id_protocolo")
    private int idProtocolo;

    private String guid;

    private Date data;

    private String observacao;

    public int getIdSanitario() {
        return idSanitario;
    }

    public void setIdSanitario(int idSanitario) {
        this.idSanitario = idSanitario;
    }

    public int getIdAnimal() {
        return idAnimal;
    }

    public void setIdAnimal(int idAnimal) {
        this.idAnimal = idAnimal;
    }

    public int getIdProtocolo() {
        return idProtocolo;
    }

    public void setIdProtocolo(int idProtocolo) {
        this.idProtocolo = idProtocolo;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
