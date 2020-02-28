package flashcards;

//import java.util.HashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

// a class to find a Key by Value in a Map
class MapHandling<K, V> {
    private Map<K, V> map;

    public MapHandling(Map<K, V> instanceMap) {
        this.map = instanceMap;
    }

    public void setMap(Map<K, V> map) {
        this.map = map;
    }

    public Map<K, V> getMap() {
        return map;
    }

    public K findKeyByValue(V value) {
        for (Map.Entry<K, V> item : this.map.entrySet()) {
            if (item.getValue().equals(value)) {
                return item.getKey();
            }
        }
        return null;
    }
    public void WriteMapToFile(String filePath) {
        File file = new File(filePath);
        try (PrintWriter printer = new PrintWriter(file)) {
            this.map.forEach((key,value) -> printer.printf("%s\t%s\t%d%n", key, value, Main.stats.getStats((String) key)));
            Main.loggedOutput(this.map.keySet().size() + " cards have been saved.");
        } catch (IOException e) {
            if (file.isDirectory()) {
                Main.loggedOutput("The specified path \"" + filePath + "\" is a directory.");
            } else if (!file.exists()) {
                Main.loggedOutput("File \"" + filePath + "\" not found.");
            } else if (!file.canWrite()) {
                Main.loggedOutput("Cannot write in file: \"" + filePath + "\". Check attributes");
            }
        }
    }
    public void importMapFromFile(String filePath) {
        File file = new File(filePath);
        try (Scanner fileInput = new Scanner(file)) {
            int counter = 0;
            while(fileInput.hasNext()) {
                String[] newLine = fileInput.nextLine().split("\t");
                counter++;
                this.map.put((K) newLine[0], (V) newLine[1]);
                Main.stats.setStats(newLine[0], Integer.parseInt(newLine[2]));
            }
            Main.loggedOutput(counter + " cards have been loaded.");
        } catch (FileNotFoundException e) {
            Main.loggedOutput("File not found.");
        }
    }
}

// declaring a class for statistics
class Stats {

    private LinkedHashMap<String, Integer> stats = new LinkedHashMap<>();

    public Stats (Map<String, String> cards) {
        for (Map.Entry<String, String> item : cards.entrySet()) {
            this.stats.put(item.getKey(), 0);
        }
    }

    public LinkedHashMap<String, Integer> getStats() {
        return stats;
    }

    public void setStats(LinkedHashMap<String, Integer> stats) {
        this.stats = stats;
    }

    // a method for updating statistics of a particular card (+1 mistake)
    public void updateStats(String key) {
        this.stats.put(key, this.stats.get(key) + 1);
    }
    // a method for resetting statistics (removes all statistics)
    public void resetStats() {
        for (Map.Entry<String, Integer> item : this.stats.entrySet()) {
            item.setValue(0);
        }
        //this.stats.forEach((key, val) -> val = 0);
        Main.loggedOutput("Card statistics has been reset.");
    }
    public Integer getStats(String key) {
        return this.stats.get(key);
    }
    public void setStats(String key, int value) {
        this.stats.put(key, value);
    }
    public void removeStats(String key) {
        this.stats.remove(key);
    }
    // returns key of the card with maximum mistakes
    public int findMaximumMistakes() {
        int maximumMistakes = 0;
        for (Map.Entry<String, Integer> item : this.stats.entrySet()) {
            if (item.getValue() > maximumMistakes) {
                maximumMistakes = item.getValue();
            }
        }
        return maximumMistakes;
    }
    // returns a List of hardest keys (with maximum mistakes)
    public ArrayList<String> findHardestKeys(int maximumMistakes) {
        ArrayList<String> hardestKeys = new ArrayList<>();
        for (Map.Entry<String, Integer> item : this.stats.entrySet()) {
            if (item.getValue() == maximumMistakes) {
                hardestKeys.add(item.getKey());
            }
        }
        return hardestKeys;
    }
}
public class Main {
    // public variables
    // declaring an array for log entries
    public static List<String> logList = new ArrayList<>();
    // declaring cards map
    public static LinkedHashMap<String, String> cards = new LinkedHashMap<>();
    // declaring a stats Class
    public static Stats stats = new Stats(cards);

    // methods
    // method for logged output
    static void loggedOutput(String outputString) {
        System.out.println(outputString);
        logList.add(outputString + "\n");
    }
    // a method for logged input for String
    static String loggedInput(Scanner in) {
        String input = in.nextLine();
        logList.add(input + "\n");
        return input;
    }
    // overloaded method for logged int input
    static int loggedInput(Scanner in, boolean integerOutput) {
        int input = in.nextInt();
        in.nextLine();
        logList.add(Integer.toString(input));
        return input;
    }

    // a method to add a card
    static void addFlashcard(Scanner scnr) {
        loggedOutput("The card:");
        String inputTerm = loggedInput(scnr);
        if (cards.containsKey(inputTerm)) {
            loggedOutput("The card " + "\"" + inputTerm + "\" already exists.");
            return;
        }
        loggedOutput("The definition of the card:");
        String inputDef = loggedInput(scnr);
        if (cards.containsValue(inputDef)) {
            loggedOutput("The definition \"" + inputDef + "\" already exists.");
            return;
        }
        cards.put(inputTerm, inputDef);
        loggedOutput("The pair (\""+ inputTerm + "\":\"" + inputDef + "\") has been added.");
        stats.setStats(inputTerm, 0);
    }
    // a method to remove card
    static void removeFlashcard(Scanner in) {
        loggedOutput("The card:");
        String cardToRemove = loggedInput(in);
        if (cards.containsKey(cardToRemove)) {
            cards.remove(cardToRemove);
            loggedOutput("The card has been removed.");
            stats.removeStats(cardToRemove);
        } else {
            loggedOutput("Can't remove \"" + cardToRemove + "\": there is no such card.");
        }
    }

