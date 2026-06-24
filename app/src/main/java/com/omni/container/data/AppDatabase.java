package com.omni.container.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.omni.container.data.dao.ProtocoloDao;
import com.omni.container.data.dao.ProtocoloItemDao;
import com.omni.container.data.entities.Protocolo;
import com.omni.container.data.entities.ProtocoloItem;

import org.jspecify.annotations.NonNull;


@Database(entities = {
        Protocolo.class,
        ProtocoloItem.class
}, version = 1)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;

    public abstract ProtocoloDao protocoloDao();

    public abstract ProtocoloItemDao protocoloItemDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "Sample.db")
                            .addCallback(new Callback() {
                                @Override
                                public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                    super.onCreate(db);
                                    db.execSQL("INSERT INTO xgp_protocolo (id_protocolo, descricao, aplicacao, responsavel, ativo, status_app) VALUES (8, 'Protocolo Recrias BS - Rafael Silveira - 09/2023', 'O', 'Rafael - Silveiria', 'S', 'A')");
                                    db.execSQL("INSERT INTO xgp_protocolo (id_protocolo, descricao, aplicacao, responsavel, ativo, status_app) VALUES (9, 'Modificador ModPlus', 'O', 'Matheus', 'S', 'A')");
                                    db.execSQL("INSERT INTO xgp_protocolo (id_protocolo, descricao, aplicacao, responsavel, ativo, status_app) VALUES (10, 'PROTOCOLO ENTRADA CONFINAMENTO', 'O', 'WELDMAN', 'S', 'A')");
                                    db.execSQL("INSERT INTO xgp_protocolo (id_protocolo, descricao, aplicacao, responsavel, ativo, status_app) VALUES (11, 'Entrada RIP', 'O', 'Baretta', 'S', 'A')");
                                    db.execSQL("INSERT INTO xgp_protocolo (id_protocolo, descricao, aplicacao, responsavel, ativo, status_app) VALUES (12, 'Protocolo Convencional', '', 'BARETTA', 'S', 'A')");


                                    db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, descricao, ordem) VALUES (8, 1, 'Raiva em todos os animais', 1)");
                                    db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, descricao, ordem) VALUES (8, 2, 'Vermifugo em todos os animais, longa duração, Longamectina Premium 3,5%', 2)");
                                    db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, descricao, ordem) VALUES (8, 3, 'Isocox - coccidiose é uma doença que atinge principalmente os bezerros. Ela é causada por um protozoário e pode causar grandes danos ao rebanho. Bezerros abaixo de 140 kg', 3)");
                                    db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, descricao, ordem, prazo_carencia) VALUES (9, 1, 'ModPlus (5ml Bezerro, 8ml Garrote, 10ml Boi)', 1, 70)");
                                    db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, id_tipo_manejo, descricao, ordem, qtde_dias, prazo_carencia) VALUES (10, 1, 45, 'Vermifugo Biopersol', 1, 0, 21)");
                                    db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, id_tipo_manejo, descricao, ordem) VALUES (10, 2, 45, 'Carbunculo - Polistar', 2)");
                                    db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, id_tipo_manejo, descricao, ordem) VALUES (10, 3, 45, 'Raiva - Rabune', 3)");
                                    db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, id_tipo_manejo, descricao, ordem) VALUES (10, 4, 45, 'Pneumonia - Biopoligem', 4)");
                                    db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, id_tipo_manejo, descricao, ordem) VALUES (10, 5, 45, 'Pour on - Aciendel Plus', 5)");
                                    db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, id_tipo_manejo, descricao, ordem, qtde_dias, prazo_carencia) VALUES (11, 1, 45, 'Vermifugo Biopersol', 1, 0, 21)");
                                    db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, id_tipo_manejo, descricao, ordem) VALUES (11, 2, 45, 'Carbunculo - Polistar', 2)");
                                    db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, id_tipo_manejo, descricao, ordem) VALUES (11, 3, 45, 'Raiva - Rabune', 3)");
                                    db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, id_tipo_manejo, descricao, ordem) VALUES (11, 4, 45, 'Pour on - Aciendel Plus', 4)");
                                }
                            })
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}