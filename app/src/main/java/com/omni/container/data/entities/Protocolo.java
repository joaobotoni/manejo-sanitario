package com.omni.container.data.entities;


import static androidx.room.ColumnInfo.INTEGER;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.TypeConverters;


import com.omni.container.data.Converters;

import java.util.Date;

@Entity(tableName = "xgp_protocolo", primaryKeys = {"id_protocolo",})
public class Protocolo implements Parcelable {

    @ColumnInfo(name = "id_protocolo")
    private int idProtocolo;

    @ColumnInfo(name = "aplicacao")
    private String aplicacao;

    @ColumnInfo(name = "descricao")
    private String descricao;

    @ColumnInfo(name = "data", typeAffinity = INTEGER)
    @TypeConverters({Converters.class})
    private Date data;

    @ColumnInfo(name = "responsavel")
    private String responsavel;

    @ColumnInfo(name = "ativo")
    private String ativo;

    @ColumnInfo(name = "usuario_created")
    private String usuarioCreated;

    @ColumnInfo(name = "data_created", typeAffinity = INTEGER)
    private Date dataCreated;

    @ColumnInfo(name = "data_push", typeAffinity = INTEGER)
    @TypeConverters({Converters.class})
    private Date dataPush;

    @ColumnInfo(name = "status_app")
    private String statusApp;

    public Protocolo() {
    }

    public int getIdProtocolo() {
        return idProtocolo;
    }

    public void setIdProtocolo(int idProtocolo) {
        this.idProtocolo = idProtocolo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public String getResponsavel() {
        return responsavel;
    }

    public void setResponsavel(String responsavel) {
        this.responsavel = responsavel;
    }

    public String getAtivo() {
        return ativo;
    }

    public void setAtivo(String ativo) {
        this.ativo = ativo;
    }

    public String getUsuarioCreated() {
        return usuarioCreated;
    }

    public void setUsuarioCreated(String usuarioCreated) {
        this.usuarioCreated = usuarioCreated;
    }

    public Date getDataCreated() {
        return dataCreated;
    }

    public void setDataCreated(Date dataCreated) {
        this.dataCreated = dataCreated;
    }

    public Date getDataPush() {
        return dataPush;
    }

    public void setDataPush(Date dataPush) {
        this.dataPush = dataPush;
    }

    public String getStatusApp() {
        return statusApp;
    }

    public void setStatusApp(String statusApp) {
        this.statusApp = statusApp;
    }

    public String getAplicacao() {
        return aplicacao;
    }

    public void setAplicacao(String aplicacao) {
        this.aplicacao = aplicacao;
    }

    protected Protocolo(Parcel in) {
        idProtocolo = in.readInt();
        aplicacao = in.readString();
        descricao = in.readString();
        data = in.readLong() > -1 ? new Date(in.readLong()) : null;
        responsavel = in.readString();
        ativo = in.readString();
        usuarioCreated = in.readString();
        dataCreated = in.readLong() > -1 ? new Date(in.readLong()) : null;
        dataPush = in.readLong() > -1 ? new Date(in.readLong()) : null;
        statusApp = in.readString();
    }

    public static final Creator<Protocolo> CREATOR = new Creator<Protocolo>() {
        @Override
        public Protocolo createFromParcel(Parcel in) {
            return new Protocolo(in);
        }

        @Override
        public Protocolo[] newArray(int size) {
            return new Protocolo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(idProtocolo);
        parcel.writeString(aplicacao);
        parcel.writeString(descricao);
        parcel.writeLong(data != null ? data.getTime() : -1);
        parcel.writeString(responsavel);
        parcel.writeString(ativo);
        parcel.writeString(usuarioCreated);
        parcel.writeLong(dataCreated != null ? dataCreated.getTime() : -1);
        parcel.writeLong(dataPush != null ? dataPush.getTime() : -1);
        parcel.writeString(statusApp);
    }
}