package dao;

import model.Account;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Anton Tolkachev.
 * Since 09.07.2018
 */
public class AccountStorageImpl implements AccountStorage {

    private final Map<String, Account> accountCache = new ConcurrentHashMap<>();


    public void storeAccount(Account account) {
        accountCache.put(account.getId(), account);
    }

    public Account loadAccountById(String id) {
        return accountCache.get(id);
    }

    public Collection<String> loadAllAccountIds() {
        return accountCache.keySet();
    }
}
