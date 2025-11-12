package DS;

import Interface.IRecord;

import java.io.*;
import java.util.*;

public class HeapFile<T extends IRecord<T>> {
    private final File dataFile;
    private final File emptyBlocksFile;
    private final File partialBlocksFile;

    private final Class<T> recordClass;
    private final int blockSize;

    private final LinkedList<Integer> emptyBlocks;
    private final LinkedList<Integer> partiallyEmptyBlocks;

    private int totalBlocks;
    private int totalRecords;

    public HeapFile(String baseFileName, Class<T> recordClass, int blockSize) {
        this.dataFile = new File(baseFileName);
        this.emptyBlocksFile = new File(baseFileName + "_empty.txt");
        this.partialBlocksFile = new File(baseFileName + "_partial.txt");

        this.recordClass = recordClass;
        this.blockSize = blockSize;
        this.emptyBlocks = new LinkedList<>();
        this.partiallyEmptyBlocks = new LinkedList<>();

        if (this.dataFile.exists()) {
            this.loadLists();
            this.loadHeader();
        } else {
            this.saveHeader();
            this.saveLists();
        }
    }

    // ========= CORE OPERATIONS ===========

    public int insertRecord(T record) {
        int blockIndex;

        if (!this.partiallyEmptyBlocks.isEmpty()) {
            blockIndex = this.partiallyEmptyBlocks.removeFirst();
        } else if (!this.emptyBlocks.isEmpty()) {
            blockIndex = this.emptyBlocks.removeFirst();
        } else {
            blockIndex = this.totalBlocks;
        }

        Block<T> block;
        if (blockIndex < this.totalBlocks) {
            block = this.getBlock(blockIndex);
        } else {
            block = new Block<>(this.recordClass, this.blockSize);
        }

        block.addRecord(record);

        // update lists
        this.updateListsAfterInsert(blockIndex, block);

        this.writeBlockToFile(block, blockIndex);
        if (blockIndex == this.totalBlocks) this.totalBlocks++;
        this.totalRecords++;

        this.saveLists();
        this.saveHeader();
        return blockIndex;
    }

    public boolean deleteRecord(T record) {
        int blocksInFile = this.totalBlocks;
        boolean deleted = false;

        for (int i = 0; i < blocksInFile; i++) {
            Block<T> block = this.getBlock(i);
            T removed = block.removeRecord(record);
            if (removed != null) {
                deleted = true;
                this.totalRecords--;
                this.updateListsAfterDelete(i, block);
                this.writeBlockToFile(block, i);
                break;
            }
        }

        if (deleted) {
            this.trimTrailingEmptyBlocks();
            this.saveLists();
            this.saveHeader();
        }

        return deleted;
    }

    public T findRecord(T record) {
        for (int i = 0; i < this.totalBlocks; i++) {
            Block<T> block = this.getBlock(i);
            T found = block.getCopyOfRecord(record);
            if (found != null) return found;
        }
        return null;
    }

    // ========= LIST MANAGEMENT ===========

    private void updateListsAfterInsert(int index, Block<T> block) {
        if (block.getValidCount() == block.getBlockFactor()) {
            this.partiallyEmptyBlocks.remove(Integer.valueOf(index));
        } else if (block.getValidCount() > 0 && block.getValidCount() < block.getBlockFactor()) {
            if (!this.partiallyEmptyBlocks.contains(index))
                this.partiallyEmptyBlocks.add(index);
            this.emptyBlocks.remove(Integer.valueOf(index));
        } else if (block.getValidCount() == 0) {
            if (!this.emptyBlocks.contains(index))
                this.emptyBlocks.add(index);
            this.partiallyEmptyBlocks.remove(Integer.valueOf(index));
        }
    }

    private void updateListsAfterDelete(int index, Block<T> block) {
        if (block.getValidCount() == 0) {
            this.emptyBlocks.add(index);
            this.partiallyEmptyBlocks.remove(Integer.valueOf(index));
        } else if (block.getValidCount() < block.getBlockFactor()) {
            if (!this.partiallyEmptyBlocks.contains(index))
                this.partiallyEmptyBlocks.add(index);
        }
    }

    private void trimTrailingEmptyBlocks() {
        // repeatedly remove last block(s) if they are empty
        while (this.totalBlocks > 0 && this.emptyBlocks.contains(this.totalBlocks - 1)) {
            this.truncateLastBlock();
            this.emptyBlocks.remove(Integer.valueOf(this.totalBlocks)); // adjust list
        }
    }

    // ========= FILE I/O ===========

    private void writeBlockToFile(Block<T> block, int blockIndex) {
        byte[] blockData = block.toByteArray();
        try (RandomAccessFile raf = new RandomAccessFile(this.dataFile, "rw")) {
            raf.seek((long) blockIndex * this.blockSize);
            raf.write(blockData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Block<T> getBlock(int blockIndex) {
        Block<T> block = new Block<>(this.recordClass, this.blockSize);
        try (RandomAccessFile raf = new RandomAccessFile(this.dataFile, "r")) {
            raf.seek((long) blockIndex * this.blockSize);
            byte[] bytes = new byte[this.blockSize];
            raf.readFully(bytes);
            block.fromByteArray(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return block;
    }

    private void truncateLastBlock() {
        try (RandomAccessFile raf = new RandomAccessFile(this.dataFile, "rw")) {
            long newLength = Math.max(0, raf.length() - this.blockSize);
            raf.setLength(newLength);
            this.totalBlocks--;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveHeader() {
        try (RandomAccessFile raf = new RandomAccessFile(this.dataFile, "rw")) {
            raf.seek(0);
            raf.writeInt(this.totalBlocks);
            raf.writeInt(this.totalRecords);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadHeader() {
        try (RandomAccessFile raf = new RandomAccessFile(this.dataFile, "r")) {
            if (raf.length() >= 8) {
                this.totalBlocks = raf.readInt();
                this.totalRecords = raf.readInt();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveLists() {
        this.saveListToFile(this.emptyBlocksFile, this.emptyBlocks);
        this.saveListToFile(this.partialBlocksFile, this.partiallyEmptyBlocks);
    }

    private void loadLists() {
        this.loadListFromFile(this.emptyBlocksFile, this.emptyBlocks);
        this.loadListFromFile(this.partialBlocksFile, this.partiallyEmptyBlocks);
    }

    private void saveListToFile(File file, List<Integer> list) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            for (int i : list) {
                pw.println(i);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error saving list file: " + file.getName(), e);
        }
    }

    private void loadListFromFile(File file, List<Integer> list) {
        list.clear();
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                try {
                    list.add(Integer.parseInt(line.trim()));
                } catch (NumberFormatException ignored) {}
            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading list file: " + file.getName(), e);
        }
    }

    // ========= ACCESSORS ===========

    public int getTotalBlocks() { return this.totalBlocks; }
    public int getTotalRecords() { return this.totalRecords; }
    public List<Integer> getEmptyBlocks() { return Collections.unmodifiableList(this.emptyBlocks); }
    public List<Integer> getPartiallyEmptyBlocks() { return Collections.unmodifiableList(this.partiallyEmptyBlocks); }

    public Class<T> getRecordClass() {
        return this.recordClass;
    }
}
