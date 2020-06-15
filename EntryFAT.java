/* This is the class that goes in the DT of the FAT Allocation, it consists of the starting Block index and size of the
 * file in bytes. This gets put in the DT of the FAT, which is a hashmap  and it gets put in the value part of the hashmap.
 */

public class EntryFAT {
    private int startingBlock;
    private int byteSize;

    // the constructor
    public EntryFAT(int startingBlock, int byteSize) {
        this.startingBlock = startingBlock;
        this.byteSize = byteSize;
    }

    // the getters that were used in my implementation.
    public int getStartingBlock() {
        return startingBlock;
    }

    public int getByteSize() {
        return byteSize;
    }

}
