package com.omni.container.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.omni.container.data.entities.ItemMedicamento;

import java.util.List;

@Dao
public interface ItemMedicamentoDao {

    @Query("SELECT * FROM xgp_item_medicamento")
    List<ItemMedicamento> getAll();
    @Query("SELECT * FROM xgp_item_medicamento WHERE id_item = :idItem")
    ItemMedicamento getByItem(int idItem);
    @Query("SELECT * FROM xgp_item_medicamento WHERE id_item IN (:idsItem)")
    List<ItemMedicamento> getMedicamentosByItens(List<Integer> idsItem);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ItemMedicamento itemMedicamento);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ItemMedicamento> itensMedicamento);

    @Update
    int update(ItemMedicamento itemMedicamento);

    @Query("DELETE FROM xgp_item_medicamento")
    int deleteAll();
}
