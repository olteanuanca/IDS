# KNN
import joblib
from sklearn import metrics
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import confusion_matrix, classification_report
from sklearn.model_selection import cross_val_score, GridSearchCV, train_test_split
from sklearn.neighbors import KNeighborsClassifier
import numpy as np
from sklearn.svm import SVC
import pandas as pd


# run main.py
# load merged dataframe
from sklearn.tree import DecisionTreeClassifier


def load_data(df_dir):
    global x_train
    global y_train
    global x_test
    global y_test
    df = pd.read_csv(df_dir).drop(['index'], axis=1)
    y = df['label']
    df = df.drop(labels=['label'], axis=1)
    x_train, x_test, y_train, y_test = train_test_split(df, y, test_size=0.2)
    print(x_train.shape, y_train.shape)
    print(x_test.shape, y_test.shape)


def knn():
    classifier = KNeighborsClassifier(n_neighbors=5)

    # Cross validation
    cv_scores = cross_val_score(classifier, x_train, y_train.values.ravel(), cv=5)
    # print each cv score (accuracy) and average them
    print(cv_scores)
    print('cv_scores mean:{}'.format(np.mean(cv_scores)))

    classifier.fit(x_train, y_train.values.ravel())
    y_pred = classifier.predict(x_test)

    print(confusion_matrix(y_test, y_pred))
    print(classification_report(y_test, y_pred))
    print("Accuracy:", metrics.accuracy_score(y_test, y_pred))

    # saving model
    filename = 'KNN_model.sav'
    joblib.dump(classifier, filename)
    return classifier


# KNN2
def knncv():
    classifier = KNeighborsClassifier()
    param_grid = {'n_neighbors': np.arange(1, 25)}

    knn_gscv = GridSearchCV(classifier, param_grid, cv=5)

    knn_gscv.fit(x_train, y_train.values.ravel())
    y_pred = classifier.predict(x_test)

    print(confusion_matrix(y_test, y_pred))
    print(classification_report(y_test, y_pred))
    print("Accuracy:", metrics.accuracy_score(y_test, y_pred))
    print(knn_gscv.best_params_)
    print(knn_gscv.best_score_)

    # saving model
    filename = 'KNNcv_model.sav'
    joblib.dump(classifier, filename)
    return classifier


# Decision Tree Classifier
def dectree():
    clf = DecisionTreeClassifier(criterion="entropy", max_depth=20)
    clf = clf.fit(x_train, y_train)

    y_pred = clf.predict(x_test)

    print(confusion_matrix(y_test, y_pred))
    print(classification_report(y_test, y_pred))
    print("Accuracy:", metrics.accuracy_score(y_test, y_pred))

    # saving model
    filename = 'decisionTree_model.sav'
    joblib.dump(clf, filename)

    return clf


# SVM
def svm():
    params_grid = [{'kernel': ['rbf'], 'gamma': [0.001],
                    'C': [1000]}]
    clf = GridSearchCV(SVC(), params_grid, cv=10)
    clf.fit(x_train, y_train.values.ravel())
    print('Best score for training data:', clf.best_score_, "\n")
    print('Best C:', clf.best_estimator_.C, "\n")
    print('Best Kernel:', clf.best_estimator_.kernel, "\n")
    print('Best Gamma:', clf.best_estimator_.gamma, "\n")

    y_pred = clf.predict(x_test)
    print(confusion_matrix(y_test, y_pred))
    print(classification_report(y_test, y_pred))
    print("Accuracy:", metrics.accuracy_score(y_test, y_pred))

    filename = 'SVM_model.sav'
    joblib.dump(clf, filename)

    return clf


# Random Forest
def randforest():
    clf = RandomForestClassifier(n_estimators=100)
    clf.fit(x_train, y_train.values.ravel())
    y_pred = clf.predict(x_test)

    print(confusion_matrix(y_test, y_pred))
    print(classification_report(y_test, y_pred))
    print("Accuracy:", metrics.accuracy_score(y_test, y_pred))

    filename = 'randomForest_model.sav'
    joblib.dump(clf, filename)
    return clf

# to reload saved model
# loaded_model = joblib.load(filename)
# result = loaded_model.score(x_test, y_test)
# print(result)
