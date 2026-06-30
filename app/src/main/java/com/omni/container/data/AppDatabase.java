package com.omni.container.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.omni.container.data.dao.ItemDao;
import com.omni.container.data.dao.ItemMedicamentoDao;
import com.omni.container.data.dao.ProtocoloDao;
import com.omni.container.data.dao.ProtocoloItemDao;
import com.omni.container.data.dao.SanitarioDao;
import com.omni.container.data.dao.SanitarioDetDao;
import com.omni.container.data.entities.Item;
import com.omni.container.data.entities.ItemMedicamento;
import com.omni.container.data.entities.Protocolo;
import com.omni.container.data.entities.ProtocoloItem;
import com.omni.container.data.entities.Sanitario;
import com.omni.container.data.entities.SanitarioDet;

import org.jspecify.annotations.NonNull;

@Database(
        entities = {
                Protocolo.class,
                ProtocoloItem.class,
                Item.class,
                Sanitario.class,
                SanitarioDet.class,
                ItemMedicamento.class
        },
        version = 1
)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "Sample.db";

    private static volatile AppDatabase INSTANCE;

    public abstract ProtocoloDao protocoloDao();

    public abstract ProtocoloItemDao protocoloItemDao();

    public abstract ItemDao itemDao();

    public abstract SanitarioDao sanitarioDao();

    public abstract SanitarioDetDao sanitarioDetDao();

    public abstract ItemMedicamentoDao itemMedicamentoDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE != null) {
            return INSTANCE;
        }
        synchronized (AppDatabase.class) {
            if (INSTANCE == null) {
                INSTANCE = buildDatabase(context);
            }
        }
        return INSTANCE;
    }

    private static AppDatabase buildDatabase(final Context context) {
        return Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        DATABASE_NAME)
                .addCallback(new SeedCallback())
                .build();
    }

    private static final class SeedCallback extends RoomDatabase.Callback {

        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            seedItens(db);
            seedItensMedicamento(db);
            seedProtocolos(db);
            seedProtocoloItens(db);
        }

        private void seedItens(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("INSERT INTO xgp_item (id_item, cod_item, descricao, ativo, id_tipo_item, aplicacao) VALUES (1, 'RABUNE', 'Raiva - Rabune', 'S', 3, 'Vacina contra a raiva dos herbívoros (bovinos), aplicação subcutânea')");
            db.execSQL("INSERT INTO xgp_item (id_item, cod_item, descricao, ativo, id_tipo_item, aplicacao) VALUES (2, 'POLISTAR', 'Carbunculo - Polistar', 'S', 3, 'Vacina polivalente contra carbúnculo sintomático e clostridioses')");
            db.execSQL("INSERT INTO xgp_item (id_item, cod_item, descricao, ativo, id_tipo_item, aplicacao) VALUES (3, 'BIOPERSOL', 'Vermifugo Biopersol', 'S', 3, 'Vermífugo de amplo espectro para controle de vermes gastrointestinais')");
            db.execSQL("INSERT INTO xgp_item (id_item, cod_item, descricao, ativo, id_tipo_item, aplicacao) VALUES (4, 'BIOPOLIGEM', 'Pneumonia - Biopoligem', 'S', 3, 'Controle e prevenção de doenças respiratórias (pneumonia bovina)')");
            db.execSQL("INSERT INTO xgp_item (id_item, cod_item, descricao, ativo, id_tipo_item, aplicacao) VALUES (5, 'ACIENDEL', 'Pour on - Aciendel Plus', 'S', 3, 'Endectocida pour-on para controle de vermes, carrapatos e bernes')");
            db.execSQL("INSERT INTO xgp_item (id_item, cod_item, descricao, ativo, id_tipo_item, aplicacao) VALUES (6, 'MODPLUS', 'ModPlus', 'S', 3, 'Modificador metabólico para promoção de ganho de peso (respeitar carência de abate)')");
            db.execSQL("INSERT INTO xgp_item (id_item, cod_item, descricao, ativo, id_tipo_item, aplicacao) VALUES (7, 'LONGAMECTINA', 'Longamectina Premium 3,5%', 'S', 3, 'Endectocida injetável de longa ação contra endo e ectoparasitas')");
            db.execSQL("INSERT INTO xgp_item (id_item, cod_item, descricao, ativo, id_tipo_item, aplicacao) VALUES (8, 'ISOCOX', 'Isocox', 'S', 3, 'Anticoccidiano para prevenção e tratamento de coccidiose em bezerros')");
        }

        private void seedItensMedicamento(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("INSERT INTO xgp_item_medicamento (id_item, principio_ativo, carencia_abate, tipo_dosagem, qtde_dose, un_dose, observacao_uso, data_created, usuario_created, data_changed, usuario_changed) VALUES (1, 'Vírus da raiva inativado', 0, 'A', 2.0, 'mL', '2 mL / Animal - exemplo; confirmar carência e dose na bula/MAPA', 1735689600000, 'SEED', 1735689600000, 'SEED')");
            db.execSQL("INSERT INTO xgp_item_medicamento (id_item, principio_ativo, carencia_abate, tipo_dosagem, qtde_dose, un_dose, observacao_uso, data_created, usuario_created, data_changed, usuario_changed) VALUES (2, 'Antígenos clostridiais (carbúnculo sintomático)', 0, 'A', 5.0, 'mL', '5 mL / Animal - exemplo; confirmar carência e dose na bula/MAPA', 1735689600000, 'SEED', 1735689600000, 'SEED')");
            db.execSQL("INSERT INTO xgp_item_medicamento (id_item, principio_ativo, carencia_abate, tipo_dosagem, qtde_dose, un_dose, peso_base, observacao_uso, data_created, usuario_created, data_changed, usuario_changed) VALUES (3, 'Endoparasiticida (vermífugo)', 21, 'P', 1.0, 'mL', 50.0, '1 mL / 50 kg PV - exemplo; confirmar carência e dose na bula/MAPA', 1735689600000, 'SEED', 1735689600000, 'SEED')");
            db.execSQL("INSERT INTO xgp_item_medicamento (id_item, principio_ativo, carencia_abate, tipo_dosagem, qtde_dose, un_dose, peso_base, observacao_uso, data_created, usuario_created, data_changed, usuario_changed) VALUES (4, 'Antibacteriano para doenças respiratórias', 30, 'P', 1.0, 'mL', 50.0, '1 mL / 50 kg PV - exemplo; confirmar carência e dose na bula/MAPA', 1735689600000, 'SEED', 1735689600000, 'SEED')");
            db.execSQL("INSERT INTO xgp_item_medicamento (id_item, principio_ativo, carencia_abate, tipo_dosagem, qtde_dose, un_dose, peso_base, observacao_uso, data_created, usuario_created, data_changed, usuario_changed) VALUES (5, 'Endectocida pour-on (avermectina)', 35, 'P', 1.0, 'mL', 10.0, '1 mL / 10 kg PV - exemplo; confirmar carência e dose na bula/MAPA', 1735689600000, 'SEED', 1735689600000, 'SEED')");
            db.execSQL("INSERT INTO xgp_item_medicamento (id_item, principio_ativo, carencia_abate, tipo_dosagem, qtde_dose, un_dose, observacao_uso, data_created, usuario_created, data_changed, usuario_changed) VALUES (6, 'Modificador metabólico', 70, 'A', 8.0, 'mL', '8 mL / Animal - exemplo; confirmar carência e dose na bula/MAPA', 1735689600000, 'SEED', 1735689600000, 'SEED')");
            db.execSQL("INSERT INTO xgp_item_medicamento (id_item, principio_ativo, carencia_abate, tipo_dosagem, qtde_dose, un_dose, peso_base, observacao_uso, data_created, usuario_created, data_changed, usuario_changed) VALUES (7, 'Ivermectina 3,5%', 49, 'P', 1.0, 'mL', 50.0, '1 mL / 50 kg PV - exemplo; confirmar carência e dose na bula/MAPA', 1735689600000, 'SEED', 1735689600000, 'SEED')");
            db.execSQL("INSERT INTO xgp_item_medicamento (id_item, principio_ativo, carencia_abate, tipo_dosagem, qtde_dose, un_dose, peso_base, observacao_uso, data_created, usuario_created, data_changed, usuario_changed) VALUES (8, 'Anticoccidiano', 30, 'P', 1.0, 'mL', 25.0, '1 mL / 25 kg PV - exemplo; confirmar carência e dose na bula/MAPA', 1735689600000, 'SEED', 1735689600000, 'SEED')");
        }

        private void seedProtocolos(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("INSERT INTO xgp_protocolo (id_protocolo, descricao, aplicacao, responsavel, ativo, status_app) VALUES (8, 'Protocolo Recrias BS - Rafael Silveira - 09/2023', 'O', 'Rafael - Silveiria', 'S', 'A')");
            db.execSQL("INSERT INTO xgp_protocolo (id_protocolo, descricao, aplicacao, responsavel, ativo, status_app) VALUES (9, 'Modificador ModPlus', 'O', 'Matheus', 'S', 'A')");
            db.execSQL("INSERT INTO xgp_protocolo (id_protocolo, descricao, aplicacao, responsavel, ativo, status_app) VALUES (10, 'PROTOCOLO ENTRADA CONFINAMENTO', 'O', 'WELDMAN', 'S', 'A')");
            db.execSQL("INSERT INTO xgp_protocolo (id_protocolo, descricao, aplicacao, responsavel, ativo, status_app) VALUES (11, 'Entrada RIP', 'O', 'Baretta', 'S', 'A')");
            db.execSQL("INSERT INTO xgp_protocolo (id_protocolo, descricao, aplicacao, responsavel, ativo, status_app) VALUES (12, 'Protocolo Convencional', '', 'BARETTA', 'S', 'A')");
        }
        private void seedProtocoloItens(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, id_item, descricao, ordem) VALUES (8, 1, 1, 'Raiva em todos os animais', 1)");
            db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, id_item, descricao, ordem) VALUES (8, 2, 7, 'Vermifugo em todos os animais, longa duração, Longamectina Premium 3,5%', 2)");
            db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, id_item, descricao, ordem) VALUES (8, 3, 8, 'Isocox - coccidiose é uma doença que atinge principalmente os bezerros. Ela é causada por um protozoário e pode causar grandes danos ao rebanho. Bezerros abaixo de 140 kg', 3)");
            db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, id_item, descricao, ordem, prazo_carencia) VALUES (9, 1, 6, 'ModPlus (5ml Bezerro, 8ml Garrote, 10ml Boi)', 1, 70)");
            db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, id_tipo_manejo, id_item, descricao, ordem, qtde_dias, prazo_carencia) VALUES (10, 1, 45, 3, 'Vermifugo Biopersol', 1, 0, 21)");
            db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, id_tipo_manejo, id_item, descricao, ordem) VALUES (10, 2, 45, 2, 'Carbunculo - Polistar', 2)");
            db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, id_tipo_manejo, id_item, descricao, ordem) VALUES (10, 3, 45, 1, 'Raiva - Rabune', 3)");
            db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, id_tipo_manejo, id_item, descricao, ordem) VALUES (10, 4, 45, 4, 'Pneumonia - Biopoligem', 4)");
            db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, id_tipo_manejo, id_item, descricao, ordem) VALUES (10, 5, 45, 5, 'Pour on - Aciendel Plus', 5)");
            db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, id_tipo_manejo, id_item, descricao, ordem, qtde_dias, prazo_carencia) VALUES (11, 1, 45, 3, 'Vermifugo Biopersol', 1, 0, 21)");
            db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, id_tipo_manejo, id_item, descricao, ordem) VALUES (11, 2, 45, 2, 'Carbunculo - Polistar', 2)");
            db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, id_tipo_manejo, id_item, descricao, ordem) VALUES (11, 3, 45, 1, 'Raiva - Rabune', 3)");
            db.execSQL("INSERT INTO xgp_protocolo_item (id_protocolo, id_protocolo_item, id_tipo_manejo, id_item, descricao, ordem) VALUES (11, 4, 45, 5, 'Pour on - Aciendel Plus', 4)");
        }
    }
}