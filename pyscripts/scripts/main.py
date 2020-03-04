from pyscripts.data import *
from pyscripts.preprocess import *
from pyscripts.reduce import *
from pyscripts.train import *

for i in range(0, len(csvlist(r'L:\Dataset'))):
    df = getcsv(i, r'L:\Dataset')
    df = setbincls_labels(df)
    df = drop_irrelevant(df)
    df = cast_data(df)
    df = replace_nan(df)
    df = df.round(decimals=3)
    df = std_scaler(df, CSV_COLUMNS)
    df = run_pca(df)
    df.to_csv(r'L:\preprocessed\data' + str(i) + '.csv', index='False')
mergecsv(r'L:\preprocessed')
load_data(r'L:\preprocessed\dataset.csv')
knncv()

