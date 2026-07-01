# DbExecutor

Classe estática aninhada de `ManejoSanitarioFragment`, responsável por executar operações de banco de dados (Room/DAO) fora da main thread e entregar o resultado de volta na main thread, com proteção contra callbacks em uma `View` já destruída.

## Responsabilidade

- Rodar chamadas de DAO em um `ExecutorService` próprio (thread pool).
- Publicar o resultado (sucesso ou erro) na main thread via `Handler`.
- Impedir que callbacks executem depois que o Fragment já perdeu sua view (`onDestroyView`), evitando `NullPointerException`.

## Ciclo de vida

`DbExecutor` deve ser criado em `onViewCreated` (ou `onCreate`) e **fechado obrigatoriamente** em `onDestroyView`, chamando `close()`. Isso marca a instância como cancelada e desliga o `ExecutorService`, impedindo que qualquer callback pendente chegue à main thread.

```java
private void setupExecutor() {
    executor = new DbExecutor(Executors.newFixedThreadPool(THREAD_POOL_SIZE), createMainThreadHandler());
}

@Override
public void onDestroyView() {
    super.onDestroyView();
    executor.close();
}
```

## Construtor

```java
DbExecutor(@NonNull ExecutorService executor, @NonNull Handler handler)
```

| Parâmetro | Descrição |
|---|---|
| `executor` | Thread pool onde as tarefas de I/O rodam. |
| `handler` | Handler da main thread usado para postar os resultados de volta. |

## API pública

### `execute` — três sobrecargas

Todas resolvem o DAO a partir do `AppDatabase` internamente, então quem chama nunca precisa lidar com `AppDatabase.getDatabase(context)` manualmente.

**1. Consulta sem parâmetro**

```java
<D, E> void execute(@NonNull Context context,
                     @NonNull Function<AppDatabase, D> daoExtractor,
                     @NonNull Function<D, E> query,
                     @NonNull Consumer<E> onSuccess,
                     @NonNull Consumer<Exception> onError)
```

Uso típico — buscar todos os registros:

```java
executor.execute(requireContext(), AppDatabase::protocoloDao,
        ProtocoloDao::getAll, onSuccess, this::handleFetchProtocolosError);
```

**2. Consulta com um parâmetro e retorno**

```java
<D, P, E> void execute(@NonNull Context context,
                        @NonNull Function<AppDatabase, D> daoExtractor,
                        @NonNull BiFunction<D, P, E> query,
                        @NonNull P param,
                        @NonNull Consumer<E> onSuccess,
                        @NonNull Consumer<Exception> onError)
```

Uso típico — buscar itens de um protocolo específico:

```java
executor.execute(requireContext(), AppDatabase::itemDao,
        ItemDao::getAllItemsByProtocolo, protocolo.getId(),
        this::handleFetchItensProtocoloSuccess, this::handleFetchItensProtocoloError);
```

**3. Operação com um parâmetro e sem retorno (`void`)**

```java
<D, P> void execute(@NonNull Context context,
                     @NonNull Function<AppDatabase, D> daoExtractor,
                     @NonNull BiConsumer<D, P> query,
                     @NonNull P param,
                     @NonNull Runnable onSuccess,
                     @NonNull Consumer<Exception> onError)
```

Uso típico — inserir uma lista de registros:

```java
executor.execute(requireContext(), AppDatabase::sanitarioDetDao,
        SanitarioDetDao::insertAll, detalhes,
        this::handleSaveSuccess, this::handleSaveError);
```

### `close()`

```java
public synchronized void close()
```

Marca o executor como cancelado (`cancelled = true`) e chama `executor.shutdown()`. Depois de `close()`, nenhum `onSuccess`/`onError` pendente é entregue, mesmo que a tarefa em background já tenha terminado — o `post()` interno verifica `isCancelled()` antes de agendar qualquer callback na main thread.

## Métodos internos (privados)

| Método | Papel |
|---|---|
| `submit(Callable, Consumer, Consumer)` | Envia uma tarefa que **retorna valor** para o thread pool. |
| `submit(Runnable, Runnable, Consumer)` | Envia uma tarefa **sem retorno** para o thread pool. |
| `resolveDao(Context, Function)` | Obtém a instância do `AppDatabase` e extrai o DAO desejado. |
| `runTask(Callable, Consumer, Consumer)` | Executa a tarefa na background thread, capturando exceções, e posta o resultado (sucesso ou erro) na main thread. |
| `runTask(Runnable, Runnable, Consumer)` | Variante para tarefas `void` — internamente converte o `Runnable` em `Callable<Void>` via `toCallable` e delega para a versão com `Callable`. |
| `toCallable(Runnable)` | Adapta um `Runnable` para `Callable<Void>`, permitindo reusar o mesmo caminho de execução/tratamento de erro para os dois tipos de tarefa. |
| `post(Runnable)` | Publica uma ação na main thread via `Handler`, mas só se o executor ainda não tiver sido cancelado. |
| `runIfActive(Runnable)` | Segunda checagem de `isCancelled()`, feita já dentro da main thread — protege contra a janela de tempo entre o `post()` ser agendado e o `close()` ser chamado antes dele rodar. |
| `isCancelled()` | Lê a flag `volatile boolean cancelled`. |

## Por que duas checagens de `cancelled`?

`post()` verifica `isCancelled()` antes de agendar o `Runnable` no `Handler`, e `runIfActive()` verifica de novo quando esse `Runnable` efetivamente executa na main thread. Isso existe porque `close()` pode ser chamado (na main thread, tipicamente em `onDestroyView`) **entre** o momento em que uma tarefa em background terminou e chamou `post()` e o momento em que a main thread processa a mensagem enfileirada no `Handler`. A segunda checagem fecha essa janela de corrida.

## Thread-safety

- `cancelled` é `volatile`, garantindo visibilidade da flag entre a thread do pool e a main thread sem precisar de bloqueio explícito nas leituras.
- `close()` é `synchronized` para evitar chamadas concorrentes de `close()` deixando o `ExecutorService` em estado inconsistente (embora `shutdown()` já seja idempotente por si só).

## Motivo de ser uma classe aninhada e não uma classe própria

Segue a convenção do projeto de manter infraestrutura específica de um Fragment (que não é reutilizada por outras telas) como classe estática aninhada, evitando poluir o pacote com uma classe de propósito único e mantendo o acoplamento explícito com o ciclo de vida do Fragment que a instancia.