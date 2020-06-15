/* Block is a class that I use in both implementations, it takes content which is a random number and size, which is in
 * bytes since last block of the file might differ. I use this class to make the directory which is a block array of
 * 32768 size.
 */
public class Block {
    // Content is a random positive number to fill up the directory.
    private int content;
    // Content size is how much space does it use, between 0 (exclusive) and BLOCK_SIZE (inclusive)
    private int contentSize;

    // constructor
    public Block(int content, int contentSize) {
        this.content = content;
        this.contentSize = contentSize;
    }

    // the getters and setters I need while implementing this project
    public int getContent() {
        return content;
    }

    public int getContentSize() {
        return contentSize;
    }


}
