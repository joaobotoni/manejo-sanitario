package com.omni.container.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;


import com.omni.container.data.entities.Protocolo;

import java.util.List;

@Dao
public interface ProtocoloDao {

    @Query("SELECT * FROM xgp_protocolo order by descricao ")
    List<Protocolo> getAll();

    @Query("SELECT * FROM xgp_protocolo WHERE id_protocolo = :idProtocolo")
    Protocolo findByIdProtocolo(int idProtocolo);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Protocolo protocolo);

    @Delete
    int delete(Protocolo protocolo);

    @Update
    int update(Protocolo protocolo);

    @Query("DELETE FROM xgp_protocolo")
    int deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Protocolo> protocolo);
}