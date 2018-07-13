package service;

import com.google.common.collect.Lists;
import dao.AccountStorage;
import dao.AccountStorageImpl;
import model.Account;
import model.Transaction;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by Anton Tolkachev.
 * Since 13.07.2018
 */
public class AccountServiceIntegrationTest {

    private class TransactionTask implements Runnable {

        private static final double TRANSACTION_AMOUNT = 10d;

        private String fromAccountId;
        private String toAccountId;

        TransactionTask(String fromAccountId, String toAccountId) {
            this.fromAccountId = fromAccountId;
            this.toAccountId = toAccountId;
        }

        @Override
        public void run() {
            Transaction transaction = Transaction.builder()
                    .fromAccountId(fromAccountId)
                    .toAccountId(toAccountId)
                    .amount(TRANSACTION_AMOUNT).build();
            accountService.doTransaction(transaction);
        }
    }

    private AccountService accountService;

    private final ExecutorService executorService = Executors.newFixedThreadPool(8);

    private final Runnable createAccountTask = () -> accountService.createAccount(String.format("Fan%s", new Random().nextInt(100)));

    @Before
    public void setUp() {
        AccountStorage storage = new AccountStorageImpl();
        accountService = new AccountServiceImpl(storage);
    }

    @Test
    public void multipleTransactionToOneAccountInMultipleThreads() {
        String targetAccountHolder = "Noize MC";

        String targetAccountId = accountService.createAccount(targetAccountHolder);

        Collection<Future> futures = Lists.newArrayList();

        for (int i = 0; i < 10; ++i) {
            Future future = executorService.submit(createAccountTask);
            futures.add(future);
        }

        waitForTaskCompletion(futures);

        Collection<String> ids = accountService.getAllAccountIds();

        futures.clear();
        for (String id : ids) {
            TransactionTask task = new TransactionTask(id, targetAccountId);
            Future<?> future = executorService.submit(task);
            futures.add(future);
        }

        waitForTaskCompletion(futures);

        Account targetAccount = accountService.getAccountById(targetAccountId);

        assertThat(targetAccount.getAmount(), is(200d));

    }

    @Test
    public void nothingShouldChangedAfterSameTransactionsFromAndToTargetAccount() {
        String targetAccountHolder = "Donald Trump";

        String targetAccountId = accountService.createAccount(targetAccountHolder);

        Collection<Future> futures = Lists.newArrayList();

        for (int i = 0; i < 10; ++i) {
            Future future = executorService.submit(createAccountTask);
            futures.add(future);
        }

        waitForTaskCompletion(futures);

        Collection<String> ids = accountService.getAllAccountIds();

        futures.clear();
        for (String id : ids) {
            TransactionTask task = new TransactionTask(id, targetAccountId);
            Future<?> future = executorService.submit(task);
            futures.add(future);

            TransactionTask taskRevert = new TransactionTask(targetAccountId, id);
            Future<?> futureRevert = executorService.submit(taskRevert);
            futures.add(futureRevert);
        }

        waitForTaskCompletion(futures);

        for (String id : ids) {
            Account account = accountService.getAccountById(id);
            assertThat(account.getAmount(), is(100d));
        }

    }

    private void waitForTaskCompletion(Collection<Future> futures) {
        futures.forEach(future -> {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

}