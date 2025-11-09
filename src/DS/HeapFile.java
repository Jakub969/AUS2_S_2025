package DS;

import Interface.IRecord;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class HeapFile<T extends IRecord<T>> {
    private int emptyBlockIndex;
    private int partiallyEmptyBlockIndex;
    private final File file;
    private final Class<T> recordClass;
    private final int blockSize;
    private int totalRecords;
    private int totalBlocks;

    public HeapFile(String fileName, Class<T> recordClass, int blockSize) {
        this.file = new File(fileName);
        this.recordClass = recordClass;
        this.blockSize = blockSize;
        this.emptyBlockIndex = -1;
        this.partiallyEmptyBlockIndex = -1;
        this.totalRecords = 0;
        this.totalBlocks = 0;
        if (file.exists()) {
            loadHeader();
        } else {
            saveHeader();
        }
    }

    public int insertRecord(T record) {
        int indexOfBlock;
        if (this.partiallyEmptyBlockIndex != -1) {
            indexOfBlock = this.partiallyEmptyBlockIndex;
        } else {
            indexOfBlock = totalBlocks;
        }
        Block<T> block;
        if (indexOfBlock < totalBlocks) {
            block = getBlock(indexOfBlock);
        } else {
            block = new Block<>(this.recordClass, this.blockSize);
        }
        block.addRecord(record);
        if (block.getValidCount() == block.getBlockFactor()) {
            this.partiallyEmptyBlockIndex = block.getNextBlockIndex();
        } else if (block.getValidCount() == 1) {
            this.emptyBlockIndex = block.getNextBlockIndex();
        }
        writeBlockToFile(block, indexOfBlock);
        if (indexOfBlock == totalBlocks) {
            totalBlocks++;
        }
        totalRecords++;
        saveHeader();
        return indexOfBlock;
    }

    public boolean deleteRecord(T record) {
        int totalBlocks = (int) (this.file.length() / this.blockSize);
        for (int i = 0; i < totalBlocks; i++) {
            Block<T> block = getBlock(i);
            T removed = block.removeRecord(record);
            if (removed != null) {
                writeBlockToFile(block, i);
                totalRecords--;
                if (block.getValidCount() == 0 && i == totalBlocks - 1) {
                    truncateLastBlock();
                }
                return true;
            }
        }
        return false;
    }

    private void truncateLastBlock() {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            long newLength = raf.length() - blockSize;
            raf.setLength(newLength);
            totalBlocks--;
            saveHeader();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void saveHeader() {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(0);
            raf.writeInt(totalBlocks);
            raf.writeInt(totalRecords);
            raf.writeInt(this.emptyBlockIndex);
            raf.writeInt(this.partiallyEmptyBlockIndex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadHeader() {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            if (raf.length() >= 16) {
                totalBlocks = raf.readInt();
                totalRecords = raf.readInt();
                this.emptyBlockIndex = raf.readInt();
                this.partiallyEmptyBlockIndex = raf.readInt();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeBlockToFile(Block<T> block, int blockIndex) {
        byte[] blockData = block.toByteArray();
        try (RandomAccessFile raf = new RandomAccessFile(this.file, "rw")) {
            raf.seek((long) blockIndex * blockSize);
            raf.write(blockData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Block<T> getBlock(int blockIndex) {
        Block<T> block = new Block<>(this.recordClass, this.blockSize);
        try (RandomAccessFile raf = new RandomAccessFile(this.file, "r")) {
            raf.seek((long) blockIndex * blockSize);
            byte[] blockBytes = new byte[blockSize];
            raf.read(blockBytes);
            block.fromByteArray(blockBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return block;
    }

    public T findRecord(T record) {
        int totalBlocks = (int) (this.file.length() / this.blockSize);
        for (int i = 0; i < totalBlocks; i++) {
            Block<T> block = getBlock(i);
            T found = block.getCopyOfRecord(record);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    public Class<T> getRecordClass() {
        return recordClass;
    }
}
