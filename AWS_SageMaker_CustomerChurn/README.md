# Telecom Customer Churn Prediction
Customer attrition, also known as customer churn, customer turnover, or customer defection, is the loss of clients or customers. (Data resource from Kaggle) [Telco Customer Churn Data](https://www.kaggle.com/blastchar/telco-customer-churn/kernels) 
## Author
- Yiran Jing


## AWS Storage
### Read and Write to S3 bucket
- I show the example that how to read in data from **S3 buckets** and the **proper way to creat datafiles in S3**   [My SageMaker Notes](https://github.com/YiranJing/BigDataAnalysis/blob/master/AWS_SageMaker_CustomerChurn/SageMakerNotes/TrainDeployBuildinModel.pdf)

# AWS Machine Learning
## Amazon SageMaker
### Part 1: Data Cheaning and Analysis
- My way of data cleaning ang data engineering with plot visualization, differ from the Kaggle open resources. Details see: [Data analysis with modelling Notebook](https://github.com/YiranJing/BigDataAnalysis/blob/master/AWS_SageMaker_CustomerChurn/notebook/ChurnDataAnalysis/Churn_Example.ipynb)

### Part 2: Trian and deploy ML model using batch transformation and hosting service
- The example that how to use **Amazon sageMaker buildin ML model** to train, deploy and validate GXBoost model using **Batch Transformation** for Telecom Customer Churn. Details see: [BuildIn XGBoost Amazon SageMaker Notebook](https://github.com/YiranJing/BigDataAnalysis/blob/master/AWS_SageMaker_CustomerChurn/notebook/AmazonSageMaker/AWS_BUILTIN_MODEL_DEPLOYMENT.ipynb)
- The example of deploying and validating GXBoost model using **hosting services**
### Part 3: Trian and deploy customised ML model
- The example that how to **create customised ML model used in Amazon sageMaker** using use **Lambda functions** to run batch transform jobs and to clean the data. Details see: [Customised XGBoost Amazon SageMaker Notebook](https://github.com/YiranJing/BigDataAnalysis/blob/master/AWS_SageMaker_CustomerChurn/notebook/AmazonSageMaker/AWS_CUSTOMISED_MODEL_DEPLOYMENT.ipynb)

## Amazon Forecasting
coming soon

# Amazon Compute
## AWS Lambda
### Part 1: AWS Lambda Functions on S3 Event Triggers invoking (ML model) endpoint 
coming soon
### Part 2: Clean data within AWS Lambda Function
Install and using Pandas and Numpy in AWS Lambda execution environment. 





### [About Me](https://github.com/YiranJing/AboutMe/blob/master/README.md) 🌱
