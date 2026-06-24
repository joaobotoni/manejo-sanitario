package com.omni.container.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.omni.container.R;
import com.omni.container.data.AppDatabase;

import java.io.Closeable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;


public class ConsultaMedicamentoFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_consulta_medicamento, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


    private void init() {
        setupViews();
        setupClickListeners();
    }


    private void destroy() {

    }

    private void setupViews() {

    }

    private void setupClickListeners() {
    }

    private void navegarParaTraz() {
        getParentFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .replace(R.id.fragment_container_view, new ManejoSanitarioFragment())
                .commit();
    }

    public static void setupVerticalRecyclerView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.Adapter<?> adapter, @NonNull Context context) {
        setupRecyclerView(recyclerView, adapter, context, LinearLayoutManager.VERTICAL);
    }

    private static void setupRecyclerView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.Adapter<?> adapter, @NonNull Context context, int orientation) {
        recyclerView.setLayoutManager(new LinearLayoutManager(context, orientation, false));
        recyclerView.setAdapter(adapter);
    }


    private static final class Executor implements Closeable {

        private final Handler handler;
        private final ExecutorService executor;
        private volatile boolean cancelled = false;

        Executor(@NonNull ExecutorService executor, @NonNull Handler handler) {
            this.executor = executor;
            this.handler = handler;
        }

        public <T> void execute(@NonNull Callable<T> task, @NonNull Consumer<T> onSuccess, @NonNull Consumer<Exception> onError) {
            executor.submit(() -> runTask(task, onSuccess, onError));
        }

        public <D, E> void execute(@NonNull Context context,
                                   @NonNull Function<AppDatabase, D> daoExtractor,
                                   @NonNull Function<D, E> query,
                                   @NonNull Consumer<E> onSuccess,
                                   @NonNull Consumer<Exception> onError) {
            executor.submit(() -> runWithDao(context, daoExtractor, query, onSuccess, onError));
        }

        public <D, P, E> void execute(@NonNull Context context,
                                      @NonNull Function<AppDatabase, D> daoExtractor,
                                      @NonNull BiFunction<D, P, E> query,
                                      @NonNull P param,
                                      @NonNull Consumer<E> onSuccess,
                                      @NonNull Consumer<Exception> onError) {
            executor.submit(() -> runWithDaoAndParam(context, daoExtractor, query, param, onSuccess, onError));
        }

        private <T> void runTask(@NonNull Callable<T> task,
                                 @NonNull Consumer<T> onSuccess,
                                 @NonNull Consumer<Exception> onError) {
            try {
                T result = task.call();
                post(() -> onSuccess.accept(result));
            } catch (Exception e) {
                post(() -> onError.accept(e));
            }
        }

        private <D, E> void runWithDao(@NonNull Context context,
                                       @NonNull Function<AppDatabase, D> daoExtractor,
                                       @NonNull Function<D, E> query,
                                       @NonNull Consumer<E> onSuccess,
                                       @NonNull Consumer<Exception> onError) {
            try (Data<D> data = Data.of(daoExtractor)) {
                E result = query.apply(data.get(context));
                post(() -> onSuccess.accept(result));
            } catch (Exception e) {
                post(() -> onError.accept(e));
            }
        }

        private <D, P, E> void runWithDaoAndParam(@NonNull Context context,
                                                  @NonNull Function<AppDatabase, D> daoExtractor,
                                                  @NonNull BiFunction<D, P, E> query,
                                                  @NonNull P param,
                                                  @NonNull Consumer<E> onSuccess,
                                                  @NonNull Consumer<Exception> onError) {
            try (Data<D> data = Data.of(daoExtractor)) {
                E result = query.apply(data.get(context), param);
                post(() -> onSuccess.accept(result));
            } catch (Exception e) {
                post(() -> onError.accept(e));
            }
        }

        private void post(@NonNull Runnable action) {
            if (isCancelled()) return;
            handler.post(() -> runIfActive(action));
        }

        private void runIfActive(@NonNull Runnable action) {
            if (isCancelled()) return;
            action.run();
        }

        private boolean isCancelled() {
            return cancelled;
        }

        @Override
        public synchronized void close() {
            cancelled = true;
            executor.shutdown();
        }
    }

    private static class Data<T> implements Closeable {

        private volatile AppDatabase database;
        private Function<AppDatabase, T> extractor;
        private volatile boolean closed = false;

        private Data(@NonNull Function<AppDatabase, T> extractor) {
            this.extractor = extractor;
        }

        static <T> Data<T> of(@NonNull Function<AppDatabase, T> extractor) {
            return new Data<>(extractor);
        }

        private synchronized T get(@NonNull Context context) {
            if (closed) {
                throw new IllegalStateException("Recurso já foi fechado.");
            }
            if (database == null) {
                database = AppDatabase.getDatabase(context.getApplicationContext());
            }
            return extractor.apply(database);
        }


        @Override
        public synchronized void close() {
            closed = true;
            database = null;
            extractor = null;
        }
    }
}
