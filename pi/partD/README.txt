1.How to implement the code.

  Put Makefile, pipair,TestBug.java and bitcode file into the same directory.
  first,implement command : make,
  second,implement the following command :
  ./pipair <bitcode file> <T SUPPORT> <T CONFIDENCE>,
  e.g.
  ./pipair hello.bc 10 80, to specify support of 10 and confidence of 80%,
  or
  ./pipair <bitcode file>,
  e.g.
  ./pipair hello.bc, to use the default support of 3 and default confidence of 65%.

2.Output instruction

  There are two kinds of output.
  The first one is the format as project 1a: bug: %s in %s, pair: (%s, %s), support: %d, confidence: %.2f%%\n
  eg.
  bug: A in scope2, pair: (A, B), support: 3, confidence: 75.00%
  The second one is the new format for the new bugs : bug: %s in %s, pair: (%s, %s), %s: %d, %s: %d\n
  In the new format, the fist %s is a function's name of pair, the second %s is a founction's name in which the bug exists, the third and the fourth one is the pair of   functions' name that the must appear together, the fifth %s is the one name of pair, the first %d is the times of appearance of the fifth %s in second %s, the sixth   %s is the other name of pair, the last %d is the times of appearance of the sixth %s in second %s.
  eg.
  bug: A in scope2, pair: (A, B), A: 3, B: 2, this means A appears 3 times and B appears 2 times in scope2 for the pair (A,B).

3.Our test case
  
  We just use the test3.bc of Test 3 from part (a) to test our code.
  
  
