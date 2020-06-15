import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.Scanner;

// In this class, we implement FAT Allocation, the details of the code is explained below, usually line by line:
public class FATAllocation {

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

    // The initialization of the Directory Table, Directory, FAT and  unique File identifer that starts from 0.
    private static DirectoryTableFAT dt = new DirectoryTableFAT();
    private static FAT fat = new FAT();
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

    /* CREATEFILE: creates the file according to fileLength, which is given in bytes. First we directly insert if this
     * is the first file and return success. Then we iterate over the FAT to see if there is a missing key, if there is
     * that is the start location of the file. Then we find spaces holding previous index and current index, insert in
     * all the values to FAT and link the last index to -1. After that, we add it to the DT, its size and starting block.
     * More is explained below.
     */
    public static int createFile(int fileID, int fileLength) {
        // calculation of how many blocks we will need and to see if we have enough space.
        int blockCount = (int) Math.ceil(fileLength / (BLOCK_SIZE * 1.0));

        // if we don't have enough space, return rejection.
        if(blockCount > directory.getSize()) {
            System.out.print("CREATE: Not enough space!");
            return -1;
        }
        // calculation of the last block's byte size.
        int lastContentSize = BLOCK_SIZE;
        if(fileLength % BLOCK_SIZE != 0) lastContentSize = fileLength % BLOCK_SIZE;


        /* Insertion of the first ever file, just insert directly to directory and create index links in fat (pointers,
         * then add it to the dt and return success.
         */
        if(dt.getDt().isEmpty()) {
            int i;
            for(i = 0; i < blockCount - 1;i++) {
                fat.getFat().put(i, i + 1);
                directory.put(i, new Block(rgen.nextInt(2147483647), BLOCK_SIZE));
            }
            fat.getFat().put(i, -1);
            directory.put(i, new Block(rgen.nextInt(2147483647), lastContentSize));
            dt.getDt().put(fileID, new EntryFAT(0, fileLength));
            return 0;
        }
        /* In this part of the code, we scan FAT in order to find empty spaces in the directory, we iterate with two indexes,
         * one being the previous index and the other being the current, this is done to properly link the previous index
         * with the current index, because file's blocks might be seperated in the file. After we reach the file's last block
         * we link it with -1 in order to say that file has ended. After all of its done, we add it to the directory with
         * the starting index of the file and total size in bytes and then return 0.
         */
        int count = 1;
        int startingBlock = 0;
        int prev = -1;
        boolean isFound = false;
        for(int i = 0; i < DIRECTORY_SIZE; i++) {
            if(!fat.getFat().containsKey(i)) {
                if(!isFound) {
                    startingBlock = i;
                    isFound = true;
                }
                if(count != blockCount) {
                    if(count == 1) {
                        fat.getFat().put(i, prev);
                        directory.put(i, new Block(rgen.nextInt(2147483647), BLOCK_SIZE));
                    } else {
                        fat.getFat().replace(prev, i);
                        fat.getFat().put(i, prev);
                        directory.put(i, new Block(rgen.nextInt(2147483647), BLOCK_SIZE));
                    }
                    count++;
                } else {
                    fat.getFat().replace(prev, i);
                    fat.getFat().put(i, -1);
                    directory.put(i, new Block(rgen.nextInt(2147483647), lastContentSize));
                    break;
                }
                prev = i;
            }
        }

        dt.getDt().put(fileID, new EntryFAT(startingBlock, fileLength));

        return 0;
    }

    /* SHRINK: Shrinking is done iterating over the FAT once again for shrinking amount in order to find file's -1 value.
     * Then we delete these entries in the FAT and update the previous key's pointer to -1 and do the above step once again.
     * If we cannot locate the key in the DT, then the file doesn't exist, return reject. If the shrinking amount is bigger than
     * the file's size, then return reject. The rest of the code is explained in detail below:
     */

    public static int shrink(int fileID, int shrinking) {
        // If the DT, doesn't have the fileID, return reject.
        if(!dt.getDt().containsKey(fileID)) {
            System.out.print("SHRINK: The file does not exist!");
            return -1;
        }

        // If the shrinking operation will delete the last block of the file, return reject.
        int blockCount = (int) Math.ceil(dt.getDt().get(fileID).getByteSize() / (BLOCK_SIZE * 1.0));
        if(shrinking >= blockCount) {
            System.out.print("SHRINK: Cannot shrink, since it will delete the last one!");
            return -1;
        }

        // calculation of how many bytes we need to remove after deletion, then update the DT according to the shrink.
        int lastContentSize = BLOCK_SIZE;
        if(dt.getDt().get(fileID).getByteSize() % BLOCK_SIZE != 0) lastContentSize = dt.getDt().get(fileID).getByteSize() % BLOCK_SIZE;

        int startingIndex = dt.getDt().get(fileID).getStartingBlock();
        dt.getDt().replace(fileID, new EntryFAT(startingIndex, (dt.getDt().get(fileID).getByteSize()
                - lastContentSize - (shrinking - 1)*BLOCK_SIZE)));


        /* Iterate over the fat once again, once you reach the file end (the value with -1), remove it from the fat,
         * update the FAT before the last one to -1 and repeat the same process until we reach how many blocks we
         * wanted to remove in the first place, after we are done return sucess.
         */
        int iteratingIndex = startingIndex;
        int oldIndex = startingIndex;
        int count = 0;
        while(true) {
            if(count == shrinking) break;
            if(fat.getFat().get(iteratingIndex) == -1) {
                fat.getFat().remove(iteratingIndex);
                fat.getFat().replace(oldIndex, -1);
                directory.remove(iteratingIndex);
                count++;
                iteratingIndex = startingIndex;
                oldIndex = startingIndex;
                continue;
            }
            oldIndex = iteratingIndex;
            iteratingIndex = fat.getFat().get(iteratingIndex);
        }

        return 0;
    }

