#!/usr/bin/env python3
# Name: Chandhini Bayina
# Number: 700756775

import sys

current_word = None
current_count = 0

for line in sys.stdin:
     # Strip trailing whitespace and split on the first tab only
    word, count = line.strip().split('\t', 1)
    try:
        count = int(count)
    except ValueError:
        continue
    # If the word is the same as the current one, accumulate its count
    if current_word == word:
        current_count += count
    else:
        # If we encounter a new word, output the previous word and its total count
        if current_word:
            print(f"{current_word}\t{current_count}")
        # Start counting for the new word
        current_word = word
        current_count = count
# After the loop, output the final word and its total count
if current_word:
    print(f"{current_word}\t{current_count}")
