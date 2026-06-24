package com.omni.container.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;


import com.omni.container.data.entities.ProtocoloItem;

import java.util.List;

@Dao
public interface ProtocoloItemDao  {

    @Query("SELECT * FROM xgp_protocolo_item")
    List<ProtocoloItem> getAll();

    @Query("SELECT * FROM xgp_protocolo_item " +
            "WHERE id_protocolo = :idProtocolo " +
            "  and id_protocolo_item = :id_protocolo_item")
    ProtocoloItem findByPk(int idProtocolo, int id_protocolo_item);

    @Query("SELECT * FROM xgp_protocolo_item " +
            "WHERE id_protocolo = :idProtocolo ")
    ProtocoloItem findByIdProtocolo(int idProtocolo);

    @Query("SELECT * FROM xgp_protocolo_item " +
            "WHERE id_protocolo = :idProtocolo ")
    List<ProtocoloItem> getAllByIdProtocolo(int idProtocolo);

    @Query("SELECT * FROM xgp_protocolo_item " +
            "WHERE id_protocolo = :idProtocolo " +
            "  and id_tipo_manejo = :id_tipo_manejo " +
            " order by ordem ")
    ProtocoloItem findByIdTipoManejo(int idProtocolo, int id_tipo_manejo);

    @Query("SELECT * FROM xgp_protocolo_item WHERE id_item = :idItem")
    ProtocoloItem findItemProtocoloByIdItem(int idItem);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ProtocoloItem protocoloItem);

    @Delete
    int delete(ProtocoloItem protocoloItem);

    @Update
    int update(ProtocoloItem protocoloItem);

    @Query("DELETE FROM xgp_protocolo_item")
    int deleteAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ProtocoloItem> protocoloItemList);

}