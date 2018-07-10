package service;

import com.google.common.util.concurrent.Striped;
import dao.AccountStorage;
import lombok.extern.slf4j.Slf4j;
import model.Account;
import model.Transaction;

import javax.inject.Inject;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

/**
 * Created by Anton Tolkachev.
 * Since 09.07.2018
 */
@Slf4j
public class AccountServiceImpl implements AccountService {

    private static final String DEFAULT_CURRENCY = "EUR";
    private static final double INITIAL_AMOUNT = 100.0;

    private final Striped<Lock> accountLocks = Striped.lazyWeakLock(2);

    private final AccountStorage accountStorage;

    @Inject
    public AccountServiceImpl(AccountStorage accountStorage) {
        this.accountStorage = accountStorage;
    }

    public void createAccount(String name) {
        Account account = Account.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .currency(DEFAULT_CURRENCY)
                .amount(INITIAL_AMOUNT)
                .build();

        accountStorage.storeAccount(account);
        log.info("New account created: {}", account);
    }

    @Override
    public Collection<String> getAllAccountIds() {
        log.info("Loading all ids from storage");
        return accountStorage.loadAllAccountIds();
    }

    @Override
    public Account getAccountById(String id) {
        Account account = accountStorage.loadAccountById(id);
        log.info("Found account for [id={}]: {}", id, account);
        return account;
    }

    @Override
    public void doTransaction(Transaction transaction) {
        Account fromAccount = accountStorage.loadAccountById(transaction.getFromAccountId());
        Account toAccount = accountStorage.loadAccountById(transaction.getToAccountId());
        if (Double.compare(fromAccount.getAmount(), transaction.getAmount()) < 0) {
            log.info("There is no enough money on the account: {}", fromAccount.getId());
            return;
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
                        accountStorage.storeAccount(fromAccountAfterTransaction);
                        accountStorage.storeAccount(toAccountAfterTransaction);
                        break;
                    } finally {
                        fromAccountLock.unlock();
                        toAccountLock.unlock();
                    }
                }
            }
        }
        log.info("End transaction: id = {}", transaction.getId());
    }
}
