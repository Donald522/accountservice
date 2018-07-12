package client;

import model.Account;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Anton Tolkachev.
 * Since 13.07.2018
 */
public class ClientRunner {

    public static void main(String[] args) {
        Client client = ClientBuilder.newClient();

        Response create = client
                .target("http://localhost:8080/account")
                .path("create")
                .request()
                .post(Entity.entity("Anton Tolkachev", MediaType.APPLICATION_JSON));

        System.out.println(create);

        String id = create.readEntity(String.class);

        System.out.println(id);

        Response response = client
                .target("http://localhost:8080/account")
                .path(id)
                .request()
                .get();

        Account account = response.readEntity(Account.class);

        System.out.println(account);
    }

}
