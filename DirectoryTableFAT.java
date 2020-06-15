import java.util.HashMap;

/* Directory Table in FAT Allocation is a HashMap since I didn't have to iterate it over the traditional way and
 * in order to benefit from O(1) times. The key is the file identifier and the value is the EntryFAT which takes startingIndex
 * and size(in bytes) of the file.
 */

public class DirectoryTableFAT {
    private HashMap<Integer, EntryFAT> dt;

    // constructor
    public DirectoryTableFAT() {
        dt = new HashMap<Integer, EntryFAT>();
    }

    // the getter needed in my implementation.
    public HashMap<Integer, EntryFAT> getDt() {
        return dt;
    }
}
