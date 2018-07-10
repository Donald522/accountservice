package dao;

import model.Account;

import java.util.Collection;

/**
 * Created by Anton Tolkachev.
 * Since 09.07.2018
 */
public interface AccountStorage {

    void storeAccount(Account account);

    Account loadAccountById(String id);

    Collection<String> loadAllAccountIds();

}
