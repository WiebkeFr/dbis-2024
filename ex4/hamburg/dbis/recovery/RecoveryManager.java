package hamburg.dbis.recovery;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class RecoveryManager {

    static final private RecoveryManager _manager;

    // TODO Add class variables if necessary
    private static final String LOG_FILE = "database/log.txt";
    private static final String DATA_PAGES = "database/pages/%d.txt";

    static {
        try {
            _manager = new RecoveryManager();
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private RecoveryManager() {
        // TODO Initialize class variables if necessary
    }

    static public RecoveryManager getInstance() {
        return _manager;
    }

    public void startRecovery() {
        // TODO
        Set<Integer> winners = analyzeLogsForWinners();
        System.out.println(winners.size() + " winners");        
        Map<Integer, List<String>> entriesToRedo = collectRedoEntries(winners);
        redoTransactions(entriesToRedo);
    }

    private Set<Integer> analyzeLogsForWinners() {
        Set<Integer> winners = new HashSet<>();
        try {
            File logFile = new File(LOG_FILE);
            Scanner scanner = new Scanner(logFile);
            while (scanner.hasNextLine()) {
                String logEntry = scanner.nextLine();
                String[] parts = logEntry.split(",");
                if ("EOT".equals(parts[2])) {
                    winners.add(Integer.parseInt(parts[1]));
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error reading log file: " + e.getMessage());
            e.printStackTrace();
        }
        return winners;
    }

    private Map<Integer, List<String>> collectRedoEntries(Set<Integer> winners) {
        Map<Integer, List<String>> redoEntries = new HashMap<>();
        try {
            File logFile = new File(LOG_FILE);
            Scanner scanner = new Scanner(logFile);
            while (scanner.hasNextLine()) {
                String logEntry = scanner.nextLine();
                String[] parts = logEntry.split(",");
                int taid = Integer.parseInt(parts[1]);
                if (winners.contains(taid) && parts.length > 3) {
                    int pageId = Integer.parseInt(parts[2]);
                    redoEntries.computeIfAbsent(pageId, k -> new ArrayList<>()).add(logEntry);
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error reading log file: " + e.getMessage());
            e.printStackTrace();
        }
        return redoEntries;
    }

    public int getCurrentPageLSN(int pageId) {
        String pageFile = String.format(DATA_PAGES, pageId);
        try {
            File file = new File(pageFile);
            if (file.exists()) {
                Scanner scanner = new Scanner(file);
                if (scanner.hasNextLine()) {
                    String[] parts = scanner.nextLine().split(",");
                    scanner.close();
                    return Integer.parseInt(parts[0]);
                }
                scanner.close();
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error reading page file: " + e.getMessage());
            e.printStackTrace();
        }
        return -1;
    }

    private void redoTransactions(Map<Integer, List<String>> redoEntries) {
        redoEntries.forEach((pageId, entries) -> {
            entries.sort(Comparator.comparingInt(e -> Integer.parseInt(e.split(",")[0])));
            Collections.reverse(entries);

            //System.out.println(entries);
            int currentPageLSN = getCurrentPageLSN(pageId);

            String lastEntry = entries.get(0);
            String[] parts = lastEntry.split(",", 4);
            int logLSN = Integer.parseInt(parts[0]);
            String data = parts[3];
            if (logLSN > currentPageLSN) {
                String pageFile = String.format(DATA_PAGES, pageId);
                String contentToWrite = logLSN + "," + data;
                createOrWriteFile(pageFile, contentToWrite, false);
            }
        });
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
