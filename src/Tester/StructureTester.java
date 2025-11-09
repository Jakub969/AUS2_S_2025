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
        int blockIndex = heapFile.insertRecord(record);
        while (expectedBlocks.size() <= blockIndex) {
            expectedBlocks.add(new ArrayList<>());
        }
        expectedBlocks.get(blockIndex).add(record);
    }

    public void removeRecord(T record) {
        boolean removedFromHeap = heapFile.deleteRecord(record);
        boolean removedFromExpected = false;

        for (List<IRecord<T>> blockList : expectedBlocks) {
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
        T heapFound = heapFile.findRecord(record);
        boolean expectedFound = expectedBlocks.stream()
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
        if (!heapFile.getRecordClass().equals(Osoba.class)) {
            throw new IllegalStateException("Random generation currently supports only Osoba class.");
        }

        int menoLen = 5 + random.nextInt(5);
        int priezLen = 5 + random.nextInt(7);
        int uuidLen = 10;

        char[] meno = randomChars(menoLen);
        char[] priezvisko = randomChars(priezLen);
        Date datum = new Date(ThreadLocalRandom.current().nextLong(0, System.currentTimeMillis()));
        char[] uuid = randomChars(uuidLen);

        return (T) new Osoba(meno, priezvisko, datum, uuid);
    }

    private char[] randomChars(int len) {
        char[] arr = new char[len];
        for (int i = 0; i < len; i++) {
            arr[i] = (char) ('A' + random.nextInt(26));
        }
        return arr;
    }

    // ===== Random operation testing =====

    public void performRandomOperations(int operationsCount) {
        List<T> insertedRecords = new ArrayList<>();

        for (int i = 0; i < operationsCount; i++) {
            int choice = random.nextInt(3); // 0=insert, 1=delete, 2=find

            switch (choice) {
                case 0 -> { // insert
                    T newRecord = generateRandomRecord();
                    insertRecord(newRecord);
                    insertedRecords.add(newRecord);
                    System.out.println("[INSERT] " + recordSummary(newRecord));
                }

                case 1 -> { // delete
                    if (!insertedRecords.isEmpty()) {
                        T toDelete = insertedRecords.get(random.nextInt(insertedRecords.size()));
                        removeRecord(toDelete);
                        insertedRecords.removeIf(r -> r.isEqual(toDelete));
                        System.out.println("[DELETE] " + recordSummary(toDelete));
                    }
                }

                case 2 -> { // find
                    if (!insertedRecords.isEmpty()) {
                        T toFind = insertedRecords.get(random.nextInt(insertedRecords.size()));
                        findRecord(toFind);
                        System.out.println("[FIND] " + recordSummary(toFind));
                    }
                }
            }
        }
    }

    private String recordSummary(T record) {
        if (record instanceof Osoba o) {
            return new String(o.getMeno()) + " " +
                    new String(o.getPriezvisko()) + " (" +
                    new String(o.getUUID()) + ")";
        }
        return record.toString();
    }

    // ===== Utility methods =====

    public void printHeap() {
        int totalBlocks = expectedBlocks.size();
        for (int i = 0; i < totalBlocks; i++) {
            System.out.println("Block " + i + ":");
            Block<T> block = heapFile.getBlock(i);
            block.printRecords();
        }
    }

    public void printExpected() {
        for (int i = 0; i < expectedBlocks.size(); i++) {
            System.out.println("Expected Block " + i + ": " + expectedBlocks.get(i));
        }
    }
}
