package com.omni.container.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;


import com.omni.container.data.entities.Item;

import java.util.List;

@Dao
public interface ItemDao {
    @Query("SELECT * FROM xgp_item order by descricao")
    List<Item> getAll();
    @Query("SELECT * FROM xgp_item WHERE id_item = :idItem")
    Item findById(int idItem);
    @Query("SELECT * FROM xgp_item WHERE descricao like :descricao")
    Item findByDescricao(String descricao);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Item> item);
    @Query("DELETE FROM xgp_item")
    int deleteAll();
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Item item);
    @Delete
    int delete(Item item);

    @Update
    int update(Item item);
}
