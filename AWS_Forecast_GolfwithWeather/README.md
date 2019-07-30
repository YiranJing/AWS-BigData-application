# Amazon Forecast
Amazon is a powerful ML tools for time series forecasting. 
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
New APIs after 7/23/19. New Featuers:
1. Forecast dimensions - Selective keys to generate forecasting. (Item_id by default: the only choice in old version).
2. Country for holidays - the holoday calendar want to include in models.
3. Number of backtest windows - The number of times that the algorithm splits the input data for use in training and evaluation. **Backtesting**: Backtesting assesses the viability of a model by discovering how it would play out using historical data. Backtesting can be an important step in optimizing your trading strategy.
4. Backetest window offset - The point in the dataset where you want to splitmthe data for model training and validation
5. Training subsample ratio(between 0 and 1): The percentage of items as the training data. 



### DeepAR+ Recipe [details](https://docs.aws.amazon.com/forecast/latest/dg/aws-forecast-recipe-deeparplus.html#aws-forecast-recipe-deeparplus-how-it-works)
Amazon Forecast DeepAR+ is a supervised learning algorithm for forecasting scalar (one-dimensional) time series using recurrent neural networks (RNNs). Classical forecasting methods, such as autoregressive integrated moving average (ARIMA) or exponential smoothing (ETS), fit a single model to each individual time series, and then use that model to extrapolate the time series into the future. 
A DeepAR+ model is trained by randomly sampling several training examples from each of the time series in the training dataset. Each training example consists of a pair of adjacent context and prediction windows with fixed predefined lengths.
#### Advantage of DeepAR+ algo
1. (Main motivation)When your dataset contains hundreds of feature time series, the **DeepAR+ recipe outperforms the standard ARIMA and ETS methods**. (Standard forecasting algorithms, such as ARIMA or ETS, might provide more accurate results on a single time series.)
2. Each target time series can also be associated with a number of categorical features, since you can combine muptiple related time series data to it. 
3. The target time series might contain missing values(break point). DeepAR+ supports only feature time series that are known in the future. This allows you to run counterfactual "what-if" scenarios.
4. Good for learning time-dependent patterns, such as spikes during weekends, as DeepAR+ automatically creates feature time series based on time-series granularity.
5. No stationary process before: To capture seasonality patterns, DeepAR+ also automatically feeds lagged (past period) values from the target time series. 

### Amazon SageMaker DeepAR [AWS blog](https://aws.amazon.com/blogs/machine-learning/now-available-in-amazon-sagemaker-deepar-algorithm-for-more-accurate-time-series-forecasting/)
The Amazon Forecast DeepAR+ algorithm improves upon the Amazon SageMaker DeepAR algorithm with the following new features:
1. Learning rate scheduling
2. Model averaging
3. Weighted sampling
- [Univarate DeepAR+ Notebook](https://github.com/YiranJing/BigDataAnalysis/blob/master/AWS_Forecast_GolfwithWeather/notebook/Univariate_DeepAR_Train_Deploy_Validation.ipynb) Target time series data only
- [Bivariate DeepAR+ Notebook](https://github.com/YiranJing/BigDataAnalysis/blob/master/AWS_Forecast_GolfwithWeather/notebook/Bivariate_DeepAR_Train_Deploy_Validation.ipynb)  Target time series and one related time series data
- [Multivarate DeepAR+ Notebook](https://github.com/YiranJing/BigDataAnalysis/blob/master/AWS_Forecast_GolfwithWeather/notebook/Multivariate_DeepAR_Train_Deploy_Validation.ipynb) Target time series and five related time series data
##### Process:
- Create input data(**Json format**), can combine muptiple realated time series data. See example [The Json format of target time series data with five related time series](https://github.com/YiranJing/BigDataAnalysis/blob/master/AWS_Forecast_GolfwithWeather/data/GolfDataforecast/MultivariateDeepAR/Golf_all_weatherdata/test/test.json). the code converting CSV to JOSN is in [Multivariate DeepAR notebook](https://github.com/YiranJing/BigDataAnalysis/blob/master/AWS_Forecast_GolfwithWeather/notebook/Multivariate_DeepAR_Train_Deploy_Validation.ipynb)
- Train and Deploy model
- Validation: MAPE and plot visualization.

###### Univariate Time Series Model:
Using target time series data only for modelling. That is, use only past observations to predict itself in the future. In our example, our goal is predicting amount for each region, and thus, we only need dataset include three columns: month, region, amount.

###### Bivariate Time Series Model:
Using target time series and one additional related time series data for modelling. In our case, we can use rainfall information for each month and each region to help our predictions for amount in each region each month.

###### Multivariate Time Series Model:
Using target time series and more than one additional related time series data for modelling. In our case, we can use rainfall, snowfall information for each month and each region to help our predictions for amount in each region each month.


### Further discuss about model choice

### [About Me](https://github.com/YiranJing/AboutMe/blob/master/README.md) 🌱
