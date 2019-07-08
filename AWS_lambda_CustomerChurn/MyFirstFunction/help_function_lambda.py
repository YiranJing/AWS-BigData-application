######################################################
#   Help Functions for Lmabda (Customer Chrun Example)
#   Author: Yiran Jing
#   Date:04-07-2019
#####################################################

import json
import boto3
import csv
import os
import io
import logging
from botocore.exceptions import ClientError
from pprint import pprint
from time import strftime, gmtime
from json import dumps, loads, JSONEncoder, JSONDecoder
from six.moves import urllib

ENDPOINT_NAME = os.environ['ENDPOINT_NAME'] # access environment variable values
runtime= boto3.client('runtime.sagemaker') # A low-level client representing Amazon SageMaker Runtime
s3 = boto3.resource('s3')
bucket = s3.Bucket('taysolsdev')
prefix = 'datasets/churn'


"""
Read in new csv dataset for predictions

Parameters:
------------
    event: dict type with JSON format
        AWS Lambda uses this parameter to pass in event data to the handle

Returns:
--------
    test_data_input:
        the new dataset that will be used for prediction
"""
def read_csv(event):
  # retrieve bucket name and file_key from the S3 event
  bucket_name = event['Records'][0]['s3']['bucket']['name'] # should be used
  #file_key = urllib.parse.unquote_plus(event['Records'][0]['s3']['object']['input_key'])
  file_key = urllib.parse.unquote_plus(event['Records'][0]['s3']['object']['key'])
  # get the file object
  obj = s3.Object(bucket_name, file_key)
  # get lines inside the csv
  lines = obj.get()['Body'].read().split(b'\n')
  # Read in CSV file
  test_data_input = lines[0].decode() # first row
  for r in lines[1:]:
    test_data_input = test_data_input + '\n' + r.decode()  # we need to decode for each row
  return test_data_input



"""
Make the prediction of probability for each observation and threshold (0.5 by default )

Parameters:
------------
    test_data_input:
        the new dataset that will be used for prediction

Returns:
--------
    predictions_probability:
        the new list with predicted probabilities for each observation
"""
def prediction_probability(test_data_input):
    response = runtime.invoke_endpoint(EndpointName=ENDPOINT_NAME,
                                    ContentType='text/csv',
                                    Body=test_data_input)
    # get the list of predictions
    predictions_probability=  response['Body'].read().decode("utf-8").split(",")  # we must decode explicitly as "utf-8"
    return predictions_probability



"""
Predict the label for each observation based on predicted probability and threshold (0.5 by default )

Parameters:
------------
    predictions_probability: list
        the list of predicted probabilities for each observation
    threshold: (optional) a float
        the threshold we want to use for label decision. 0.5 by default

Returns:
--------
    predictions_label:
        the new list with predicted labels
"""
def predicted_label(predictions_probability, threshold = 0.5):
    predictions_label=[0 if float(x) < threshold else 1 for x in predictions_probability]
    return predictions_label



"""
Write out the predictions to S3 bucket

Parameters:
------------
    predictions_probability: list
        the list of predicted probabilities for each observation
    event: dict type with JSON format
        AWS Lambda uses this parameter to pass in event data to the handle
"""
def write_out_s3(event, predictions_probability):
    # the output data must be bytes-like object, and split by '\n'
    result = predictions_probability[0]
    for item in predictions_probability[1:]:
        result = result + "\n" + item
    output_bytes = bytes(result.encode('UTF-8'))
    # get the output bucket
    bucket_name = event['Records'][0]['s3']['bucket']['name'] # should be used
    #new_key = urllib.parse.unquote_plus(event['Records'][0]['s3']['object']['output_key'])

    ## currently, write back to inoput datafile
    bucket_name = event['Records'][0]['s3']['bucket']['name']
    file_key = urllib.parse.unquote_plus(event['Records'][0]['s3']['object']['key'])
    new_object = s3.Object(bucket_name, file_key)
    new_object.put(Body=output_bytes)
