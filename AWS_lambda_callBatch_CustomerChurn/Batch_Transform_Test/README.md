Use this code in other data case, you just need to modify 
1. the environmental variables through the Lambda function console 
2. The path or S3 trigger event (the path of input datafile)
3. Rename the transformJobName

To check if Lambda function work:
1. Check through Amazon SageMaker interface: click Batch Transform jobs to see if the lambda function triggered new batch job.
2. After Batch job finish, go to output folder to check if the output file 'xxx.csv.out' exits. 
3. To understand model performance, download data or re-readin sageMaker notebook. 
