package controller;

import model.Account;
import model.Transaction;
import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import service.AccountService;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;

/**
 * Created by Anton Tolkachev.
 * Since 12.07.2018
 */
public class AccountServiceControllerTest extends JerseyTest {

    private static final String ACCOUNT_ID = "acc-id-12345";
    private static final String ACCOUNT_ID_1 = "acc-id-1996";
    private static final String ACCOUNT_HOLDER_NAME = "Anton";
    private static final String SERVER_API_ENDPOINT = "http://localhost:9998/account";
    private static final Long TRANSACTION_ID = 2636633L;
    private static final Account ACCOUNT = Account.builder().name(ACCOUNT_HOLDER_NAME).id(ACCOUNT_ID).build();

    public static class MockAccountServiceFactory implements Factory<AccountService> {
        @Override
        public AccountService provide() {
            final AccountService mockedService = Mockito.mock(AccountService.class);
            Mockito.when(mockedService.createAccount(ACCOUNT_HOLDER_NAME))
                    .thenAnswer(invocation -> ACCOUNT_ID);
            Mockito.when(mockedService.doTransaction(any()))
                    .thenAnswer(invocation -> TRANSACTION_ID);
            Mockito.when(mockedService.getAccountById(ACCOUNT_ID))
                    .thenAnswer(invocation -> ACCOUNT);
            Mockito.when(mockedService.getAllAccountIds())
                    .thenAnswer(invocation -> Arrays.asList(ACCOUNT_ID, ACCOUNT_ID_1));
            return mockedService;
        }

        @Override
        public void dispose(AccountService t) {}
    }

    @Override
    public Application configure() {
        AbstractBinder binder = new AbstractBinder() {
            @Override
            protected void configure() {
                bindFactory(MockAccountServiceFactory.class)
                        .to(AccountService.class);
            }
        };
        ResourceConfig config = new ResourceConfig(AccountServiceController.class);
        config.register(binder);
        return config;
    }

    @Test
    public void testCreateAccount() {
        Client client = ClientBuilder.newClient();
        Response response = client
                .target(SERVER_API_ENDPOINT)
                .path("create")
                .request()
                .post(Entity.entity(ACCOUNT_HOLDER_NAME, MediaType.APPLICATION_JSON));

        Assert.assertEquals(200, response.getStatus());

        String respBody = response.readEntity(String.class);
        Assert.assertEquals(ACCOUNT_ID, respBody);

        response.close();
        client.close();
    }

    @Test
    public void testDoTransaction() {
        Transaction transaction = Transaction.builder()
                .fromAccountId("acc-from-id-111")
                .toAccountId("acc-to-id-445")
                .amount(200d)
                .build();

        Client client = ClientBuilder.newClient();
        Response response = client
                .target(SERVER_API_ENDPOINT)
                .path("transfer")
                .request()
                .post(Entity.entity(transaction, MediaType.APPLICATION_JSON));

        Assert.assertEquals(200, response.getStatus());

        Long respBody = response.readEntity(Long.class);
        Assert.assertEquals(TRANSACTION_ID, respBody);

        response.close();
        client.close();
    }

    @Test
    public void testGetAccountById() {
        Client client = ClientBuilder.newClient();
        Response response = client
                .target(SERVER_API_ENDPOINT)
                .path(ACCOUNT_ID)
                .request()
                .get();

        Assert.assertEquals(200, response.getStatus());

        Account account = response.readEntity(Account.class);
        Assert.assertEquals(ACCOUNT_HOLDER_NAME, account.getName());

        response.close();
        client.close();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGetAllAccountIds() {
        Client client = ClientBuilder.newClient();
        Response response = client
                .target(SERVER_API_ENDPOINT)
                .path("all")
                .request()
                .get();

        Assert.assertEquals(200, response.getStatus());

        Collection<String> ids = response.readEntity(Collection.class);

        assertThat(ids, hasItem(ACCOUNT_ID));
        assertThat(ids, hasItem(ACCOUNT_ID_1));

        response.close();
        client.close();
    }

}