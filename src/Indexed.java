import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Indexed {
    private static final int MAX_BLOCKS = 40;
    private static final int BLOCK_SIZE = 10;
    private static final int INDEX_BLOCK_SIZE = 8;

    private static List<String> directory;
    private final int[] disk;
    private final Map<String, List<Integer>> fileIndex;
    private int headMoves;

    public Indexed() {
        directory = new ArrayList<>();
        this.disk = new int[MAX_BLOCKS];
        Arrays.fill(disk, 0);
        this.fileIndex = new HashMap<>();
        headMoves = 0;
    }

    public void indexedSimulation() throws FileNotFoundException {
        String operation;
        String fileName;
        String input;

        System.out.println("-------------------- START INDEXED SIMULATION --------------------");
        Scanner myScan = new Scanner(new File("src/disk.dat"));

        int diskCounter = Integer.parseInt(myScan.nextLine());
        System.out.println("Total Blocks: " + diskCounter);
        while (myScan.hasNextLine()) {
            input = myScan.nextLine();
            Matcher matcher = Pattern.compile("\"([^\"]*)\"|(\\S+)").matcher(input);
            operation = matcher.find() ? matcher.group(2) : null;
            switch (operation) {
                case "add":
                    fileName = matcher.find() ? matcher.group(1) : null;
                    String size = input.substring(input.length() - 2).trim();
                    int fileSize = Integer.parseInt(size);
                    addFile(fileName, fileSize);
                    break;
                case "read":
                    fileName = matcher.find() ? matcher.group(1) : null;
                    readFile(fileName);
                    break;
                case "del":
                    fileName = matcher.find() ? matcher.group(1) : null;
                    deleteFile(fileName);
                    break;
                case "append":
                    fileName = matcher.find() ? matcher.group(1) : null;
                    size = input.substring(input.length() - 2).trim();
                    int appendSize = Integer.parseInt(size);
                    appendToFile(fileName, appendSize);
                    break;
                case "print":
                    printDisk(directory);
                    break;
            }
        }
        System.out.println("-------------------- STATS - INDEXED --------------------");
        System.out.println("Head Moves: " + headMoves);
        System.out.println("-------------------- END OF INDEXED ANALYSIS --------------------");
    }

    private void addFile(String fileName, int fileSize) {
        if (fileIndex.containsKey(fileName)) {
            System.out.println("File " + fileName + " already exists.");
            return;
        }

        List<Integer> blocks = new ArrayList<>();
        for (int i = 0; i < fileSize; i++) {
            int block = findFreeBlock();
            if (block == -1) {
                System.out.println("File " + fileName + " could not be added due to insufficient space.");
                return;
            }
            disk[block] = 1;
            blocks.add(block);
        }
        directory.add(fileName);
        fileIndex.put(fileName, blocks);
        System.out.println("File " + fileName + " added successfully.");
    }

    private int findFreeBlock() {
        for (int i = 0; i < MAX_BLOCKS; i++) {
            if (disk[i] == 0) {
                return i;
            }
        }
        return -1;
    }

    private void deleteFile(String fileName) {
        if (!fileIndex.containsKey(fileName)) {
            System.out.println("File " + fileName + " not found.");
            return;
        }

        List<Integer> blocks = fileIndex.get(fileName);
        for (int block : blocks) {
            disk[block] = 0;
        }
        directory.remove(fileName);
        fileIndex.remove(fileName);
        System.out.println("File " + fileName + " deleted successfully.");
    }

    private void readFile(String fileName) {
        if (!fileIndex.containsKey(fileName)) {
            System.out.println("File " + fileName + " not found.");
            return;
        }

        List<Integer> blocks = fileIndex.get(fileName);
        for (int block : blocks) {
            System.out.println("Reading block " + block);
            headMoves++;
        }
    }

    private void appendToFile(String fileName, int appendSize) {
        if (!fileIndex.containsKey(fileName)) {
            System.out.println("File " + fileName + " not found.");
            return;
        }

        List<Integer> blocks = fileIndex.get(fileName);
        for (int i = 0; i < appendSize; i++) {
            int block = findFreeBlock();
            if (block == -1) {
                System.out.println("Append failed for file " + fileName + " due to insufficient space.");
                return;
            }
            blocks.add(block);
            disk[block] = 1;
        }
        System.out.println("Append successful for file " + fileName + ".");
    }

    private void printDisk(List<String> directory) {
        System.out.println("-------------------- Current Drive Contents --------------------");
        System.out.println("\nDIRECTORY:");

        int fileNumber = 1;
        String separator = "-";
        int[] block = new int[MAX_BLOCKS];
        Arrays.fill(block, 0);

        for (String entry : directory) {
            System.out.println(fileNumber + ". " + entry);
            String[] parts = entry.split(" ");
            String blocksPart = parts[parts.length - 1];
            int sepPos = blocksPart.indexOf(separator);
            String blockEnd = blocksPart.substring(sepPos + separator.length());
            String blockStart = blocksPart.substring(0, sepPos + separator.length());
            int end = Integer.parseInt(blockEnd.replace("]","").replace("[",""));
            int start = Integer.parseInt(blockStart.replace("]","").replace("[","").replace("-",""));
            for (int i = start; i <= end; i++) {
                block[i] = fileNumber;
            }
            fileNumber++;
        }

        System.out.println("\nDETAILS:");
        for (int i = 0; i < MAX_BLOCKS; i += BLOCK_SIZE) {
            for (int j = i; j < i + BLOCK_SIZE; j++) {
                if (block[j] == 0) {
                    System.out.print("* ");
                } else {
                    System.out.print(block[j] + " ");
                }
            }
            System.out.println();
        }
    }
}
