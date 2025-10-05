#!/usr/bin/env python3
# Name: Chandhini Bayina
# Number: 700756775

import sys
import re

# Tokenize by alphanumeric words (ignore punctuation, case-insensitive)
tokenizer = re.compile(r"[A-Za-z0-9']+").findall

for line in sys.stdin:
    # Convert line to lowercase and extract words.
    for word in tokenizer(line.lower()):
        print(f"{word}\t1")
