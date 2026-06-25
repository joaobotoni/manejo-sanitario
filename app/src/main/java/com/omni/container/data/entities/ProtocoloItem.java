package com.omni.container.data.entities;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;


@Entity(tableName = "xgp_protocolo_item", primaryKeys = {"id_protocolo", "id_protocolo_item"})
public class ProtocoloItem implements Parcelable {

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

    @Ignore
    private boolean isSelected;

    public ProtocoloItem() {
    }

    protected ProtocoloItem(Parcel in) {
        idProtocolo = in.readInt();
        idProtocoloItem = in.readInt();
        idTipoManejo = readNullableInt(in);
        idItem = readNullableInt(in);
        descricao = in.readString();
        ordem = readNullableInt(in);
        qtdeDias = readNullableInt(in);
        prazoCarencia = readNullableInt(in);
        isSelected = in.readByte() != 0;
    }

    @NonNull
    public static final Creator<ProtocoloItem> CREATOR = new Creator<ProtocoloItem>() {
        @Override
        public ProtocoloItem createFromParcel(Parcel in) {
            return new ProtocoloItem(in);
        }

        @Override
        public ProtocoloItem[] newArray(int size) {
            return new ProtocoloItem[size];
        }
    };

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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(idProtocolo);
        dest.writeInt(idProtocoloItem);
        writeNullableInt(dest, idTipoManejo);
        writeNullableInt(dest, idItem);
        dest.writeString(descricao);
        writeNullableInt(dest, ordem);
        writeNullableInt(dest, qtdeDias);
        writeNullableInt(dest, prazoCarencia);
        dest.writeByte((byte) (isSelected ? 1 : 0));
    }

    private static Integer readNullableInt(Parcel in) {
        if (in.readByte() == 0) return null;
        return in.readInt();
    }

    private static void writeNullableInt(Parcel dest, Integer value) {
        if (value == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(value);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProtocoloItem)) return false;
        ProtocoloItem that = (ProtocoloItem) o;
        return idProtocolo == that.idProtocolo && idProtocoloItem == that.idProtocoloItem;
    }

    @Override
    public int hashCode() {
        return 31 * idProtocolo + idProtocoloItem;
    }
}