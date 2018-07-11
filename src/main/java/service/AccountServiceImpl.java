package service;

import com.google.common.util.concurrent.Striped;
import dao.AccountStorage;
import lombok.extern.slf4j.Slf4j;
import model.Account;
import model.Transaction;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Created by Anton Tolkachev.
 * Since 09.07.2018
 */
@Slf4j
public class AccountServiceImpl implements AccountService {

    private static final String DEFAULT_CURRENCY = "EUR";
    private static final double INITIAL_AMOUNT = 100.0;

    private static final BiConsumer<Map<String, Account>, Collection<Account>> INSERT_ACCOUNTS = (map, accounts) -> accounts.forEach(acc -> map.put(acc.getId(), acc));

    private static final BiFunction<Map<String, Account>, Void, Collection<String>> LOAD_ALL_ACCOUNT_IDS = (map, placeHolder) -> map.keySet();

    private static final BiFunction<Map<String, Account>, String, Account> LOAD_ACCOUNT_BY_ID = Map::get;

    private final Striped<Lock> accountLocks = Striped.lazyWeakLock(2);

    private final AccountStorage accountStorage;

    @Inject
    public AccountServiceImpl(AccountStorage accountStorage) {
        this.accountStorage = accountStorage;
    }

    public String createAccount(String name) {
        Account account = Account.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .currency(DEFAULT_CURRENCY)
                .amount(INITIAL_AMOUNT)
                .build();

        accountStorage.execute(INSERT_ACCOUNTS, Collections.singletonList(account));
        accountStorage.commit();
        log.info("New account created: {}", account);
        return account.getId();
    }

    @Override
    public Collection<String> getAllAccountIds() {
        log.info("Loading all ids from storage");
        return accountStorage.query(LOAD_ALL_ACCOUNT_IDS, null);
    }

    @Override
    public Account getAccountById(String id) {
        Account account = accountStorage.query(LOAD_ACCOUNT_BY_ID, id);
        log.info("Found account for [id={}]: {}", id, account);
        return account;
    }

    @Override
    public long doTransaction(Transaction transaction) {
        Account fromAccount = accountStorage.query(LOAD_ACCOUNT_BY_ID, transaction.getFromAccountId());
        Account toAccount = accountStorage.query(LOAD_ACCOUNT_BY_ID, transaction.getToAccountId());
        if (Double.compare(fromAccount.getAmount(), transaction.getAmount()) < 0) {
            log.info("There is no enough money on the account: {}. Transaction cancelled.", fromAccount.getId());
            return transaction.getId();
        }
        Lock fromAccountLock = accountLocks.get(fromAccount.getId());
        Lock toAccountLock = accountLocks.get(toAccount.getId());
        while (true) {
            if (fromAccountLock.tryLock()) {
                if (toAccountLock.tryLock()) {
                    try {
                        log.info("Start transaction: {}", transaction);

                        Account fromAccountAfterTransaction = fromAccount.toBuilder()
                                .amount(fromAccount.getAmount() - transaction.getAmount())
                                .build();
                        Account toAccountAfterTransaction = toAccount.toBuilder()
                                .amount(toAccount.getAmount() + transaction.getAmount())
                                .build();

                        accountStorage.execute(INSERT_ACCOUNTS, Arrays.asList(fromAccountAfterTransaction, toAccountAfterTransaction));
                        accountStorage.commit();
                        break;
                    } catch (Exception e) {
                        log.error("Error occurred during accounts updating. Rolling back transaction: id = {}", transaction.getId(), e);
                        accountStorage.rollback();
                        throw new RuntimeException(e);
                    } finally {
                        fromAccountLock.unlock();
                        toAccountLock.unlock();
                    }
                }
            }
        }
        log.info("End transaction: id = {}", transaction.getId());
        return transaction.getId();
    }
}
