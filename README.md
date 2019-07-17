# relative-placement

This project implements the relative placement scoring system.

Rules: 
https://www.worldsdc.com/wp-content/uploads/2016/04/Relative_placement.pdf

Example: https://www.worldsdc.com/wp-content/uploads/2016/04/Scoring_By_Hand_EXAMPLE_9-28-04.pdf

## Install
To build the jar file and generate javadocs.
~~~bash
$ gradle build javadoc
~~~

## Run
To run scoring system on "input.csv" and save the results to "results.csv".  
The expected token used is "@".
~~~bash
$ java -jar relative-placement.jar input.csv results.csv @
~~~

The expected input format is:

| Leader | Follower | Judge 1 | Judge 2 | Judge 3 | Judge 4 | Judge 5 | Head Judge |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Lead 1 | Follower 1 | rank a | rank b | rank c | rank d | rank e | rank f
| ... | ... | ... | ... | ... | ... | ... | ... |
| Lead n | Follower n | rank a+n | rank b+n | rank c+n | rank d+n | rank e+n | rank f+n |
There should e 3+ judges + 1 head judge.  Number of regular judges will be odd.
The ranks per judge must range from 1 to n, with no duplicates.

### Steps Taken
1. Load in input.csv file.
2. Score and rank contestants.
3. Generate sorted result to "results.csv".


## Future Plans
* Create a GUI
  * Support file load dialog to select CSV file
  * Support JTable view