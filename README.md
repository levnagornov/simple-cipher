## Simple cipher
A simple application that can encrypt a text file (works only with Russian-language texts, other characters will be skipped) using the Caesar cipher. 
The application was created to test whether multiprocessing would help speed up the operating time - and as it turned out, the operating time was **reduced by at least 2 times**.

### There are 3 implementations:
- singleThreadCaesar
- multiThreadCaesarByLines
- multiThreadCaesarByChunks

### There are 2 heavy txt files:
- text.txt contains several copies of John Ronald Reuel Tolkien's book. Lord of the Rings
- result.txt contains the result of encryption of text.txt

Created by Lev Nagornov.
