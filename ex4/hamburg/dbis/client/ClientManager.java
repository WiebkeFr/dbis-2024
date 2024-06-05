package hamburg.dbis.client;

import hamburg.dbis.utils.DataLoader;
import hamburg.dbis.utils.RandomHashSet;

import java.util.Random;

public class ClientManager {

    static final private ClientManager _manager;

    static {
        try {
            _manager = new ClientManager();
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private ClientManager() {
    }

    static public ClientManager getInstance() {
        return _manager;
    }

    public void startClients() {

        // TODO modify the amount of clients and their schedule to your liking
        // - You can create a schedule manually (see client1)
        //   or you can create them automatically with random values (see client 2)
        // - using the client function toggleClientDebugMessages() let's you activate
        //   console debug info of the client what they attemp to do on the persistence manager

        int clientid = 1;
        RandomHashSet<String> exampleData = DataLoader.loadExampleData();
        Random rnd = new Random();

        // Creating Client 1 (Pages 1 - 19)
        Schedule schedule1 = Schedule.createSchedule()
                .addOperation(5, "Magica De Spell")
                .addOperation(13, "Scrooge McDuck")
                .addOperation(1, "Granny Beagle")
                .addOperation(5, "Huey, Dewey, and Louie");

        Client client1 = new Client(clientid++, schedule1, 1500, 4000);
        client1.toggleClientDebugMessages();


        // Creating Client 2 (Pages 20 - 29)
        int minPageClient2 = 20;
        int maxPageClient2 = 30;
        Schedule schedule2 = Schedule.createSchedule()
                .addOperation(rnd.nextInt(minPageClient2, maxPageClient2), exampleData.getRandomElement())
                .addOperation(rnd.nextInt(minPageClient2, maxPageClient2), exampleData.getRandomElement())
                .addOperation(rnd.nextInt(minPageClient2, maxPageClient2), exampleData.getRandomElement())
                .addOperation(rnd.nextInt(minPageClient2, maxPageClient2), exampleData.getRandomElement())
                .addOperation(rnd.nextInt(minPageClient2, maxPageClient2), exampleData.getRandomElement())
                .addOperation(rnd.nextInt(minPageClient2, maxPageClient2), exampleData.getRandomElement())
                .addOperation(rnd.nextInt(minPageClient2, maxPageClient2), exampleData.getRandomElement())
                .addOperation(rnd.nextInt(minPageClient2, maxPageClient2), exampleData.getRandomElement());

        Client client2 = new Client(clientid++, schedule2);
        client2.toggleClientDebugMessages();

        // Creating Client 3 (Pages 30 - 39)
        int minPageClient3 = 30;
        int maxPageClient3 = 40;
        Schedule schedule3 = Schedule.createSchedule()
                .addOperation(rnd.nextInt(minPageClient3, maxPageClient3), exampleData.getRandomElement())
                .addOperation(rnd.nextInt(minPageClient3, maxPageClient3), exampleData.getRandomElement())
                .addOperation(rnd.nextInt(minPageClient3, maxPageClient3), exampleData.getRandomElement())
                .addOperation(rnd.nextInt(minPageClient3, maxPageClient3), exampleData.getRandomElement())
                .addOperation(rnd.nextInt(minPageClient3, maxPageClient3), exampleData.getRandomElement())
                .addOperation(rnd.nextInt(minPageClient3, maxPageClient3), exampleData.getRandomElement())
                .addOperation(rnd.nextInt(minPageClient3, maxPageClient3), exampleData.getRandomElement())
                .addOperation(rnd.nextInt(minPageClient3, maxPageClient3), exampleData.getRandomElement());

        Client client3 = new Client(clientid++, schedule3);
        client3.toggleClientDebugMessages();


        // Creating Client 4 (Pages 40 - 49)
        int minPageClient4 = 40;
        int maxPageClient4 = 50;
        Schedule schedule4 = Schedule.createSchedule()
                .addOperation(rnd.nextInt(minPageClient4, maxPageClient4), exampleData.getRandomElement())
                .addOperation(rnd.nextInt(minPageClient4, maxPageClient4), exampleData.getRandomElement())
                .addOperation(rnd.nextInt(minPageClient4, maxPageClient4), exampleData.getRandomElement())
                .addOperation(rnd.nextInt(minPageClient4, maxPageClient4), exampleData.getRandomElement())
                .addOperation(rnd.nextInt(minPageClient4, maxPageClient4), exampleData.getRandomElement())
                .addOperation(rnd.nextInt(minPageClient4, maxPageClient4), exampleData.getRandomElement())
                .addOperation(rnd.nextInt(minPageClient4, maxPageClient4), exampleData.getRandomElement())
                .addOperation(rnd.nextInt(minPageClient4, maxPageClient4), exampleData.getRandomElement());

        Client client4 = new Client(clientid++, schedule4);
        client4.toggleClientDebugMessages();




        // Start the clients
        client1.start();
        client2.start();
        client3.start();
        client4.start();
    }
}
