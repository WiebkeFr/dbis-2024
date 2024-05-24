package hamburg.dbis.persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class PersistenceManager {

    // Add class variables if necessary
    static final private PersistenceManager _manager;
    private AtomicInteger tid;
    private AtomicInteger lsn;
    ConcurrentHashMap<Integer, String> buffer;
    ConcurrentHashMap<Integer, ArrayList<String>> onGoingTransactions;
    ConcurrentHashMap<Integer, ArrayList<String>> finishedTransactions;
    private static final int BUFFER_LIMIT = 5;
    private static final String LOG_FILE = "database/log.txt";
    private static final String DATA_PAGES = "database/pages/%.txt";

    static {
        try {
            _manager = new PersistenceManager();
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private PersistenceManager() {
        // Get the last used transaction id from the log (if present) at startup
        // Initialize class variables if necessary
        String[] lastIds = getLastIds(LOG_FILE);
        lsn = new AtomicInteger(Integer.parseInt(lastIds[0]));
        tid = new AtomicInteger(Integer.parseInt(lastIds[1]));
        buffer = new ConcurrentHashMap<>();
        onGoingTransactions = new ConcurrentHashMap<>();
        finishedTransactions = new ConcurrentHashMap<>();
    }

    static public PersistenceManager getInstance() {
        return _manager;
    }

    public synchronized int beginTransaction() {
        // returns a valid transaction id to the client
        int taid = tid.incrementAndGet();
        onGoingTransactions.put(taid, new ArrayList());
        return taid;
    }

    public void commit(int taid) {
        // handle commits
        String logEntry = lsn.incrementAndGet() + "," + taid + ",EOT";
        createOrWriteFile(LOG_FILE, logEntry, true);
        ArrayList correspondingLsn = onGoingTransactions.remove(taid);
        finishedTransactions.put(taid, correspondingLsn);
    }

    public void write(int taid, int pageid, String data) {
        // handle writes of Transaction taid on page pageid with data
        // 1. Log
        int currentLsn = lsn.incrementAndGet();
        String logEntry = currentLsn + "," + taid + "," + pageid + "," + data;
        createOrWriteFile(LOG_FILE, logEntry, true);

        // 2. Write page in buffer
        String pageEntry = currentLsn + "," + data;
        buffer.put(pageid, pageEntry);
        onGoingTransactions.get(taid).add(Integer.toString(currentLsn));

        // 3. Check buffer limit
        if (buffer.size() > BUFFER_LIMIT) {
            // 3a. Determinate pages of finished transactions
            List finishedLsns = finishedTransactions.values().stream().flatMap(values -> values.stream()).toList();
            ConcurrentHashMap<Object, Object> finishedPages = new ConcurrentHashMap<>();
            buffer.entrySet().stream().filter(t -> finishedLsns.contains(t.getValue().split(",")[0]))
                    .forEach(entry -> finishedPages.put(entry.getKey(), entry.getValue()));

            // 3b. Write selected pages in persistant memory and remove from buffer
            finishedPages.forEach((key, value) -> {
                createOrWriteFile(DATA_PAGES.formatted(key), value.toString(), false);
                buffer.remove(key);
            });
        }
    }

    private String[] getLastIds(String fileName){
        // helper function to get last used LSN and highest Transaction ID
        String[] line = {"0", "0"};
        try {
            File myObj = new File(fileName);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String[] nextLine = myReader.nextLine().split(",");
                line[0] = nextLine[0];
                String nextTid = nextLine[1];
                if (Integer.parseInt(nextTid) > Integer.parseInt(line[1])) {
                    line[1] = nextTid;
                }
            }
            myReader.close();
            return line;
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return line;
    }

    private void createOrWriteFile(String fileName, String data, boolean append) {
        // Helper function to create file (if necessary) and write data
        File f = new File(fileName);
        if(!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileWriter fw;
        try {
            fw = new FileWriter(fileName, append);
            fw.write(data + "\n");
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
