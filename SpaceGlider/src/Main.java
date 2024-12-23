import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.println("Enter File name to compress (path):");
        Scanner scanner = new Scanner(System.in);
        String path = scanner.nextLine();
        Functions.createWordDictionary(path,null);
        Functions.orderLinesByValue(path+"_dict.dict");
        Functions.rewriteFileWithBinaryReplacement(path,path"_dict.dictsrt","compressedfile");
        Functions.revertCompressedFile("compressedfile", path+"_dict.dictsrt", "decompressedfile");
    }
}