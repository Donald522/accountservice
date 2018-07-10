package runner;

import controller.AccountServiceController;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Created by Anton Tolkachev.
 * Since 08.07.2018
 */
public class AccountServiceRunner {

    private static final String SERVER_HOST = "http://localhost";
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) throws Exception {
        URI baseUri = UriBuilder.fromUri(SERVER_HOST).port(SERVER_PORT)
                .build();
        ResourceConfig config = new ResourceConfig(AccountServiceController.class)
                .register(new ApplicationBinder());

        Server server = JettyHttpContainerFactory.createServer(baseUri, config,
                false);

        ContextHandler contextHandler = new ContextHandler();
        contextHandler.setHandler(server.getHandler());

        HandlerCollection handlerCollection = new HandlerCollection();
        handlerCollection.setHandlers(new Handler[] { contextHandler, new DefaultHandler() });
        server.setHandler(handlerCollection);

        server.start();
        server.join();
    }
}
