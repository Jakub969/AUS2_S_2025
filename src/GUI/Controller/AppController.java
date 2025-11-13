package GUI.Controller;

import DS.HeapFile;
import DS.Block;
import Tester.Osoba;

import java.util.ArrayList;
import java.util.List;

public class AppController {

    private final HeapFile<Osoba> heapFile;

    public AppController(HeapFile<Osoba> heapFile) {
        this.heapFile = heapFile;
    }

    public boolean insertOsoba(Osoba osoba) {
        int blockIndex = this.heapFile.insertRecord(osoba);
        return blockIndex >= 0;
    }

    public boolean deleteOsoba(int index, String uuid) {
        Osoba dummy = Osoba.fromUUID(uuid);
        return this.heapFile.deleteRecord(index, dummy);
    }

    public Osoba findOsoba(int index, String uuid) {
        Osoba dummy = Osoba.fromUUID(uuid);
        return this.heapFile.findRecord(index, dummy);
    }

    public List<List<Osoba>> loadBlocks() {
        int total = this.heapFile.getTotalBlocks();
        List<List<Osoba>> list = new ArrayList<>();

        for (int i = 0; i < total; i++) {
            Block<Osoba> block = this.heapFile.getBlock(i);
            List<Osoba> records = new ArrayList<>();

            for (int j = 0; j < block.getValidCount(); j++) {
                records.add(block.getRecordAt(j).createCopy());
            }
            list.add(records);
        }
        return list;
    }
}