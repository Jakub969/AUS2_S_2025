package Tester;

import DS.Block;
import DS.HeapFile;
import Interface.IRecord;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class StructureTester<T extends IRecord<T>> {
    private final HeapFile<T> heapFile;
    private final List<List<IRecord<T>>> expectedBlocks;
    private final Random random;

    public StructureTester(HeapFile<T> heapFile) {
        this.heapFile = heapFile;
        this.expectedBlocks = new LinkedList<>();
        this.random = new Random();
    }

    // ===== Basic operations =====

    public void insertRecord(T record) {
        int blockIndex = this.heapFile.insertRecord(record);
        while (this.expectedBlocks.size() <= blockIndex) {
            this.expectedBlocks.add(new ArrayList<>());
        }
        this.expectedBlocks.get(blockIndex).add(record);
    }

    public void removeRecord(T record) {
        boolean removedFromHeap = this.heapFile.deleteRecord(record);
        boolean removedFromExpected = false;

        for (List<IRecord<T>> blockList : this.expectedBlocks) {
            if (blockList.removeIf(r -> r.isEqual(record))) {
                removedFromExpected = true;
                break;
            }
        }

        if (removedFromHeap != removedFromExpected) {
            throw new IllegalStateException("Delete mismatch: heap and in-memory state differ.");
        }
    }

    public void findRecord(T record) {
        T heapFound = this.heapFile.findRecord(record);
        boolean expectedFound = this.expectedBlocks.stream()
                .flatMap(List::stream)
                .anyMatch(r -> r.isEqual(record));

        boolean heapFoundBool = (heapFound != null);

        if (heapFoundBool != expectedFound) {
            throw new IllegalStateException("Find mismatch: heap and expected state differ.");
        }
    }

    // ===== Random record generation =====

    @SuppressWarnings("unchecked")
    public T generateRandomRecord() {
        if (!this.heapFile.getRecordClass().equals(Osoba.class)) {
            throw new IllegalStateException("Random generation currently supports only Osoba class.");
        }

        int menoLen = 5 + this.random.nextInt(11);
        int priezLen = 5 + this.random.nextInt(10);
        int uuidLen = 10;

        String meno = this.randomString(menoLen);
        String priezvisko = this.randomString(priezLen);
        Date datum = new Date(ThreadLocalRandom.current().nextLong(0, System.currentTimeMillis()));
        String uuid = this.randomString(uuidLen);

        return (T) new Osoba(meno, priezvisko, datum, uuid);
    }

    private String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char c = (char) ('A' + this.random.nextInt(26));
            sb.append(c);
        }
        return sb.toString();
    }

    // ===== Random operation testing =====

    public void performRandomOperations(int operationsCount) {
        List<T> insertedRecords = new ArrayList<>();

        for (int i = 0; i < operationsCount; i++) {
            int choice = this.random.nextInt(3); // 0=insert, 1=delete, 2=find

            switch (choice) {
                case 0 -> { // insert
                    T newRecord = this.generateRandomRecord();
                    this.insertRecord(newRecord);
                    insertedRecords.add(newRecord);
                    System.out.println("[INSERT] " + this.recordSummary(newRecord));
                }

                case 1 -> { // delete
                    if (!insertedRecords.isEmpty()) {
                        T toDelete = insertedRecords.get(this.random.nextInt(insertedRecords.size()));
                        this.removeRecord(toDelete);
                        insertedRecords.removeIf(r -> r.isEqual(toDelete));
                        System.out.println("[DELETE] " + this.recordSummary(toDelete));
                    }
                }

                case 2 -> { // find
                    if (!insertedRecords.isEmpty()) {
                        T toFind = insertedRecords.get(this.random.nextInt(insertedRecords.size()));
                        this.findRecord(toFind);
                        System.out.println("[FIND] " + this.recordSummary(toFind));
                    }
                }
            }
        }
    }

    private String recordSummary(T record) {
        if (record instanceof Osoba o) {
            return o.getMeno() + " " +
                    o.getPriezvisko() + " (" +
                    o.getUUID() + ")";
        }
        return record.toString();
    }

    // ===== Utility methods =====

    public void printHeap() {
        int totalBlocks = this.expectedBlocks.size();
        for (int i = 0; i < totalBlocks; i++) {
            System.out.println("Block " + i + ":");
            Block<T> block = this.heapFile.getBlock(i);
            block.printRecords();
        }
    }

    public void printExpected() {
        for (int i = 0; i < this.expectedBlocks.size(); i++) {
            System.out.println("Expected Block " + i + ": " + this.expectedBlocks.get(i));
        }
    }
}
