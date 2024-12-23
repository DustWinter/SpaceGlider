10000000 = 128
01000000 00000000 = 16384
00100000 00000000 00000000 = 2097152
00001000 00000000 00000000 000000000 = 268435456

total words : 8859143



I want to create a function that will read a file word by word then rewrite it in another one using a specific way:

when trying to rewrite the word the program will check its line placements in another file called enwik9_dict.dict.srt
if the word is between lines[1:129[ replace it with a binnary set between [10000000 : 11111111]
if the word is between lines [129 : 129+16384[ replace it with binary set between [01000000 00000000 : 01111111 11111111]
if the word is between lines [129+16384 : 22097152+129+16384[ replace it with binary set between [00100000 00000000 00000000: 00111111 11111111 11111111]
else replace with binaries between [00100000 00000000 00000000 00000000: til then end of what left of lines