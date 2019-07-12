# AWS Lambda with MapReduce

Author: Yiran Jing
Date: 12 July 2019

### Motivation
In the case that we have a **big size dataset**, and using only one lambda function will get **runTimeOut error** since the maximum running time for lambda function is 15 mins. But we do need to process all data, So we have to **split the data into a number of chunks** (which can finish Lambda function in 15 mins). Then we can train a model on each individual chunk. Also, we want this work can be done **automatically**(easily) and **in parallel**(more efficiency).

There is a solution: Build **three lambda functions** for MapReduce algorithm.
### Steps:
1. Install Pandas and Numpy to Amazon Lambda.
2. The first Lambda function, which is triggered by the new large input dataset, will split this big dataset to multiple small datasets automatically and write out to folder "Chunk" in S3 bucket .
3. The second lambda function, which is triggered by any new 'PUT' dataset in "chunk" folder, will automatically write back predictions based on the endpoint of the model we built.
4. The third lambda function, which is triggered by the 'UPDATE' new data in "chunk" folder, will merge it to the final single output file.






### [About Me](https://github.com/YiranJing/AboutMe/blob/master/README.md) 🌱
