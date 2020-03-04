import pandas as pd
from sklearn.decomposition import PCA


def drop_irrelevant(dataframe):
    to_remove = ['Timestamp', 'Dst Port']
    dataframe = dataframe.drop(labels=to_remove, axis=1)
    if 'Flow ID' in dataframe.columns:
        dataframe = dataframe.drop(labels=['Flow ID'], axis=1)
    if 'Src IP' in dataframe.columns:
        dataframe = dataframe.drop(labels=['Src IP'], axis=1)
    if 'Src Port' in dataframe.columns:
        dataframe = dataframe.drop(labels=['Src Port'], axis=1)
    if 'Dst Port' in dataframe.columns:
        dataframe = dataframe.drop(labels=['Dst Port'], axis=1)
    if 'Dst IP' in dataframe.columns:
        dataframe = dataframe.drop(labels=['Dst IP'], axis=1)
    return dataframe


def non_unique_drop(dataframe):
    # Removing columns without enough unique values
    to_remove = []
    for col in dataframe.columns:
        if len(dataframe[col].unique()) == 1:
            to_remove.append(col)
    dataframe = dataframe.drop(labels=to_remove, axis=1)
    return dataframe


def run_pca(dataframe):
    # pca = PCA(n_components='mle',svd_solver='full')
    # pca = PCA()
    # X = dataframe.values[:, :-1]
    # print(X.shape)
    # pca.fit(X)
    # red_cols = ['pca_%i' % i for i in range(pca.n_components_)]
    # labelcol = dataframe['Label']
    # dataframe = pd.DataFrame(pca.transform(
    #     X), columns=red_cols, index=dataframe.index)
    # dataframe = pd.concat([dataframe, labelcol], axis=1)
    pca = PCA(.95)
    pca.fit_transform(dataframe)
    return dataframe
