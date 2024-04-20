import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Contiguous {
    private static final int MAX_BLOCKS = 40;
    private static final int BLOCK_SIZE = 10;

    private static List<String> directory;
    private final int[] disk;
    private int headMoves;
    private int notAdded;

    public Contiguous() {
        directory = new ArrayList<>();
        this.disk = new int[MAX_BLOCKS];
        Arrays.fill(disk, 0);
        headMoves = 0;
        notAdded = 0;
    }

    public void contiguousSimulation() throws FileNotFoundException {

        String operation;
        String fileName;
        String size;
        int fileSize;
        int fileNumber = 1;
        String input;

        System.out.println("-------------------- START CONTIGUOUS SIMULATION --------------------");
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
                    size = input.substring(input.length() - 2).trim();
                    fileSize = Integer.parseInt(size);
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
                case "print":
                    printDisk(directory);
                    break;
                case "append":
                    fileName = matcher.find() ? matcher.group(1) : null;
                    size = input.substring(input.length() - 2).trim();
                    fileSize = Integer.parseInt(size);
                    appendToFile(fileName, fileSize, fileNumber);
                    break;
            }
        }
        System.out.println("-------------------- STATS - Contiguous --------------------");
        System.out.println("Head Moves: " + headMoves);
        System.out.println("File(s) not added due to space constraints: " + notAdded);
        System.out.println("--------------------END OF CONTIGUOUS ANALYSIS--------------------");
    }

    private void addFile(String fileName, int fileSize) {
        int startIndex = -1;
        int count = 0;
        for (int i = 0; i < MAX_BLOCKS; i++) {
            if (disk[i] == 0) {
                if (startIndex == -1) {
                    startIndex = i;
                }
                count++;
                if (count == fileSize) {
                    for (int j = startIndex; j < startIndex + fileSize; j++) {
                        disk[j] = fileSize;
                    }
                    directory.add(fileName + " BLOCKS: [" + startIndex + "-" + (startIndex + fileSize - 1) + "]");
                    System.out.println("File " + fileName + " ADDED to drive");
                    return;
                }
            } else {
                startIndex = -1;
                count = 0;
            }
        }
        System.out.println("File " + fileName + " FAILED TO ADD to drive, insufficient space");
        notAdded++;
    }

    private void deleteFile(String fileName) {
        String separator = "-";
        for (Iterator<String> iterator = directory.iterator(); iterator.hasNext(); ) {
            String entry = iterator.next();
            if (entry.startsWith(fileName)) {
                String[] parts = entry.split(" ");
                String blocksPart = parts[parts.length - 1];
                int sepPos = blocksPart.indexOf(separator);
                String blockEnd = blocksPart.substring(sepPos + separator.length());
                String blockStart = blocksPart.substring(0, sepPos + separator.length());
                int end = Integer.parseInt(blockEnd.replace("]","").replace("[",""));
                int start = Integer.parseInt(blockStart.replace("]","").replace("[","").replace("-",""));
                for (int i = start; i <= end; i++) {
                    disk[i] = 0;
                }
                iterator.remove();
                System.out.println("File " + fileName + " DELETED successfully");
                return;
            }
        }
        System.out.println("File " + fileName + " not found");
    }

    private void readFile(String fileName) {
        for (String entry : directory) {
            if (entry.startsWith(fileName)) {
                System.out.println("File " + fileName + " was READ successfully with 1 head moves");
                headMoves++;
                return;
            }
        }
        System.out.println("File " + fileName + " not found");
    }

    private void appendToFile(String fileName, int fileSize, int fileNumber) {
        String separator = "-";
        for (Iterator<String> iterator = directory.iterator(); iterator.hasNext(); ) {
            String entry = iterator.next();
            if (entry.startsWith(fileName)) {
                String[] parts = entry.split(" ");
                String blocksPart = parts[parts.length - 1];
                int sepPos = blocksPart.indexOf(separator);
                String blockEnd = blocksPart.substring(sepPos + separator.length());
                String blockStart = blocksPart.substring(0, sepPos + separator.length());
                int end = Integer.parseInt(blockEnd.replace("]","").replace("[",""));
                int start = Integer.parseInt(blockStart.replace("]","").replace("[","").replace("-",""));
                if (end + fileSize < MAX_BLOCKS && disk[end + 1] == 0) {
                    for (int i = end + 1; i <= end + fileSize; i++) {
                        disk[i] = fileNumber;
                    }
                    System.out.println("File " + fileName + " APPENDED successfully");
                    return;
                } else {
                    System.out.println("File " + fileName + " APPEND, doesn't fit in place, removing from existing memory location, add in new location");
                    iterator.remove();
                    for (int i = start; i <= end; i++) {
                        disk[i] = 0;
                    }
                    System.out.println("Moving file of " + ((end - start) + fileSize) + " blocks with as many head moves.");
                    headMoves += ((end - start) + fileSize);
                    addFile(fileName, ((end - start) + fileSize + 1));
                    return;
                }
            }
        }
        System.out.println("File " + fileName + " not found");
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
