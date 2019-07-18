######################################################
#   Lambda Functions for Customer Chrun Example
#   Author: Yiran Jing
#   Date:16-07-2019
#####################################################

"""
Use this code in other data case, you just need to modify 
1. the environmental variables through the Lambda function console 
2. The path or S3 trigger event (the path of input datafile)
3. Rename the transformJobName

To check if Lambda function work:
1. Check through Amazon SageMaker interface: click Batch Transform jobs to see if the lambda function triggered new batch job.
2. After Batch job finish, go to output folder to check if the output file 'xxx.csv.out' exits. 
3. To understand model performance, download data or re-readin sageMaker notebook. 
"""

import json
import boto3
import csv
import os
import io
import logging
import pickle
from botocore.exceptions import ClientError
from pprint import pprint
from time import strftime, gmtime
from json import dumps, loads, JSONEncoder, JSONDecoder
from six.moves import urllib


bucket = os.environ['BUCKET']
output_key = os.environ['KEY']
client = boto3.client('sagemaker')
batch_output = 's3://{}/{}'.format(bucket, output_key) # specify the location of batch output
Modelname = os.environ['MODELNAME'] # the model name we already have
transformJobName = 'transformLambda-xgboost-Churn'+ strftime("%Y-%m-%d-%H-%M-%S", gmtime())

def lambda_handler(event, context):
    # get new input path from event
    bucket_name = event['Records'][0]['s3']['bucket']['name'] # should be used
    file_key = event['Records'][0]['s3']['object']['key']
    batch_input = 's3://{}/{}'.format(bucket_name, file_key)
    
    response = client.create_transform_job(
    TransformJobName=transformJobName,
    ModelName=Modelname,
    MaxConcurrentTransforms=0,
    MaxPayloadInMB=6,
    # Amazon SageMaker sends the maximum number of records in each request, up to the MaxPayloadInMB limit
    BatchStrategy='MultiRecord', # must fit within the MaxPayloadInMB limit,
    TransformInput={
        'DataSource': {
            'S3DataSource': {
                'S3DataType': 'S3Prefix',
                'S3Uri': batch_input # folder name
            }
        },
        'ContentType': 'text/csv',
        'CompressionType': 'None',
        'SplitType': 'Line'
    },
    TransformOutput={
        'S3OutputPath': batch_output,
        'AssembleWith': 'Line'
    },
    TransformResources={
        'InstanceType': 'ml.m4.xlarge',
        'InstanceCount': 1
    }
    )
