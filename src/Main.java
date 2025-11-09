import DS.HeapFile;
import Tester.Osoba;
import Tester.StructureTester;

public class Main {
    public static void main(String[] args) {
        HeapFile<Osoba> heap = new HeapFile<>("osobyHeap.bin", Osoba.class, 4096);
        StructureTester<Osoba> tester = new StructureTester<>(heap);
        tester.performRandomOperations(50);

        tester.printHeap();
        tester.printExpected();
    }
}