    // a method to ask User for random cards
    static void askForRandomDefinition (Scanner in) {
        loggedOutput("How many times to ask?");
        int timesToAsk = loggedInput(in, true);
        Random random = new Random();
        for (int i = 0; i < timesToAsk; i++) {
            int randIndex = random.nextInt(cards.keySet().size());
            int counter = 0;
            for (Map.Entry<String, String> item : cards.entrySet()) {
                if (counter++ == randIndex) {
                    loggedOutput("Print the definition of " + "\"" + item.getKey() + "\":");
                    String ans = loggedInput(in);
                    if (ans.equalsIgnoreCase(item.getValue())) {
                        loggedOutput("Correct answer.");
                    } else {
                        String corrCard = new MapHandling<>(cards).findKeyByValue(ans);
                        stats.updateStats(item.getKey());
                        if (corrCard != null) {
                            loggedOutput("Wrong answer. The correct one is \"" + item.getValue() +"\", you've just written the definition of \"" + corrCard + "\".");
                        } else {
                            loggedOutput("Wrong answer. The correct one is \"" + item.getValue() + "\".");
                        }
                    }
                }
            }
        }
    }

    // a method to export to File
    // action 0 - import cards from file
    // action 1 - export cards to file
    // action 2 - export log
    static void FileHandling(Scanner in, int action) {
        loggedOutput("File name:");
        String filePath = loggedInput(in);
        if (action == 0) {
            new MapHandling<>(cards).importMapFromFile(filePath);
        } else if (action == 1) {
            new MapHandling<>(cards).WriteMapToFile(filePath);
        } else if (action == 2) {
            File file = new File(filePath);
            //String ofLogList = logList.toString().substring(1, logList.toString().length() - 1);
            //String[] output = ofLogList.substring(1, ofLogList.length() - 1);
            try (PrintWriter printer = new PrintWriter(file)) {
                for (String str : logList) {
                    printer.println(str);
                }

                loggedOutput("The log has been saved.");
            } catch (IOException e) {
                if (file.isDirectory()) {
                    loggedOutput("The specified path \"" + filePath + "\" is a directory.");
                } else if (!file.exists()) {
                    loggedOutput("File \"" + filePath + "\" not found.");
                } else if (!file.canWrite()) {
                    loggedOutput("Cannot write in file: \"" + filePath + "\". Check attributes");
                }
            }
        }
    }
    // method for hardest card asking
    static void askForHardestCard() {
        int mistakes = stats.findMaximumMistakes();
        if (mistakes != 0) {
            ArrayList<String> hardestKeys = stats.findHardestKeys(mistakes);
            if (hardestKeys.size() > 1) {
                String output = "";
                for (String str : hardestKeys) {
                    output +="\"" + str + "\", ";
                }
                output = output.trim();
                loggedOutput("The hardest cards are " + output.substring(0, output.length() - 1) + ". You have " + mistakes + " errors answering it.");
            } else {
                loggedOutput("The hardest card is \"" + hardestKeys.get(0) + "\". You have " + mistakes + " errors answering it.");
            }
        } else {
            loggedOutput("There are no cards with errors.");
        }
    }


    public static void main(String[] args) {
        // processing arguments
        String exportFilePath = "";
        for (int i = 1; i < args.length; i+=2) {
            if (args[i - 1].equalsIgnoreCase("-import")) {
                new MapHandling<String, String>(cards).importMapFromFile(args[i]);
            } else if (args[i - 1].equalsIgnoreCase("-export")) {
                exportFilePath = args[i];
            } else loggedOutput("Unknown argument: " + args[i - 1]);
        }
        // let's introduce the Menu
        Scanner inputKeyboard = new Scanner(System.in);
        while (true) {
            loggedOutput("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):");
            String command = loggedInput(inputKeyboard);
            if (command.equalsIgnoreCase("add")) {
                addFlashcard(inputKeyboard);
            } else if (command.equalsIgnoreCase("remove")) {
                removeFlashcard(inputKeyboard);
            } else if (command.equalsIgnoreCase("import")) {
                FileHandling(inputKeyboard, 0);
            } else if (command.equalsIgnoreCase("export")) {
                FileHandling(inputKeyboard, 1);
            } else if (command.equalsIgnoreCase("ask")) {
                askForRandomDefinition(inputKeyboard);
            } else if (command.equalsIgnoreCase("exit")) {
                break;
            } else if (command.equalsIgnoreCase("log")) {
                FileHandling(inputKeyboard, 2);
            } else if (command.equalsIgnoreCase("hardest card")) {
                askForHardestCard();
            } else if (command.equalsIgnoreCase("reset stats")) {
                stats.resetStats();
            }
            else {
                loggedOutput("Unknown action command.");
            }
        }
        loggedOutput("Bye bye!");
        if (!exportFilePath.isEmpty()) {
            new MapHandling<String, String>(cards).WriteMapToFile(exportFilePath);
        }
    }
}