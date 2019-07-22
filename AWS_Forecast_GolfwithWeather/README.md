# Amazon Forecast
Amazon is a powerful ML tools for time series forecasting. And the key point for user-defined is **the quality of input data**
##### Author: Yiran Jing
##### Date: 15-07-2019
### Dataset Description
1. Target time series data: OneStreamGolfData
2. Related time series data: weatherdata
3. Target item: Amount
4. Item id: Region
### EDA and feature engineering [notebook](https://github.com/YiranJing/BigDataAnalysis/blob/master/AWS_Forecast_GolfwithWeather/notebook/EDA_FeatureEngineer-key_region.ipynb)
1. Clean dataset
2. Select main featuers
3. Log transformation
4. Stationary transformation with augmented Dickey fuller test

### AWS forecast [notes](https://github.com/YiranJing/BigDataAnalysis/blob/master/AWS_Forecast_GolfwithWeather/Amazon_Forecast_notes.pdf)
Modeling, Deploying, and Forecasting using Amazon forecast console.


### DeepAR+ Recipe [details](https://docs.aws.amazon.com/forecast/latest/dg/aws-forecast-recipe-deeparplus.html#aws-forecast-recipe-deeparplus-how-it-works)
Amazon Forecast DeepAR+ is a supervised learning algorithm for forecasting scalar (one-dimensional) time series using recurrent neural networks (RNNs). Classical forecasting methods, such as autoregressive integrated moving average (ARIMA) or exponential smoothing (ETS), fit a single model to each individual time series, and then use that model to extrapolate the time series into the future. 
A DeepAR+ model is trained by randomly sampling several training examples from each of the time series in the training dataset. Each training example consists of a pair of adjacent context and prediction windows with fixed predefined lengths.
##### Advantage of DeepAR+ algo
1. When your dataset contains hundreds of feature time series, the DeepAR+ recipe outperforms the standard ARIMA and ETS methods. (Standard forecasting algorithms, such as ARIMA or ETS, might provide more accurate results on a single time series.)
2. Each target time series can also be associated with a number of categorical features, since you can combine muptiple related time series data to it. 
3. The target time series might contain missing values(break point). DeepAR+ supports only feature time series that are known in the future. This allows you to run counterfactual "what-if" scenarios.
4. Good for learning time-dependent patterns, such as spikes during weekends, as DeepAR+ automatically creates feature time series based on time-series granularity.
5. No stationary process before: To capture seasonality patterns, DeepAR+ also automatically feeds lagged (past period) values from the target time series. 

### Amazon SageMaker DeepAR [AWS blog](https://aws.amazon.com/blogs/machine-learning/now-available-in-amazon-sagemaker-deepar-algorithm-for-more-accurate-time-series-forecasting/)
The Amazon Forecast DeepAR+ algorithm improves upon the Amazon SageMaker DeepAR algorithm with the following new features:
1. Learning rate scheduling
2. Model averaging
3. Weighted sampling



### Further discuss about model choice


### [About Me](https://github.com/YiranJing/AboutMe/blob/master/README.md) 🌱
