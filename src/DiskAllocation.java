import java.io.FileNotFoundException;

public class DiskAllocation {
    public static void main(String[] args) throws FileNotFoundException {
        Contiguous contiguous = new Contiguous();
        contiguous.contiguousSimulation();

//        Indexed indexed = new Indexed();
//        indexed.indexedSimulation();
    }
}
