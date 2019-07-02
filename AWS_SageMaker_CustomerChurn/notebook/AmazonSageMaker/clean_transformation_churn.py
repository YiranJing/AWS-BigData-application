####
##Script used for churn dataset cleaning and transformation
####

import pandas as pd
import os
import boto3
import re
import json
import sagemaker
import numpy as np
from sagemaker import get_execution_role
from sklearn.metrics import confusion_matrix
from sklearn import metrics
from sagemaker.amazon.amazon_estimator import get_image_uri
from scipy import stats
import xgboost as xgb
import sklearn as sk

bucket = 'taysolsdev'
prefix = 'datasets/churn'

def read_data(data_path):
    df = pd.read_csv(data_path, encoding = "ISO-8859-1")
    df = df.set_index('customerID') # we set customerId as index
    df = df.convert_objects(convert_numeric=True)
    return df

def fill_missing(df, miss_column):
    df[miss_column].fillna((df[miss_column].mean()), inplace=True)
    return df

def bonxcox_transf(data):
    y, lmbda=stats.boxcox(data)
    output=((data**lmbda - 1) / lmbda)
    print("The lmbda is: "+str(lmbda))
    del data # must delete old one, and remember to mention it in report
    return output

# since first column is target for SageMaker XGBoost
def shift_last_column_to_first(df):
    cols = list(df.columns)
    cols = [cols[-1]] + cols[:-1]
    df = df[cols]
    return df

def Cleaning_and_Transformation(data_path):
    df = read_data(data_path)
    df = fill_missing(df, 'TotalCharges')
    df = df.drop_duplicates()
    df = pd.get_dummies(df, drop_first=True)
    df['TotalCharges'] = bonxcox_transf(df['TotalCharges'])
    df = shift_last_column_to_first(df)
    return df

def split_train_validation_test(data_path):
    df = Cleaning_and_Transformation(data_path)
    # split dataset 70%, 20% and 10%
    train_data, validation_data, test_data = np.split(df.sample(frac=1, random_state=1729), [int(0.7 * len(df)), int(0.9 * len(df))])
    return train_data, validation_data, test_data

def save_to_s3(data_path):
    train_data, validation_data, test_data=split_train_validation_test(data_path)
    train_data.to_csv('train.csv', header=False, index=False)
    boto3.Session().resource('s3').Bucket(bucket).Object(os.path.join(prefix, 'train/train.csv')).upload_file('train.csv')

    validation_data.to_csv('validation.csv', header=False, index=False)
    boto3.Session().resource('s3').Bucket(bucket).Object(os.path.join(prefix, 'validation/validation.csv')).upload_file('validation.csv')

    test_data.to_csv('test.csv', header=False, index=False)
    boto3.Session().resource('s3').Bucket(bucket).Object(os.path.join(prefix, 'test/test.csv')).upload_file('test.csv')

    test_for_batch = test_data.iloc[:, 1:] # delete the target column
    test_for_batch.to_csv('test_data_Batch.csv',header=False, index = False)
    boto3.Session().resource('s3').Bucket(bucket).Object(os.path.join(prefix, 'batch/test_data_Batch.csv')).upload_file('test_data_Batch.csv')

def get_train_validation_test_data(data_path):
    save_to_s3(data_path)

    # read in train dataset
    train_path = 's3://taysolsdev/datasets/churn/train/train.csv'
    train_set = pd.read_csv(train_path, encoding = "ISO-8859-1",header=None)

    # read in validation dataset
    valid_path = 's3://taysolsdev/datasets/churn/validation/validation.csv'
    valid_set = pd.read_csv(valid_path, encoding = "ISO-8859-1",header=None)

    # read in test dataset
    test_path = 's3://taysolsdev/datasets/churn/test/test.csv'
    test_set = pd.read_csv(test_path, encoding = "ISO-8859-1",header=None)

    # the batch dataset used for prediction cannot have target column
    batch_input = 's3://taysolsdev/datasets/churn/batch/test_data_Batch.csv' # test data used for prediction

    return train_set, valid_set, test_set, batch_input
