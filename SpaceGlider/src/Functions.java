import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Functions {

    public static void createWordDictionary(String fileName, Map<String, Integer> wordCount) {
        char record_separator = 0x1e;
        if (wordCount == null) {
            wordCount = new ConcurrentHashMap<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] words = line.split("\\s+");
                    for (String word : words) {
                        if (!word.isEmpty()) {
                            wordCount.merge(word, 1, Integer::sum);
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Error reading file: " + fileName, e);
            }
        }

        String dictFileName = fileName + "_dict.dict";
        File dictFile = new File(dictFileName);

        if (ensureFile(dictFile)) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(dictFile))) {
                for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
                    writer.write(entry.getKey() + record_separator + entry.getValue());
                    writer.newLine();
                }
            } catch (IOException e) {
                throw new RuntimeException("Error writing dictionary to file: " + dictFileName, e);
            }
        }
    }

    public static void orderLinesByValue(String fileName) {
        char record_separator = 0x1e;
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + fileName, e);
        }

        lines.sort((line1, line2) -> {
            String[] parts1 = line1.split(String.valueOf(record_separator));
            String[] parts2 = line2.split(String.valueOf(record_separator));
            int value1 = Integer.parseInt(parts1[1].trim());
            int value2 = Integer.parseInt(parts2[1].trim());
            return Integer.compare(value2, value1);
        });

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName + "srt"))) {
            for (String line : lines) {
                String[] parts = line.split(String.valueOf(record_separator));
                writer.write(parts[0]);
                writer.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Error writing ordered lines to file: " + fileName, e);
        }
    }

    public static void rewriteFileWithBinaryReplacement(String inputFileName, String dictFileName, String outputFileName) {
        Map<String, Integer> dictMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(dictFileName))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                dictMap.put(line.trim(), lineNumber++);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading dictionary file: " + dictFileName, e);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFileName));
             FileOutputStream writer = new FileOutputStream(outputFileName)) {
            String word;
            while ((word = reader.readLine()) != null) {
                String[] words = word.split("\\s+");
                for (String w : words) {
                    if (!w.isEmpty()) {
                        int index = dictMap.getOrDefault(w.trim(), -1);
                        byte[] binaryReplacement = getBinaryReplacementAsBytes(index);
                        writer.write(binaryReplacement);
                    }
                }
                writer.write('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException("Error processing input file: " + inputFileName, e);
        }
    }

    public static void revertCompressedFile(String inputFileName, String dictFileName, String outputFileName) {
        Map<Integer, String> dictMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(dictFileName))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                dictMap.put(lineNumber++, line.trim());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading dictionary file: " + dictFileName, e);
        }

        try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(inputFileName));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName))) {
            int byteRead;
            while ((byteRead = reader.read()) != -1) {
                if (byteRead == '\n') {
                    writer.newLine();
                    continue;
                }
                int numBytes = getNumBytes(byteRead);
                byte[] byteSet = new byte[numBytes];
                byteSet[0] = (byte) byteRead;
                for (int i = 1; i < numBytes; i++) {
                    byteSet[i] = (byte) reader.read();
                }
                int index = getIndexFromByteSet(byteSet);
                String word = dictMap.getOrDefault(index, "");
                writer.write(word);
                writer.write(' ');
            }
            writer.newLine(); // Ensure the last line ends with a newline
        } catch (IOException e) {
            throw new RuntimeException("Error processing input file: " + inputFileName, e);
        }
    }

    private static int getNumBytes(int firstByte) {
        if ((firstByte & 0b10000000) != 0) {
            return 1;
        } else if ((firstByte & 0b01000000) != 0) {
            return 2;
        } else if ((firstByte & 0b00100000) != 0) {
            return 3;
        } else {
            return 4;
        }
    }

    private static int getIndexFromByteSet(byte[] byteSet) {
        int index = 0;
        if (byteSet.length == 1) {
            index = (byteSet[0] & 0b01111111) + 1;
        } else if (byteSet.length == 2) {
            index = ((byteSet[0] & 0b00111111) << 8) | (byteSet[1] & 0xFF) + 129;
        } else if (byteSet.length == 3) {
            index = ((byteSet[0] & 0b00011111) << 16) | ((byteSet[1] & 0xFF) << 8) | (byteSet[2] & 0xFF) + 129 + 16384;
        } else if (byteSet.length == 4) {
            index = ((byteSet[0] & 0b00001111) << 24) | ((byteSet[1] & 0xFF) << 16) | ((byteSet[2] & 0xFF) << 8) | (byteSet[3] & 0xFF) + 22097152 + 129 + 16384;
        }
        return index;
    }

    private static String getBinaryReplacement(int index) {
        if (index >= 1 && index < 129) {
            return Integer.toBinaryString(0b10000000 + (index - 1));
        } else if (index >= 129 && index < 129 + 16384) {
            return Integer.toBinaryString(0b0100000000000000 + (index - 129));
        } else if (index >= 129 + 16384 && index < 22097152 + 129 + 16384) {
            return Integer.toBinaryString(0b001000000000000000000000 + (index - (129 + 16384)));
        } else {
            return Integer.toBinaryString(0b00010000000000000000000000000000 + (index - (22097152 + 129 + 16384)));
        }
    }

    private static byte[] getBinaryReplacementAsBytes(int index) {
        String binary = getBinaryReplacement(index);
        int byteLength = (binary.length() + 7) / 8;
        byte[] bytes = new byte[byteLength];
        for (int i = 0; i < byteLength; i++) {
            int start = i * 8;
            int end = Math.min(start + 8, binary.length());
            bytes[i] = (byte) Integer.parseInt(binary.substring(start, end), 2);
        }
        return bytes;
    }

    private static boolean ensureFile(File file) {
        try {
            if (file.createNewFile()) {
                System.out.println("File created: " + file.getName());
                return true;
            } else {
                System.out.println("File already exists. Deleting it.");
                if (!file.delete()) {
                    throw new RuntimeException("Unable to delete existing file: " + file.getName());
                }
                return ensureFile(file);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error handling file: " + file.getName(), e);
        }
    }

}
