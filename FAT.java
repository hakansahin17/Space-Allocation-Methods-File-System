import java.util.HashMap;

/* FAT is another HashMap used while implementing my FAT allocation. It is used to store pointers (indexes) on which block
 * it is pointed to. If the value is -1, then the file ends there, if it is anything else, the file's next element is at
 * that index.
 */
public class FAT {
    private HashMap<Integer, Integer> fat;

    // the constructor
    public FAT() {
        fat = new HashMap<Integer, Integer>();
    }

    // the getter that is used while implementing my FAT Allocation.
    public HashMap<Integer, Integer> getFat() {
        return fat;
    }

}
