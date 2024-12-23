# SpaceGlider

SpaceGlider is a file compression and decompression tool designed for text files. It uses a custom dictionary-based approach to compress files efficiently

#

Tested on [enwik9](http://mattmahoney.net/dc/enwik9.zip) with a compression rate of 74.4%.

## Features
- Compresses text files using a word dictionary.
- Orders dictionary entries by frequency.
- Rewrites files with binary replacements for words.

## Usage

1. **Run the program:**
   ```
   java Main
   ```

2. **Enter the file path to compress when prompted.**

3. **The program will generate the following files:**
   - `<input_file>_dict.dict`: The word dictionary.
   - `<input_file>_dict.dictsrt`: The sorted word dictionary.
   - `compressedfile`: The compressed file.
   - `decompressedfile`: The decompressed file.


## Limitations
- Made for files containing 270,549,120 words or less.

## Main Functions

### `createWordDictionary`
Creates a word dictionary from the input file, counting the frequency of each word.

### `orderLinesByValue`
Orders the dictionary entries by their frequency in descending order.

### `rewriteFileWithBinaryReplacement`
Rewrites the input file using binary replacements for words based on the dictionary.

### `revertCompressedFile`
Reverts the compressed file back to its original form using the dictionary.
