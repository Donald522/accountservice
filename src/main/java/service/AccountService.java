package service;

import model.Account;
import model.Transaction;

import java.util.Collection;

/**
 * Created by Anton Tolkachev.
 * Since 09.07.2018
 */
public interface AccountService {

    String createAccount(String name);

    Collection<String> getAllAccountIds();

    Account getAccountById(String id);

    long doTransaction(Transaction transaction);

}
