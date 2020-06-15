
/* This class is the directory which I used in both my implementations, it consists of a block array and the amount of
 * spaces that is left in the directory for easy O(1) size operations instead of O(n).
 */
public class Directory {
    private Block[] directory;
    private int size;

    // the constructor to initialize the directory
    public Directory(int directorySize) {
        directory = new Block[directorySize];
        size = directorySize;
    }

    // the getters and setters that were needed to implement the project.
    public Block[] getDirectory() {
        return directory;
    }

    public Block get(int index) {
        return directory[index];
    }

    public int getSize() {
        return size;
    }

    /* put is a method that just puts the block in the specified index of the directory, the reason why I didn't use the
     * array directly is that so size changes automatically.
     */
    public void put(int index, Block block) {
        directory[index] = block;
        size--;
    }
    /* remove was created the same reason why put was created in the first place, to automatically modify the size.
     * It returns the removed block in case I ever needed it.
     */
    public Block remove(int index) {
        Block removed = new Block(directory[index].getContent(), directory[index].getContentSize());
        directory[index] = null;
        size++;
        return removed;
    }



}
