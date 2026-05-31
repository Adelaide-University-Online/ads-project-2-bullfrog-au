[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/6MtIQWZn)
# ADS Assignment Starter

A basic Java starter project for Algorithms and Data Structures assignments. This project provides a simple foundation that students can build upon for their coursework.

## Project Structure

```
ADSAssignmentStarter/
├── src/
│   └── main/
│       └── java/
│           └── Main.java          # Main entry point with example code
├── .gitignore                     # Git ignore file for Java projects
└── README.md                      # This file
```

**Good luck with your assignments!** 🚀



Notes:

- You can assume that each course will run in all Study Periods. 
- students will provide the name of a text file and a number which indicates how many courses they can study concurrently
- The tool should return the courses the student needs to study, and the order they will study them in (broken into the different study periods).

1. Graph Representation: 
    - Implement a graph data structure to represent the network of courses. Each course should be a vertex, and edges should represent pre-requisites between courses. 
    - Choose and justify an appropriate representation method (e.g., adjacency matrix, adjacency list) based on the characteristics of the problem. 
    - Choose and justify an appropriate graph directionality (e.g. directed or undirected) and weighting (e.g. weighted or unweighted) based on the characteristics of the problem. 
    - Read the data from the text file and construct the Graph. 
1. Solving the problem: 
    - Once you have created your graph you need to solve the problem and implement the solution in code. 
1. Explain and justify your solution: 
    - Record a 4-10 minute video walking through how you solved the problem and the decisions you made as you completed the task. 
    - Your video should be well-prepared, paced and clearly show your code and the output it generates. 
    - Your recording should be aimed at another technical person. Imagine you are talking with a colleague who is working on another project that is going to use your code. 
    - This is your opportunity to explain what you have done and why. Points are given for explaining your code and for discussing why you have chosen that approach. 

## Program Implementation and Testing

This project implements a degree scheduling tool using a directed graph of courses and prerequisites. `FileParser` reads the input file into a `Graph`, `DegreePlanner` generates a study-period schedule while respecting dependencies and concurrency limits, and `Runner` provides the interactive command-line workflow.

Unit testing is implemented with JUnit4 in `src/test` with one test class per production class (`CourseTest`, `GraphTest`, `FileParserTest`, `DegreePlannerTest`, and `RunnerTest`). Tests cover normal flow, edge cases, and invalid inputs, and they use `junit.Assert` assertions throughout.