# JHUgle
600.226.02 | Data Structures | Project 2

--------

### Description
Given an input file of websites and keywords on the websites (see input.txt for format), JHUgle (ha) creates an interactive search engine where the user enters in two terms, then AND or OR, and it returns the website addresses whose contents contain both terms (AND) or either term (OR). I store my URL lists as HashSets in a Deque, so that the union and intersection operations are easy to compute.

### Usage
Input text file is given as the first command line argument. e.g. `java JHUgle input.txt`

### Files
- JHUgle.java - main driver to run JHUgle search engine
- LPHashMap.java - implementation of HashMap using linear probing, implements Iterable and Iterator
- LPHashMapTest.java - JUnit test suite for LPHashMap
- LPMapEntry.java - concrete class for map entry (key, value) pairs
- MapJHU.java - map interface similar to Java Map
- input.txt - sample input
