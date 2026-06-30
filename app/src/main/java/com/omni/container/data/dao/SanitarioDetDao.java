package com.omni.container.data.dao;

import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.omni.container.data.entities.Item;
import com.omni.container.data.entities.Sanitario;
import com.omni.container.data.entities.SanitarioDet;

import java.util.List;

@Dao
public interface SanitarioDetDao {
    @Query("SELECT * FROM xgp_sanitario_det")
    List<SanitarioDet> getAll();
    @Query("SELECT * FROM xgp_sanitario_det WHERE id_sanitario_det = :id")
    SanitarioDet findById(int id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SanitarioDet> sanitarioDets);
    @Query("DELETE FROM xgp_sanitario_det")
    int deleteAll();
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(SanitarioDet sanitarioDet);
    @Delete
    int delete(SanitarioDet sanitarioDet);
    @Update
    int update(SanitarioDet sanitarioDet);
}
