from pyscripts.data import *
from sklearn.preprocessing import StandardScaler, MinMaxScaler
from sklearn.impute import SimpleImputer
import numpy as np


def minmax_scaler(dataframe, col_names):
    cols_to_scale = dataframe.columns.difference(['Label'])
    scaler = MinMaxScaler()
    dataframe[cols_to_scale] = scaler.fit_transform(dataframe[cols_to_scale])
    # dataframe = pd.DataFrame(dataframe, columns=col_names)
    return dataframe


def std_scaler(dataframe, col_names):
    cols_to_scale = dataframe.columns.difference(['Label'])
    scaler = StandardScaler()
    dataframe[cols_to_scale] = scaler.fit_transform(dataframe[cols_to_scale])
    # dataframe = pd.DataFrame(dataframe, columns=col_names)
    return dataframe


def cast_data(dataframe):
    cols_to_cast = dataframe.columns.difference(['Timestamp', 'Label'])
    dataframe[cols_to_cast] = dataframe[cols_to_cast].astype(dtype='float64')
    return dataframe


def replace_nan(dataframe):
    inf_columns = []
    inf_columns = [c for c in dataframe.columns if dataframe[dataframe[c] == np.inf][c].count() > 0]
    for col in inf_columns:
        dataframe[col].replace(to_replace=np.inf, value=np.nan, inplace=True)
        pd.to_numeric(dataframe[col], errors='coerce')
        dataframe[col] = dataframe[col].astype(dtype='float64')
        mean = dataframe[col].mean(skipna=True)
        dataframe[col].replace(to_replace=np.nan, value=mean, inplace=True)

    return dataframe
