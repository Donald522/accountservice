package service;

import model.Account;
import model.Transaction;

import java.util.Collection;

/**
 * Created by Anton Tolkachev.
 * Since 09.07.2018
 */
public interface AccountService {

    void createAccount(String name);

    Collection<String> getAllAccountIds();

    Account getAccountById(String id);

    void doTransaction(Transaction transaction);

}
