package DS;

import Interface.IRecord;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class HeapFile<T extends IRecord<T>> {
    private int emptyBlockIndex;
    private int partiallyEmptyBlockIndex;
    private Block<T> currentBlock;
    private int lastBlockIndex;
    private final File file;
    private final Class<T> recordClass;
    private final int blockSize;
    private int totalRecords;

    public HeapFile(String fileName, Class<T> recordClass, int blockSize) {
        this.file = new File(fileName);
        this.recordClass = recordClass;
        this.blockSize = blockSize;
        this.currentBlock = new Block<>(this.recordClass, this.blockSize);
        this.emptyBlockIndex = -1;
        this.partiallyEmptyBlockIndex = -1;
        this.lastBlockIndex = 0;
        this.totalRecords = 0;
    }

    public int insertRecord(T record) {
        int indexOfBlock;
        Block<T> block;
        if (this.partiallyEmptyBlockIndex != -1 || this.emptyBlockIndex != -1) {
            if (this.partiallyEmptyBlockIndex != -1) {
                indexOfBlock = this.partiallyEmptyBlockIndex;
            } else {
                indexOfBlock = this.emptyBlockIndex;
            }
            block = getBlock(indexOfBlock);
            block.addRecord(record);
            if (block.getValidCount() < block.getBlockFactor()) {
                addEmptyBlockToList(block, indexOfBlock);
            } else {
                removeBlockFromList(block, indexOfBlock);
            }
        } else {
            indexOfBlock = (int) this.file.length();
            block = new Block<>(this.recordClass, this.blockSize);
            block.addRecord(record);
            addBlockToList(block, indexOfBlock);
        }
        writeBlockToFile(block, indexOfBlock);
        this.totalRecords++;
        return indexOfBlock;
    }

    private void addBlockToList(Block<T> block, int indexOfBlock) {
        if (this.partiallyEmptyBlockIndex != -1) {
            Block<T> partiallyEmptyBlock = getBlock(this.partiallyEmptyBlockIndex);
            partiallyEmptyBlock.setPreviousBlockIndex((int)this.file.length());
            block.setNextBlockIndex(this.partiallyEmptyBlockIndex);
            writeBlockToFile(partiallyEmptyBlock, this.partiallyEmptyBlockIndex);
        }
        this.partiallyEmptyBlockIndex = indexOfBlock;
    }

    private void removeBlockFromList(Block<T> block, int indexOfBlock) {
        if (block.getNextBlockIndex() != -1) {
            Block<T> nextBlock = getBlock(block.getNextBlockIndex());
            nextBlock.setPreviousBlockIndex(-1);
            writeBlockToFile(nextBlock, block.getNextBlockIndex());
        }
        if (this.partiallyEmptyBlockIndex == indexOfBlock) {
            this.partiallyEmptyBlockIndex = block.getNextBlockIndex();
        }
        if (this.emptyBlockIndex == indexOfBlock) {
            this.emptyBlockIndex = block.getNextBlockIndex();
        }
        block.setNextBlockIndex(-1);
    }

    private void addEmptyBlockToList(Block<T> block, int indexOfBlock) {
        if (this.emptyBlockIndex == indexOfBlock) {
            if (block.getNextBlockIndex() != -1) {
                Block<T> nextBlock = getBlock(block.getNextBlockIndex());
                nextBlock.setPreviousBlockIndex(-1);
                writeBlockToFile(nextBlock, block.getNextBlockIndex());
            }
            this.emptyBlockIndex = block.getNextBlockIndex();

            if (this.partiallyEmptyBlockIndex != -1) {
                Block<T> partiallyEmptyBlock = getBlock(this.partiallyEmptyBlockIndex);
                partiallyEmptyBlock.setPreviousBlockIndex(indexOfBlock);
                writeBlockToFile(partiallyEmptyBlock, this.partiallyEmptyBlockIndex);
            }
            block.setNextBlockIndex(this.partiallyEmptyBlockIndex);
            this.partiallyEmptyBlockIndex = indexOfBlock;
        }
    }

    private void writeBlockToFile(Block<T> block, int blockIndex) {
        byte[] blockData = block.toByteArray();
        try (RandomAccessFile raf = new RandomAccessFile(this.file, "rw")) {
            raf.seek((long) blockIndex * block.getSize());
            raf.write(blockData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Block<T> getBlock(int blockIndex) {
        Block<T> block = new Block<>(this.recordClass, this.blockSize);
        try (RandomAccessFile raf = new RandomAccessFile(this.file, "rw")) {
            raf.seek((long) blockIndex * block.getSize());
            byte[] blockBytes = new byte[block.getSize()];
            raf.read(blockBytes);
            block.fromByteArray(blockBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return block;
    }
}
