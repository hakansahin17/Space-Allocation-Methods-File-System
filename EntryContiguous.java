
/* EntryContiguous is a class I used in order to implement Contiguous Allocation since the DT entry of Contiguous
 * slightly differs from the DT entry of the FAT Allocation. These entries go into the DirectoryTable of the Contiguous
 * Allocation. It takes fileIdentifier of the file, firstIndex of the file and size(in blocks).
 */
public class EntryContiguous implements Comparable<EntryContiguous> {
    private int fileIdentifier;
    private int firstIndex;
    private int size;

    // the constructor which initializes the Entry.
    public EntryContiguous(int fileIdentifier, int firstIndex, int size) {
        this.fileIdentifier = fileIdentifier;
        this.firstIndex = firstIndex;
        this.size = size;
    }

    // the getters and setters I used while implementing the Contiguous Allocation
    public int getFileIdentifier() {
        return fileIdentifier;
    }

    public int getFirstIndex() {
        return firstIndex;
    }

    public void setFirstIndex(int firstIndex) {
        this.firstIndex = firstIndex;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    /* The reason why I used compareTo is that I wanted to sort the entries in the DT according to their firstIndex in
     * order for easy defragmentation/compaction, so we can do it from right to left.
     */
    @Override
    public int compareTo(EntryContiguous o) {
        return this.firstIndex - o.firstIndex;
    }
}
