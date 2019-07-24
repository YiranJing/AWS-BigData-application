# Telecom Customer Churn Prediction
Customer attrition, also known as customer churn, customer turnover, or customer defection, is the loss of clients or customers. (Data resource from Kaggle) [Telco Customer Churn Data](https://www.kaggle.com/blastchar/telco-customer-churn/kernels)
## Author
- Yiran Jing


## AWS S3 Storage
- I show the example that how to read in and write out data using **S3 buckets** and the proper way to creat data files in S3 [My SageMaker Notes](https://github.com/YiranJing/BigDataAnalysis/blob/master/AWS_SageMaker_CustomerChurn/SageMakerNotes/TrainDeployBuildinModel.pdf)

## AWS Machine Learning: Amazon SageMaker
### Part 1: Data Cheaning and Analysis
- My way of data cleaning ang data engineering with plot visualization, differ from the Kaggle open resources. Details see: [Data analysis with modelling Notebook](https://github.com/YiranJing/BigDataAnalysis/blob/master/AWS_SageMaker_CustomerChurn/notebook/ChurnDataAnalysis/Churn_Example.ipynb)

### Part 2: Trian and deploy ML model using batch transformation and hosting service
- The example that how to use **Amazon sageMaker buildin ML model** to train, deploy and validate GXBoost model using **Batch Transformation** for Telecom Customer Churn. Details see: [BuildIn XGBoost Amazon SageMaker Notebook](https://github.com/YiranJing/BigDataAnalysis/blob/master/AWS_SageMaker_CustomerChurn/notebook/AmazonSageMaker/AWS_BUILTIN_MODEL_DEPLOYMENT.ipynb)
- The example of deploying and validating GXBoost model using **hosting services**
### Part 3: Trian and deploy customised ML model
- The example that how to **create customised ML model used in Amazon sageMaker** using use **Lambda functions** to run batch transform jobs and to clean the data. Details see: [Customised XGBoost Amazon SageMaker Notebook](https://github.com/YiranJing/BigDataAnalysis/blob/master/AWS_SageMaker_CustomerChurn/notebook/AmazonSageMaker/AWS_CUSTOMISED_MODEL_DEPLOYMENT.ipynb)

## AWS Machine Learning: Amazon Forecast 
Time series forecasting combining related data and ML models [Details(Lastest Version after 23/7/2019 AWS updation)](https://github.com/YiranJing/BigDataAnalysis/tree/master/AWS_Forecast_GolfwithWeather)
- **JSON** format for input data (Big differ compared to the old version)
- Basic: Amazon forecast console. (Easiest way: No coding skill needed) 
- Advanced: SageMaker forecastingz. Call build-in algorithm DeepAR+, more flexible version.

## Amazon Compute: AWS Lambda
### Part 1: Real-time Predictions: AWS Lambda Functions on S3 Event Triggers invoking (ML model) endpoint
Using the built XGBoost endpoint from [Amazon Sagemaker of Customer Churn](https://github.com/YiranJing/BigDataAnalysis/tree/master/AWS_SageMaker_CustomerChurn/notebook/AmazonSageMaker). You can find [Notes](https://github.com/YiranJing/BigDataAnalysis/blob/master/AWS_lambda_CustomerChurn/Lambda_Function_Notes.pdf) here. And the [source code](https://github.com/YiranJing/BigDataAnalysis/tree/master/AWS_lambda_CustomerChurn/MyFirstFunction)


### Part 2: Batch Predictions: AWS Lambda Functions on S3 Event Triggers calling batch transformation using trained ML model
Using the built XGBoost model from [Amazon Sagemaker of Customer Churn](https://github.com/YiranJing/BigDataAnalysis/tree/master/AWS_SageMaker_CustomerChurn/notebook/AmazonSageMaker). You can find [Notes](https://github.com/YiranJing/BigDataAnalysis/blob/master/AWS_lambda_callBatch_CustomerChurn/Lambda_Function_Batch_notes.pdf) here. And the [source code](https://github.com/YiranJing/BigDataAnalysis/tree/master/AWS_lambda_callBatch_CustomerChurn/Batch_Transform_Test)



### Part 3: Handle large input dataset in Lambda Function
Since the maximum running time for lambda function is 15 mins, to handle the input file:
1. The **easist way** is creating and running a new Batch Jobs in lambda function [source code](https://github.com/YiranJing/BigDataAnalysis/tree/master/AWS_lambda_callBatch_CustomerChurn/Batch_Transform_Test). Easiest coding in lambda function but much larger I/O cost than in-point predictions.
2. You can build MapReduce Algo within Real-time Predictions [source code](https://github.com/YiranJing/BigDataAnalysis/tree/master/AWS_lambda_CustomerChurn/MyFirstFunction). More coding needed but it is a more flexible version, which can easily join the predictions with the input file.


### Part 4: Using Custom Library in AWS lambda
In many cases, we want use custom libraries within Lambda function. For example, using Pandas and Numpy to manupuate dataset (such as clean row data and merge dateset etc.) within Lambda function, then, we need to install Pandas and Numpy in AWS Lambda execution environment. [Click me to see how to do it](https://docs.aws.amazon.com/lambda/latest/dg/lambda-python-how-to-create-deployment-package.html). The disadvantage of using custom library is that we cannot do in-line coding and debug within Lambda function console, instand, we have to write up lambda function and upload it as a zip file. 




### [About Me](https://github.com/YiranJing/AboutMe/blob/master/README.md) 🌱
