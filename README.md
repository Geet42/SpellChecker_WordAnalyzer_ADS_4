# Spell Checker and Word Frequency Analyzer using Hash Tables

**COMP47500 - Advanced Data Structures - Assignment 4: Hashing**

---

## Group Members and Work Split

### Member 1: Geet - Core ADTs and Map Interface
| File                | Description                                               |
|---------------------|-----------------------------------------------------------|
| `Entry.java`        | Entry ADT (key-value pair)                                |
| `MapADT.java`       | Map Interface defining get, put, remove, entrySet, etc.   |
| `ListBasedMap.java`  | List-Based Map (unsorted ArrayList, O(n) baseline)       |
| `SetADT.java`       | Set ADT using Adaptor Pattern over MapADT                 |
| `Multiset.java`     | Multiset (Bag) ADT using Adaptor Pattern over MapADT      |
| `Multimap.java`     | Multimap ADT using Adaptor Pattern over MapADT            |

### Member 2: Muskaan - Hash Table Implementations
| File                | Description                                                          |
|---------------------|----------------------------------------------------------------------|
| `HashTableSC.java`  | Hash Table with Separate Chaining, Division and MAD compression      |
| `HashTableOA.java`  | Hash Table with Open Addressing (Linear Probing + Double Hashing)    |
| `SpellChecker.java` | Application demonstrating all ADTs and Map implementations           |

### Member 3: Shridhar - Skip List and Experiments
| File                    | Description                                                    |
|-------------------------|----------------------------------------------------------------|
| `SkipListMap.java`      | Skip List implementing MapADT (randomized, O(log n) expected) |
| `ExperimentRunner.java` | 5 experiments comparing all implementations                   |
| `README.md`             | Documentation, analysis, and tuning recommendations            |

---

## 1. Theoretical Contribution

### Problem: Efficient Dictionary Lookup for Spell Checking

Spell checking requires testing whether each word in a document belongs to a dictionary of D valid words. Using a List-Based Map, each membership test scans up to D entries, giving O(n x D) total cost for a document of n words. For large dictionaries (100,000+ words) and long documents, this becomes impractical.

### Solution: Hash Table as a Set via the Adaptor Pattern

We solve this by implementing the Map ADT as a Hash Table and then building a Set ADT on top of it using the Adaptor Pattern. The Set stores dictionary words as keys in the underlying Hash Table, where each lookup takes O(1) average time. This reduces the total spell-checking cost from O(n x D) to O(n + D), since building the dictionary is O(D) and checking n words is O(n).

### Complexity Comparison

| Operation           | List-Based Map  | Hash Table (avg) | Hash Table (worst) | Skip List (expected) |
|---------------------|-----------------|-------------------|---------------------|----------------------|
| get (lookup)        | O(n)            | O(1)              | O(n)                | O(log n)             |
| put (insert)        | O(n)            | O(1)              | O(n)                | O(log n)             |
| remove              | O(n)            | O(1)              | O(n)                | O(log n)             |
| Space               | O(n)            | O(n)              | O(n)                | O(n) expected        |
| Ordered iteration   | No              | No                | No                  | Yes                  |

The Hash Table achieves O(1) average case through:
1. A hash function mapping keys to integer hash codes (Java uses polynomial hashing for strings).
2. A compression function mapping hash codes to bucket indices (Division or MAD method).
3. A collision resolution strategy (Separate Chaining or Open Addressing).
4. Rehashing when the load factor exceeds a threshold, keeping chains or probe sequences short.

### Why the Adaptor Pattern matters

The Adaptor Pattern lets us build Set, Multiset, and Multimap from any Map implementation without rewriting logic. The Set stores entries as (element, true), the Multiset stores (element, count), and the Multimap stores (key, List of values). Swapping the backing map (ListBasedMap to HashTableSC to SkipListMap) changes performance characteristics without changing application code.

---

## 2. Implementation Details

### Entry ADT
A simple key-value pair with getKey(), getValue(), setValue(). Stored in every Map implementation.

### Map Interface (MapADT)
Defines the contract: get, put, remove, containsKey, size, isEmpty, entrySet, keySet, values. All four Map implementations satisfy this interface.

### List-Based Map
Stores entries in an unsorted ArrayList. Every operation does a linear scan. Used as the O(n) baseline in experiments.

### Hash Table with Separate Chaining (HashTableSC)
- Each bucket is a LinkedList of Entry objects.
- On collision, the new entry is appended to the chain at that bucket.
- Hash Code: uses Java's hashCode(), which for strings computes a polynomial hash: h = s[0] * 31^(n-1) + s[1] * 31^(n-2) + ... + s[n-1].
- Two Compression Functions:
  - Division Method: index = |hashCode| mod N
  - MAD Method: index = |a * hashCode + b| mod p mod N, where p is a prime > N and a > 0
- Load Factor: alpha = n / N. When alpha exceeds the threshold, rehashing doubles the capacity and reinserts all entries.

### Hash Table with Open Addressing (HashTableOA)
- Entries are stored directly in the array (no chains).
- Collisions resolved by probing for the next empty slot.
- Two Probing Strategies:
  - Linear Probing: h(k,i) = (h(k) + i) mod N. Simple but causes primary clustering: consecutive occupied slots form long runs.
  - Double Hashing: h(k,i) = (h1(k) + i * h2(k)) mod N, where h2(k) = q - (hashCode mod q) and q is a prime < N. The variable step size breaks up clusters.
- Deleted entries are marked DEFUNCT so probe sequences remain valid.
- Load factor threshold is kept lower (0.5) because performance degrades faster than with chaining.

