# GigMatchPro_AvlHeap

GigMatch Pro is a Java-based simulation of a freelancing marketplace, implemented for the CmpE 250 course. It models customers, freelancers, employments, cancellations, monthly updates, blacklist rules, and dynamic skill/rating changes exactly as defined in the project specification. The system uses custom data structures (AVL tree and hash map) to efficiently rank freelancers and handle large-scale inputs. A composite score ranks freelancers based on skill–service matching, rating, and reliability. The program processes a command-based input file and outputs results in the required format.
Requirements:
Java 17 or later
A command-line environment (Terminal or PowerShell)
An input file (e.g., input.txt)
No external libraries are required.
How to run:
Navigate to the project directory.
Compile all Java files using: javac src/*.java
Run the program with: java src.Main input.txt output.txt
The results will be written to the specified output file.
