package com.omni.container.ui.states;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Objects;

public final class ProtocoloItemUiState implements Parcelable {
    private final int id;
    @NonNull
    private final String descricao;
    @NonNull
    private final OrigemItem origem;
    private final boolean isChecked;

    public ProtocoloItemUiState(int id, @NonNull String descricao, @NonNull OrigemItem origem, boolean isChecked) {
        this.id = id;
        this.descricao = descricao;
        this.origem = origem;
        this.isChecked = isChecked;
    }

    protected ProtocoloItemUiState(Parcel in) {
        id = in.readInt();
        descricao = in.readString();
        origem = OrigemItem.valueOf(in.readString());
        isChecked = in.readByte() != 0;
    }

    public static final Creator<ProtocoloItemUiState> CREATOR = new Creator<ProtocoloItemUiState>() {
        @Override
        public ProtocoloItemUiState createFromParcel(Parcel in) {
            return new ProtocoloItemUiState(in);
        }

        @Override
        public ProtocoloItemUiState[] newArray(int size) {
            return new ProtocoloItemUiState[size];
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
    public ProtocoloItemUiState withChecked(boolean checked) {
        return new ProtocoloItemUiState(id, descricao, origem, checked);
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
        if (!(o instanceof ProtocoloItemUiState)) return false;
        ProtocoloItemUiState that = (ProtocoloItemUiState) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}