### Skip List (SkipListMap)
- A randomized data structure: a layered linked list providing expected O(log n) operations.
- Search: start from the top level and move right while the next key is smaller, then drop down. Repeat until level 0.
- Insertion: search to find position, generate a random level (coin flip with probability 1/2 per level), splice the new node into each level.
- Deletion: search to find the node, unlink at each level, reduce the current level if needed.
- Keys must be Comparable (provides ordered iteration, unlike hash tables).

### Set, Multiset, Multimap (Adaptor Pattern)
- SetADT wraps MapADT<E, Boolean>. Supports add, contains, remove, union, intersection, difference.
- Multiset wraps MapADT<E, Integer>. Values represent counts of each element.
- Multimap wraps MapADT<K, List<V>>. Each key maps to a list of values.
- All three can be backed by any MapADT implementation.

---

## 3. Experiments

### Experiment 1: Collision Resolution Strategies
**Goal**: Compare Separate Chaining, Linear Probing, and Double Hashing.
**Setup**: Insert and look up 10,000 random 8-character strings.
**Metrics**: Average probes per operation, max chain length or longest cluster, rehash count, wall-clock time.
**Analysis**: Separate Chaining is most forgiving because chains grow independently. Linear Probing suffers from clustering as the table fills. Double Hashing reduces clustering by varying the step size.

### Experiment 2: Compression Functions (Division vs MAD)
**Goal**: Determine if MAD provides better distribution than Division.
**Setup**: Insert 10,000 strings into HashTableSC with each compression method.
**Metrics**: Average probes, max chain length, chain length distribution.
**Analysis**: Division is simple but can produce patterns when the table size is a power of 2. MAD uses a prime modulus and a multiplier to spread keys more uniformly, often producing shorter chains and more even distribution.

### Experiment 3: Load Factor and Rehashing
**Goal**: Find the optimal load factor threshold for each strategy.
**Setup**: Insert 10,000 strings with thresholds ranging from 0.3 to 2.0.
**Metrics**: Final capacity, actual load factor, average probes, rehash count.
**Analysis**: For Separate Chaining, 0.75 is a good default: it triggers rehashing before chains grow too long. For Open Addressing, the threshold must be lower (0.5 to 0.6) because performance degrades rapidly as the table fills. Higher thresholds save memory but increase probe counts.

### Experiment 4: Scalability
**Goal**: Verify theoretical time complexities across all four Map implementations.
**Setup**: Insert and look up N elements for N = 500, 1000, 2000, 5000, 10000.
**Metrics**: Wall-clock time for insert + lookup.
**Expected Results**: List-Based Map time grows quadratically (O(n^2) total). Hash Tables grow near-linearly (O(n) total). Skip List grows as O(n log n).

### Experiment 5: Clustering in Open Addressing
**Goal**: Directly measure primary clustering in Linear Probing vs Double Hashing.
**Setup**: Insert 5,000 strings, then measure cluster count and longest cluster.
**Metrics**: Number of clusters, longest consecutive run of occupied slots, average lookup probes.
**Analysis**: Linear Probing produces fewer but much larger clusters (primary clustering). Double Hashing produces more but smaller clusters, resulting in shorter probe sequences.

---

## 4. Analysis and Tuning Recommendations

### Hash Function
Java's String.hashCode() uses polynomial hashing with base 31, which provides good distribution for typical text data. No custom hash code was needed for our application.

### Compression Function
- Use Division when table capacity is a prime number.
- Use MAD when capacity is a power of 2 (as in our doubling resize strategy), because the prime modulus in MAD compensates for the non-prime capacity.

### Collision Resolution
- Separate Chaining is the safest general-purpose choice. It handles high load factors gracefully and is simpler to implement correctly.
- Double Hashing is better than Linear Probing when memory is constrained and you need open addressing.
- Linear Probing is cache-friendly but only practical at low load factors (below 0.5).

### Load Factor
- Separate Chaining: 0.75 (Java HashMap default) balances memory and speed well.
- Open Addressing: 0.5 keeps probe sequences short; going above 0.6 risks significant slowdown.
- If the approximate number of entries is known in advance, pre-sizing the table avoids unnecessary rehashing.

### When to Use Skip Lists
Skip Lists are preferable when ordered iteration is required (e.g., range queries, sorted output). For pure lookup/insert/remove without ordering, Hash Tables are faster.

---

## 5. How to Build and Run

```bash
# Compile all files
javac -d out src/main/java/adt/*.java src/main/java/hashtable/*.java src/main/java/skiplist/*.java src/main/java/app/*.java src/main/java/experiments/*.java

# Run the Spell Checker application
java -cp out app.SpellChecker

# Run the performance experiments
java -cp out experiments.ExperimentRunner
```

---

## 6. Concepts Used

Entry ADT, Map ADT, Map Interface, List-Based Map, Adaptor Pattern,
Hash Tables, Hash Function, Hash Code, Compression Function,
Division Method, MAD Method (Multiply-Add-Divide), Polynomial Hashing,
Collision, Separate Chaining, Open Addressing, Linear Probing,
Double Hashing, Load Factor, Rehashing, Skip Lists, Randomized Algorithms,
Search/Insertion/Deletion in Skip Lists, Set ADT, Multiset, Multimap.

---

## 7. References
- Goodrich, M.T., Tamassia, R., Goldwasser, M.H. *Data Structures and Algorithms in Java*, 6th Edition, Chapters 9-10.
- Pugh, W. (1990). "Skip Lists: A Probabilistic Alternative to Balanced Trees." Communications of the ACM.
- Knuth, D.E. *The Art of Computer Programming*, Vol. 3: Sorting and Searching.
