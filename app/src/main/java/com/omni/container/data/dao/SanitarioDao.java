package com.omni.container.data.dao;

import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Update;

import com.omni.container.data.entities.Item;
import com.omni.container.data.entities.Sanitario;

import java.util.Date;
import java.util.List;

@Dao
public interface SanitarioDao {
    @Query("SELECT * FROM xgp_sanitario")
    List<Sanitario> getAll();
    @Query("SELECT * FROM xgp_sanitario WHERE id_sanitario = :id")
    Sanitario findById(int id);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Sanitario> sanitario);
    @Query("DELETE FROM xgp_sanitario")
    int deleteAll();
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Sanitario sanitario);
    @Delete
    int delete(Sanitario sanitario);
    @Update
    int update(Sanitario sanitario);
}
