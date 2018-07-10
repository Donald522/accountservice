package runner;

import dao.AccountStorage;
import dao.AccountStorageImpl;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import service.AccountService;
import service.AccountServiceImpl;

import javax.inject.Singleton;

/**
 * Created by Anton Tolkachev.
 * Since 09.07.2018
 */
public class ApplicationBinder extends AbstractBinder {
    @Override
    protected void configure() {
        bind(AccountServiceImpl.class).to(AccountService.class).in(Singleton.class);
        bind(AccountStorageImpl.class).to(AccountStorage.class).in(Singleton.class);
    }
}
