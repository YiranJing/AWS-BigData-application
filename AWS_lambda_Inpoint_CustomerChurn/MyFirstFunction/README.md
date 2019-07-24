To start Lambda function easier: try batch job first [Source code](https://github.com/YiranJing/BigDataAnalysis/blob/master/AWS_lambda_callBatch_CustomerChurn/Batch_Transform_Test/lambda_function.py)


Use this code in other data case, you just need to modify 
1. the environmental variables through the Lambda function console 
2. The perfix of S3 trigger event (the path of input data folder)
3. Rename the transformJobName

To check if Lambda function work:
1. Check through Amazon SageMaker interface: click Batch Transform jobs to see if the lambda function triggered new batch job.
2. After Batch job finish, go to output folder to check if the output file 'xxx.csv.out' exits. 
3. To understand model performance, download data or re-readin sageMaker notebook. 

