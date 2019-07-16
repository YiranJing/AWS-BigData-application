######################################################
#   Lambda Functions for Customer Chrun Example
#   Author: Yiran Jing
#   Date:16-07-2019
#####################################################


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
"""
In other use case, you can only modify the following variables and then copy left parts to your lambda function.
bucket, output_key, batch_output, Modelname, transformJobName.
"""
bucket = 'taysolsdev'
output_key = 'datasets/churn/output/'
client = boto3.client('sagemaker')
batch_output = 's3://{}/{}'.format(bucket, output_key) # specify the location of batch output
Modelname = 'xgboost-2019-07-16-00-26-47-648' # the model name we already have
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
