package dao;

import model.Account;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Created by Anton Tolkachev.
 * Since 09.07.2018
 */
public interface AccountStorage {

    <T, U> U query(BiFunction<Map<String, Account>, T, U> command, T parameter);

    <T> void execute(BiConsumer<Map<String, Account>, T> command, T parameter);

    void commit();

    void rollback();

}
