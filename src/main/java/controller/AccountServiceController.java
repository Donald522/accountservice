package controller;


import model.Account;
import model.Transaction;
import service.AccountService;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;

/**
 * Created by Anton Tolkachev.
 * Since 08.07.2018
 */
@Path("/account")
public class AccountServiceController {

    private final AccountService accountService;

    @Inject
    public AccountServiceController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<String> getAllAccounts() {
        return accountService.getAllAccountIds();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Account getAccountById(@PathParam("id") String accountId) {
        return accountService.getAccountById(accountId);
    }

    @POST
    @Path("/create")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createAccount(@QueryParam("name") String name) {
        return doPost(accountService::createAccount, name);
    }

    @POST
    @Path("/transfer")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response doTransaction(@QueryParam("fromAccountId") String fromAccountId,
                              @QueryParam("toAccountId") String toAccountId,
                              @QueryParam("amount") double amount) {
        Transaction transaction = Transaction.builder()
                .id(UUID.randomUUID().getLeastSignificantBits())
                .fromAccountId(fromAccountId)
                .toAccountId(toAccountId)
                .amount(amount)
                .build();

        return doPost(accountService::doTransaction, transaction);
    }

    private <T, U> Response doPost(Function<T, U> function, T parameter) {
        try {
            U result = function.apply(parameter);
            return Response.status(200).entity(result).build();
        } catch (Exception e) {
            return Response.status(500).entity(e).build();
        }
    }

}
