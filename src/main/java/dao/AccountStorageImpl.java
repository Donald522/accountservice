package dao;

import model.Account;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Created by Anton Tolkachev.
 * Since 09.07.2018
 */
public class AccountStorageImpl implements AccountStorage {

    private final Map<String, Account> accountCache = new ConcurrentHashMap<>();

    private final ThreadLocal<Map<String, Account>> threadLocalBuffer = new ThreadLocal<>();

    @Override
    public <T, U> U query(BiFunction<Map<String, Account>, T, U> command, T parameter) {
        return command.apply(accountCache, parameter);
    }

    @Override
    public <T> void execute(BiConsumer<Map<String, Account>, T> command, T parameter) {
        Map<String, Account> accountsToCommit = threadLocalBuffer.get();
        if (accountsToCommit == null) {
            accountsToCommit = new HashMap<>();
            threadLocalBuffer.set(accountsToCommit);
        }
        command.accept(accountsToCommit, parameter);
    }

    @Override
    public void commit() {
        Map<String, Account> accountsToCommit = threadLocalBuffer.get();
        if (accountsToCommit != null) {
            accountCache.putAll(accountsToCommit);
            accountsToCommit.clear();
        }
    }

    @Override
    public void rollback() {
        Map<String, Account> accountsToCommit = threadLocalBuffer.get();
        if (accountsToCommit != null) {
            accountsToCommit.clear();
        }
    }
}
