# relative-placement

This project implements the relative placement scoring system.

Placements are awarded based on reaching a majority of judges 
votes starting by counting only 1st places and progressively increasing to 2nd ... n-th.

### Tie Break
If multiple contestants reach the same majority, a quality metric is used as tie-breaker. If the tie persist, continue testing the tied pair at the next placement level.

Rules: 
https://www.worldsdc.com/wp-content/uploads/2016/04/Relative_placement.pdf

Example: https://www.worldsdc.com/wp-content/uploads/2016/04/Scoring_By_Hand_EXAMPLE_9-28-04.pdf

# Install
To build the jar file and generate javadocs.
~~~bash
$ gradle build javadoc
~~~

# Run
To launch the swing GUI, run the following command.
~~~bash
$ java -jar relative-placement.jar
~~~

Currently the Java Swing UI shows the results in a table.  There is no support yet for printing.  

The recommended use case is to export CSV and format within a spreadsheet application.  This will give more options for formatting the output.  Check the examples folder.
* Format colors / Fonts
* Add rotation to headers

### Input CSV Format
The expected input format is:

| Leader | Follower | Judge 1 | Judge 2 | Judge 3 | Judge 4 | Judge 5 | Head Judge |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Lead 1 | Follower 1 | rank a | rank b | rank c | rank d | rank e | rank f
| ... | ... | ... | ... | ... | ... | ... | ... |
| Lead n | Follower n | rank a+n | rank b+n | rank c+n | rank d+n | rank e+n | rank f+n |

### Error Checks
* There should be 3+ regular judges + 1 head judge.  Number of regular judges should be odd.
* The ranks per judge must range from 1 to n, with no duplicates.

### Steps Taken
1. Load in input.csv file.
2. Score and rank contestants.
3. Generate sorted result to "results.csv".