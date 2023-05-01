import com.fasterxml.jackson.databind.ObjectMapper;
import com.eventstore.dbclient.*;
import java.util.Date;
import java.util.UUID;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

class AccountCreated {
    private UUID id;
    private String login;
    public UUID getId() {
        return id;
    }
    public String getLogin() {
        return login;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public void setLogin(String login) {
        this.login = login;
    }
}

public class orderevent {
    private String orderId;
    private String customerId;
    private Date orderDate;

    public static void main(String args[]) throws ExecutionException, InterruptedException, IOException {
        EventStoreDBClientSettings setts
                = EventStoreDBConnectionString.parseOrThrow("esdb://localhost:2113?tls=false");
        EventStoreDBClient client = EventStoreDBClient.create(setts);

        AccountCreated createdEvent = new AccountCreated();

        createdEvent.setId(UUID.randomUUID());
        createdEvent.setLogin("julia");
        EventData eventData = EventData
                .builderAsJson("account-created", createdEvent)
                .build();

        WriteResult writeResult = client
                .appendToStream("accounts", eventData)
                .get();

        ReadStreamOptions readStreamOptions = ReadStreamOptions.get()
                .fromStart()
                .notResolveLinkTos();

        ReadResult readResult = client
                .readStream("accounts", 4, readStreamOptions)
                .get();

        for (ResolvedEvent resolvedEvent : readResult.getEvents()) {
            RecordedEvent recordedEvent = resolvedEvent.getOriginalEvent();
            System.out.println(new ObjectMapper().writeValueAsString(recordedEvent.getEventId()));

            // ResolvedEvent resolvedEvent = readResult
            //        .getEvents()
            //       .get(0);

            //AccountCreated writtenEvent = resolvedEvent.getOriginalEvent()
            //       .getEventDataAs(AccountCreated.class);
        }
    }
}

