# Walkability Analysis with SQL and Map API.
Perform a walkability analysis for different neighbourhoods in the Greater Sydney area
### Author
- Yiran Jing
- Hongyu He

Semester 1, 2018 

- results: [Report](../WalkabilityAnalysis/report.pdf) 

#### DataSet description
Mainly use four csv datasets of Statistical Area 2 (SA2), provided by Australia Bureau of Statistics (ABS)
- Cleaning data 
 - data cleaning is performed as there are attributes where there were a few observations with missing value
#### Business objective 
Perform a data analysis of the walkability of 312 different neighbourhoods in Sydney

### DB Schema with SQL [NoteBook](../WalkabilityAnalysis/notebook/MajorCode/SQL_Primary_datasets.ipynb)
- Normalised schema with PKs and suitable FKs
- Creating and Loading tables from csv files to DataBase using Python Notebook.
<img width="928" alt="Screen Shot 2019-06-29 at 9 29 35 pm" src="https://user-images.githubusercontent.com/31234892/60383525-50640100-9ab5-11e9-814f-67afb13b6812.png">

### Google Map API 
- The boundary of the neighbourhoods is added as an extra column in the neighbourhoods dataset
- Add longitude and latitude for each records in dataset. [NoteBook](../WalkabilityAnalysis/notebook/MajorCode/New_neighbourhood_goolgeMapAPI.ipynb)

### Precise spatial join with neighbourhood 
Performing the precise spatial join with Sydney2_2016_AUST.shp, to add an extra column called area_id where car pods are located in. [NoteBook](../WalkabilityAnalysis/notebook/MajorCode/Spatial_Join.ipynb)


### Walkability Analysis [NoteBook](../WalkabilityAnalysis/notebook/MajorCode/Walkability_Score_analysis.ipynb)
- 5D analysis based on Z score
- MapVisualization
![MapVisualization](https://user-images.githubusercontent.com/31234892/60383405-8bfdcb80-9ab3-11e9-8ba7-d9d139b067a1.png)

### Correlation Tests and Analysis
