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
Currently only a library of functions.
1. Load from a CSV file.
2. Score and rank contestants.
3. Generate an output of the sorted results (in CSV format)

## Future Plans
* Create a GUI
  * Support file load dialog to select CSV file
  * Support JTable view