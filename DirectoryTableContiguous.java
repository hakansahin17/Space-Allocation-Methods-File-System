import java.util.ArrayList;
import java.util.Collections;

/* This is the directory table I used in order to implement Contiguous Allocation. It consists of an arraylist of
 * entries.
 */
public class DirectoryTableContiguous {
    private ArrayList<EntryContiguous> dt;

    // the constructor in order to initalize the directoryTable
    public DirectoryTableContiguous() {
        dt = new ArrayList<EntryContiguous>();
    }

    // the getter method I needed while implementing Contiguous Allocation.
    public ArrayList<EntryContiguous> getDt() {
        return dt;
    }

    /* Insert uses the basic insert to the directoryTable, but every time we insert a new entry, it sorts the arrayList
     * with compareTo method implemented in the EntryContiguous. It sorts according to their firstIndexes for easier
     * implementation of compaction/defragmentation.
     */
    public void insert(EntryContiguous entry) {
        dt.add(entry);
        Collections.sort(dt);
    }

}
