package com.omni.container.ui.states;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class ItemMedicamentoUiState implements Parcelable {
    private final int id;
    @NonNull
    private final String descricao;
    @NonNull
    private final String aplicacao;
    @NonNull
    private final OrigemItem origem;
    private final boolean isChecked;

    public ItemMedicamentoUiState(int id, @NonNull String descricao, @NonNull String aplicacao, @NonNull OrigemItem origem, boolean isChecked) {
        this.id = id;
        this.descricao = descricao;
        this.aplicacao = aplicacao;
        this.origem = origem;
        this.isChecked = isChecked;
    }

    private ItemMedicamentoUiState(Parcel in) {
        id = in.readInt();
        descricao = Objects.requireNonNull(in.readString());
        aplicacao = Objects.requireNonNull(in.readString());
        origem = OrigemItem.valueOf(in.readString());
        isChecked = in.readByte() != 0;
    }

    public static final Creator<ItemMedicamentoUiState> CREATOR = new Creator<>() {
        @Override
        public ItemMedicamentoUiState createFromParcel(Parcel in) {
            return new ItemMedicamentoUiState(in);
        }

        @Override
        public ItemMedicamentoUiState[] newArray(int size) {
            return new ItemMedicamentoUiState[size];
        }
    };

    public int getId() {
        return id;
    }

    @NonNull
    public String getDescricao() {
        return descricao;
    }

    @NonNull
    public OrigemItem getOrigem() {
        return origem;
    }

    public boolean isChecked() {
        return isChecked;
    }

    @NonNull
    public String getAplicacao() {
        return aplicacao;
    }

    @NonNull
    public ItemMedicamentoUiState withChecked(boolean checked) {
        return new ItemMedicamentoUiState(id, descricao, aplicacao, origem, checked);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(descricao);
        dest.writeString(origem.name());
        dest.writeByte((byte) (isChecked ? 1 : 0));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemMedicamentoUiState)) return false;
        ItemMedicamentoUiState that = (ItemMedicamentoUiState) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}