    /* EXTENSION: extension adds newly amount of blocks at the end of the file, if DT doesn't contain the fileID key,
     * return reject or if extension is bigger than directory's available space, return reject. Otherwise we find the
     * first index of the file, then we iterate over FAT to find an empty space, after finding one, we start iterating
     * over the FAT with the startIndex in order to find the fileEnd, after finding it, replace -1 with the empty space
     * and create until the extension amount, end the last block with a -1 once again. The code is explained in more detail
     * below:
     */
    private static int extend(int fileID, int extension) {
        // If we don't have that file, return reject
        if(!dt.getDt().containsKey(fileID)) {
            System.out.print("EXTEND: The file does not exist!");
            return -1;
        }

        // If the extansion amount is larger than available space, return reject
        if(extension > directory.getSize()) {
            System.out.print("EXTEND: Not enough space!");
            return -1;
        }

        // find the starting index of the file from the DT.
        int startingIndex = dt.getDt().get(fileID).getStartingBlock();

        // update the size of the file in the DT.
        dt.getDt().replace(fileID, new EntryFAT(startingIndex, (dt.getDt().get(fileID).getByteSize() + extension*BLOCK_SIZE)));

        // iteratingIndex is from the startingIndex
        int iteratingIndex = startingIndex;

        // after we reach the extension amount, we break from the for look since we are done with extension
        int count = 0;

        /* The for loop starts from the first index of the directory table, we are looking for an empty space, after
         * finding our first empty space, we iterate over the file starting from the startIndex in order to find end of
         * file. After finding it we update the pointer from -1 to file's newly added block and also add a new fat entry
         * with -1 to indicate end of file. We repeat the above steps until we reach the extension amount, then break and
         * return success.
         */
        for(int i = 0; i < DIRECTORY_SIZE; i++) {
            if(count == extension) break;
            if(!fat.getFat().containsKey(i)) {
                while(true) {
                    if(fat.getFat().get(iteratingIndex) == -1) {
                        fat.getFat().replace(iteratingIndex, i);
                        fat.getFat().put(i, -1);
                        directory.put(i, new Block(rgen.nextInt(2147483647), BLOCK_SIZE));
                        count++;
                        break;
                    }
                    iteratingIndex = fat.getFat().get(iteratingIndex);
                }
            }
        }


        return 0;
    }

    /* ACCESS: in access, we return the byteOffset from the start of the file of the file's wanted byte offset. There are
     * two reject conditions that we need to check. First one being if the file exists, the second one being if the byte
     * wanted is bigger than file's size. otherwise we find the block that the byteoffset is in and the return the
     * byteoffset from the start of the file. More is explained in detail below:
     */
    private static int access(int fileID, int byteOffset) {
        // If the file doesn't exist, return reject
        if(!dt.getDt().containsKey(fileID)) {
            System.out.print("ACCESS: The file does not exist!");
            return -1;
        }
        // If the wanted byteOffset is bigger than file's size, return reject
        if(dt.getDt().get(fileID).getByteSize() < byteOffset) {
            System.out.print("ACCESS: Byte is off limits!");
            return -1;
        }

        // calculation of how many blocks we need to iterate
        int a = (int) Math.ceil(byteOffset / (BLOCK_SIZE * 1.0));

        // calculation of the last block's offset
        int lastByteOffset = BLOCK_SIZE;
        if(byteOffset % BLOCK_SIZE != 0)
            lastByteOffset = byteOffset % BLOCK_SIZE;

        // we start from iterating the starting index of the file.
        int index = dt.getDt().get(fileID).getStartingBlock();
        int startingIndex = dt.getDt().get(fileID).getStartingBlock();

        // we loop until we reach the block we have wanted
        for(int i = index; i < (index + a - 1); i++)
            startingIndex = fat.getFat().get(startingIndex);

        // then we return the byteoffset from the start of the file.
        return BLOCK_SIZE*startingIndex + lastByteOffset;
    }
}
