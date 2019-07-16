######################################################
#   Lambda Functions for Customer Chrun Example
#   Author: Yiran Jing
#   Date:04-07-2019
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

## inport UDF for lambda function
from help_function_lambda import read_csv
from help_function_lambda import prediction_probability
from help_function_lambda import predicted_label
from help_function_lambda import write_out_s3

ENDPOINT_NAME = os.environ['ENDPOINT_NAME'] # access environment variable values
runtime= boto3.client('runtime.sagemaker') # A low-level client representing Amazon SageMaker Runtime
s3 = boto3.resource('s3')
bucket = s3.Bucket('taysolsdev')
prefix = 'datasets/churn'




"""
Make probability predictions for each observation and then write out the predictions to S3 bucket.
The main steps are described in UDF: look help_function_lambda.py

Parameters:
------------
    event: dict type with JSON format
        AWS Lambda uses this parameter to pass in event data to the handle
    context: Lambda Context type
        AWS Lambda uses this parameter to provide runtime information to your handler

Returns:
--------
    predictions_probability:
        the new list with predicted probabilities for each observation
"""
def lambda_handler(event, context):
    test_data_input = read_csv(event) # UDF to read in csv dataset
    # you can print out the dataset to check
    # print(test_data_input)
    predictions_probability = prediction_probability(test_data_input)
    # write out to s3 bucket
    write_out_s3(event, predictions_probability)
    return predictions_probability



 help_function_lambda
