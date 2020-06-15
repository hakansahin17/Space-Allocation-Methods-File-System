import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

// In this class, we implement Contiguous Allocation, the details of the code is explained below, usually line by line:
public class ContiguousAllocation {
    // Directory's size
    private static final int DIRECTORY_SIZE = 32768;

    // The values of BLOCK_SIZE and FILE_NAME, change these in order to test different files.
    private static final int BLOCK_SIZE = 2048;
    private static final String FILE_NAME = "input_2048_600_5_5_0.txt";

    // reject values of the methods in order print how many rejects are in the end.
    private static int createRejects = 0;
    private static int extendRejects = 0;
    private static int shrinkRejects = 0;
    private static int accessRejects = 0;

    // The initialization of the Directory Table, Directory and unique File identifer that starts from 0.
    private static DirectoryTableContiguous dt = new DirectoryTableContiguous();
    private static Directory directory = new Directory(DIRECTORY_SIZE);
    private static int uniqueFileIdentifier = 0;

    // the random generator used for insertion to Directory.
    private static Random rgen = new Random();

    /* The main method used for parsing the input files line by line, since there is ":" between values, we split lines
     * into string arrays every line and use a switch clause in order to run the code line by line.
     * IMPORTANT NOTE: If there is a rejection, the implemented methods return -1, therefore we can increase the rejects
     * there in order to print out the final output. Also time is taken in milliseconds at the start of the code and at
     * the end to see how long the runtime takes.
     */
    public static void main(String[] args) {
        long timeStart = System.currentTimeMillis();
        try {
            Scanner reader = new Scanner(new File(FILE_NAME));
            while(reader.hasNextLine()) {
                String line = reader.nextLine();
                String[] parsedValues = line.split(":");
                switch(parsedValues[0]) {
                    case "c":
                        int error1 = createFile(uniqueFileIdentifier, Integer.parseInt(parsedValues[1]));
                        if(error1 == -1) {
                            System.out.print(" " +line +"\n");
                            uniqueFileIdentifier--;
                            createRejects++;
                        }
                        uniqueFileIdentifier++;
                        break;
                    case "a":
                        int error2 = access(Integer.parseInt(parsedValues[1]), Integer.parseInt(parsedValues[2]));
                        if(error2 == -1) {
                            System.out.print(" " +line +"\n");
                            accessRejects++;
                        }
                        break;
                    case "e":
                        int error3 = extend(Integer.parseInt(parsedValues[1]), Integer.parseInt(parsedValues[2]));
                        if(error3 == -1) {
                            System.out.print(" " +line +"\n");
                            extendRejects++;
                        }
                        break;
                    case "sh":
                        int error4 = shrink(Integer.parseInt(parsedValues[1]), Integer.parseInt(parsedValues[2]));
                        if(error4 == -1) {
                            System.out.print(" " +line +"\n");
                            shrinkRejects++;
                        }
                        break;
                }
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error while reading file.");
            e.printStackTrace();
        }
        System.out.println("Create Rejects: " + createRejects);
        System.out.println("Extend Rejects: " + extendRejects);
        System.out.println("Shrink Rejects: " + shrinkRejects);
        System.out.println("Access Rejects: " + accessRejects);
        System.out.println("Total time: " + (System.currentTimeMillis() - timeStart) + " ms.");

    }
    /* CREATEFILE: creates the file according to fileLength, which is given in bytes. First we convert to this into
     * blockCounts and if there isn't space return -1, if there is space it does the following steps:
     * 1) If it is the first insertion, just insert and return 0.
     * 2) If it isn't the first insertion, iterate over DT to find a first-fit place in order to insert the file:
     *      2a) If space is found, insert and return 0.
     *      2b) If space isn't found, defragment (shift all the files to the left of the directory), then insert after
     *          last element of the DT, since there won't be any spaces in between after defragmentation.
     *
     * The code is explained more in detail below:
     */
    public static int createFile(int fileID, int fileLength) {
        // calculates the fileLength in blockSize.
        int blockCount = (int) Math.ceil(fileLength / (BLOCK_SIZE * 1.0));

        // If there isn't enough space, reject.
        if(blockCount > directory.getSize()) {
            System.out.print("CREATE: Not enough space!");
            return -1;
        }

        // Calculate the last block's size in bytes.
        int lastContentSize = BLOCK_SIZE;
        if(fileLength % BLOCK_SIZE != 0)
            lastContentSize = fileLength % BLOCK_SIZE;

        // If DT is empty, start inserting from 0'th index of the directory, then insert to DT and return 0 (success)
        if(dt.getDt().isEmpty()) {
            int i;
            for(i = 0; i < blockCount - 1; i++)
                directory.put(i, new Block(rgen.nextInt(2147483647), BLOCK_SIZE));
            directory.put(i, new Block(rgen.nextInt(2147483647), lastContentSize));
            dt.insert(new EntryContiguous(fileID, 0, blockCount));

            return 0;
        }

       /* In this part of the code, I try to find a first fit in the DT in order to insert my file, if a first-fit is
        * found in the DT, insert the file and return 0 (success). It does this by iterating over the DT and check if
        * a space exists after the file. if it isn't found, continue with the rest of the code which is explained below.
        */
        int count = 0;
        for(EntryContiguous entry : dt.getDt()) {
            for(int i = 0; i < blockCount; i++) {
                if(entry.getFirstIndex() + entry.getSize() + i < DIRECTORY_SIZE && directory.get(entry.getFirstIndex() +
                        entry.getSize() + i) == null) {
                    count++;
                    if(count == blockCount) {
                        int j;
                        for(j = 0; j < blockCount - 1; j++)
                            directory.put(entry.getFirstIndex() + entry.getSize() + j,
                                    new Block(rgen.nextInt(2147483647), BLOCK_SIZE));
                        directory.put(entry.getFirstIndex() + entry.getSize() + j,
                                new Block(rgen.nextInt(2147483647), lastContentSize));
                        dt.insert(new EntryContiguous(fileID, entry.getFirstIndex() + entry.getSize(), blockCount));
                        return 0;
                    }
                } else {
                    count = 0;
                    break;
                }
            }
        }

        /* Since we weren't able to find a first-fit in the dt, now it is time to defragment(shift all the files to the
         * left). It does this by starting to iterate over from the last element of the DT, if there is a space before its
         * first index, shift the file to the left by 1 and update its DT by index -1. then we reset i value, since there
         * might be more space than 1, the loop finishes when we we reach i = 0, which will mean that all files successfully
         * shifted to left. Therefore we have successfully finished our defragmentation, in which we can move to next step.
         */
        for(int i = dt.getDt().size() - 1; i > 0; i--) {
            EntryContiguous entry = dt.getDt().get(i);
            if(directory.get(entry.getFirstIndex() - 1) == null) {
                for(int j = 0; j < entry.getSize(); j++) {
                    directory.getDirectory()[entry.getFirstIndex() + j - 1] = directory.get(entry.getFirstIndex()+ j);
                }
                directory.getDirectory()[entry.getFirstIndex() + entry.getSize() - 1] = null;
                dt.getDt().get(i).setFirstIndex(entry.getFirstIndex() - 1);
                i = dt.getDt().size();
            }
        }


        /* Since we have shifted all the files to the left, the only available space would be at the last index of the
         * DT, since the available space will be there where the last indexed file ends, therefore we just insert it
         * after the highest starting indexed file and return 0 (success).
         */
        int i;
        for(i = 0; i < blockCount - 1; i++)
            directory.put(dt.getDt().get(dt.getDt().size() - 1).getFirstIndex() +
                    dt.getDt().get(dt.getDt().size() - 1).getSize() + i, new Block(rgen.nextInt(2147483647),
                    BLOCK_SIZE));
        directory.put(dt.getDt().get(dt.getDt().size() - 1).getFirstIndex() +
                dt.getDt().get(dt.getDt().size() - 1).getSize() + i, new Block(rgen.nextInt(2147483647),
                lastContentSize));
        dt.insert(new EntryContiguous(fileID, dt.getDt().get(dt.getDt().size() - 1).getFirstIndex() +
                dt.getDt().get(dt.getDt().size() - 1).getSize(), blockCount));

        return 0;
    }

    /* SHRINK: There are two ways of shrink rejection, first being that it will delete the last file, the other one being
     * that the file doesn't exist. In this code, we iterate over the DT in order to find the file we want to shrink.
     * Once we have found it, first we check if shrinking operation will delete all of the file which is not allowed.
     * If it doesn't delete the last block, we will shrink the file by the amount that was specified. If the loop ends,
     * that means our file doesn't exist, therefore we will return a rejection.
     */
    public static int shrink(int fileID, int shrinking) {
        for(EntryContiguous e : dt.getDt()) {
            if(e.getFileIdentifier() == fileID) {
                if(shrinking >= e.getSize()) {
                    System.out.print("SHRINK: Cannot shrink, since it will delete the last one!");
                    return -1;
                } else {
                    for(int i = 0; i < shrinking; i++) {
                        directory.remove(e.getFirstIndex() + e.getSize() - 1);
                        e.setSize(e.getSize() - 1);
                    }
                    return 0;
                }
            }
        }
        System.out.print("SHRINK: Cannot locate the file!");
        return -1;
    }

    /* ACCESS: it can have two rejections, one being that byte is larger than the file size, the other is being that
     * the file doesn't exist. We start by calculating the blocksize of the byteoffset and the lastByteOffset
     * then return the byteoffset from the start of the directory. We do this by first finding the file in the dt, and
     * then calculate the byteOffset, then we return it from the start of the Directory.
     */
    private static int access(int fileID, int byteOffset) {
        int block = (int) Math.ceil(byteOffset / (BLOCK_SIZE * 1.0));
        int lastByteOffset = BLOCK_SIZE;
        if(byteOffset % BLOCK_SIZE != 0)
            lastByteOffset = byteOffset % BLOCK_SIZE;
        for(EntryContiguous entry : dt.getDt()) {
            if(entry.getFileIdentifier() == fileID) {
                int byteTotal = 0;
                for(int i= 0; i < entry.getSize(); i++) {
                    byteTotal = byteTotal + directory.get(entry.getFirstIndex() + i).getContentSize();
                }
                if(byteOffset > byteTotal) {
                    System.out.print("ACCESS: Byte is off limits!");
                    return -1;
                }
                return (BLOCK_SIZE*(entry.getFirstIndex() + block - 1))+ lastByteOffset;
            }
        }
        System.out.print("ACCESS: Cannot locate the file!");
        return -1;
    }

    /* EXTEND: extends the file according to to extension(which is given in block size). First we check if there is
     * space available in the directory, if it isn't return -1 (rejection). If there is, I did the following steps:
     * 1) If there is available space after the file, extend the file and return 0 (success).
     * 2) If there isn't space found after the file, first defragment (shift all the files to the left) and then open
     * up space after the file, shift all the files that comes after our file by the amount needed for extension.
     *
     * The code is explained more in detail below:
     *
     */
    public static int extend(int fileID, int extension) {
        // If there isn't available space, reject.
        if(extension > directory.getSize()) {
            System.out.print("EXTEND: Not enough space!");
            return -1;
        }

        // find the index of the DT and find our entry for the operations. If we cannot find that means file doesn't
        // exist.
        int entryIndex = -1;
        for(int i = 0; i < dt.getDt().size(); i++) {
            EntryContiguous entry = dt.getDt().get(i);
            if(entry.getFileIdentifier() == fileID) {
                entryIndex = i;
                break;
            }
        }
        if(entryIndex == -1) {
            System.out.print("EXTEND: Cannot locate the file!");
            return -1;
        }
        EntryContiguous myEntry = dt.getDt().get(entryIndex);


        /* Check if there is amount of space already available after the file, if it is available, insert it and return
         * 0 (success). If it isn't available, continue with the code:
         */
        int count = 0;
        for(int i = 0; i < extension; i++) {
            if(myEntry.getFirstIndex() + myEntry.getSize() + i < DIRECTORY_SIZE && directory.get(myEntry.getFirstIndex() + myEntry.getSize() + i) == null) {
                count++;
                if(count == extension) {
                    for(int j = 0; j < extension; j++)
                        directory.put(myEntry.getFirstIndex() + myEntry.getSize() + j, new Block(rgen.nextInt(2147483647), BLOCK_SIZE));
                    dt.getDt().get(entryIndex).setSize(dt.getDt().get(entryIndex).getSize() + extension);
                    return 0;
                }
            } else {
                break;
            }
        }

        /* Same defragmentation code that was used in the createFile, we shift all the files to the left in order to
         * remove spaces created from shrink operations.
         */
        for(int i = dt.getDt().size() - 1; i > 0; i--) {
            EntryContiguous entry = dt.getDt().get(i);
            if(directory.get(entry.getFirstIndex() - 1) == null) {
                for(int j = 0; j < entry.getSize(); j++) {
                    directory.getDirectory()[entry.getFirstIndex() + j - 1] = directory.get(entry.getFirstIndex()+ j);
                }
                directory.getDirectory()[entry.getFirstIndex() + entry.getSize() - 1] = null;
                dt.getDt().get(i).setFirstIndex(entry.getFirstIndex() - 1);
                i = dt.getDt().size();
            }
        }

        /* Then we start by shifting the files after entryIndex in DT by the amount of extension in order to open up space
         * for extension. we do this by iterating from last until entryIndex, and shift them to the right and update their
         * DT firstIndex accordingly. We repeat these steps for extension times. Therefore we end up opening the exact
         * space we need for the extension.
         */
        for(int i = 0; i < extension; i++) {
            for(int j = dt.getDt().size() - 1; j > entryIndex; j--) {
                EntryContiguous entry = dt.getDt().get(j);
                for(int k = 0; k < entry.getSize(); k++) {
                    directory.getDirectory()[entry.getFirstIndex() + entry.getSize() - k] = directory.get(entry.getFirstIndex() + entry.getSize() - k - 1);
                }
                directory.getDirectory()[dt.getDt().get(j).getFirstIndex()] = null;
                dt.getDt().get(j).setFirstIndex(dt.getDt().get(j).getFirstIndex() + 1);
            }
        }

        /* Then we just put the extended blocks where our file ends, since we have opened just enough space for the
         * extension. Then finally we update our dt and return 0 (success).
         */
        for(int i = 0; i < extension; i++) {
            directory.put(dt.getDt().get(entryIndex).getFirstIndex() + dt.getDt().get(entryIndex).getSize() + i, new Block(rgen.nextInt(2147483647), BLOCK_SIZE));
        }
        dt.getDt().get(entryIndex).setSize(dt.getDt().get(entryIndex).getSize() + extension);


        return 0;
    }



}
