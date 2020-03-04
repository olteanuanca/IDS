import os
import time
import numpy as np
import pandas as pd
from sklearn.decomposition import PCA
from sklearn.preprocessing import StandardScaler
import tensorflow as tf
import tensorflow.config as tfc
import tensorflow.data as tfd
import tensorflow.python as tf

csv_header = 'dst_port,protocol,timestamp,flow_duration,tot_fwd_pkts,tot_bwd_pkts,totlen_fwd_pkts,' \
             'totlen_bwd_pkts,fwd_pkt_len_max,fwd_pkt_len_min,fwd_pkt_len_mean,fwd_pkt_len_std,bwd_pkt_len_max,' \
             'bwd_pkt_len_min,bwd_pkt_len_mean,bwd_pkt_len_std,flow_byts_s,flow_pkts_s,flow_iat_mean,' \
             'flow_iat_std,flow_iat_max,flow_iat_min,fwd_iat_tot,fwd_iat_mean,fwd_iat_std,fwd_iat_max,' \
             'fwd_iat_min,bwd_iat_tot,bwd_iat_mean,bwd_iat_std,bwd_iat_max,bwd_iat_min,fwd_psh_flags,' \
             'bwd_psh_flags,fwd_urg_flags,bwd_urg_flags,fwd_header_len,bwd_header_len,fwd_pkts_s,bwd_pkts_s,' \
             'pkt_len_min,pkt_len_max,pkt_len_mean,pkt_len_std,pkt_len_var,fin_flag_cnt,syn_flag_cnt,' \
             'rst_flag_cnt,psh_flag_cnt,ack_flag_cnt,urg_flag_cnt,cwe_flag_count,ece_flag_cnt,down_up_ratio,' \
             'pkt_size_avg,fwd_seg_size_avg,bwd_seg_size_avg,fwd_byts_b_avg,fwd_pkts_b_avg,fwd_blk_rate_avg,' \
             'bwd_byts_b_avg,bwd_pkts_b_avg,bwd_blk_rate_avg,subflow_fwd_pkts,subflow_fwd_byts,subflow_bwd_pkts,' \
             'subflow_bwd_byts,init_fwd_win_byts,init_bwd_win_byts,fwd_act_data_pkts,fwd_seg_size_min,' \
             'active_mean,active_std,active_max,active_min,idle_mean,idle_std,idle_max,idle_min,label'

MULTICLASS_DICT = {'Benign': 0, 'Bot': 1, 'DoS attacks-Hulk': 2, 'DoS attacks-SlowHTTPTest': 3, 'SQL Injection': 4,
                   'DDoS attacks-LOIC-HTTP': 5, 'Infilteration': 66, 'DoS attacks-GoldenEye': 7,
                   'DoS attacks-Slowloris': 8, 'Brute Force -Web': 9, 'Brute Force -XSS': 10,
                   'FTP-BruteForce': 11, 'SSH-Bruteforce': 12, 'DDOS attack-HOIC': 13, 'DDOS attack-LOIC-UDP': 14}
BINCLASS_DICT = {'Benign': 0, 'Bot': 1, 'DoS attacks-Hulk': 1, 'DoS attacks-SlowHTTPTest': 1, 'SQL Injection': 1,
                 'DDoS attacks-LOIC-HTTP': 1, 'Infilteration': 1, 'DoS attacks-GoldenEye': 1,
                 'DoS attacks-Slowloris': 1, 'Brute Force -Web': 1, 'Brute Force -XSS': 1,
                 'FTP-BruteForce': 1, 'SSH-Bruteforce': 1, 'DDOS attack-HOIC': 1, 'DDOS attack-LOIC-UDP': 1}
LABELS = ['Benign', 'Bot', 'DoS attacks-Hulk', 'DoS attacks-SlowHTTPTest', 'SQL Injection', 'DDoS attacks-LOIC-HTTP',
          'Infilteration', 'DoS attacks-GoldenEye', 'DoS attacks-Slowloris', 'Brute Force -Web', 'Brute Force -XSS',
          'FTP-BruteForce', 'SSH-Bruteforce', 'DDOS attack-HOIC', 'DDOS attack-LOIC-UDP']

CSV_COLUMNS = csv_header.split(sep=",")
csv_out = 'dataset.csv'
# csv_dir = r'L:\preprocessed'

csv_localdir = os.getcwd()


def mergecsv(csv_dir):
    csv_list = csvlist()

    csv_merge = open(csv_dir + '\\'+csv_out, 'w')
    csv_merge.write(csv_header)
    csv_merge.write('\n')

    for file in csv_list:
        csv_in = open(file)
        for line in csv_in:
            if line.startswith(csv_header):
                continue
            csv_merge.write(line)
        csv_in.close()

    csv_merge.close()
    print('Files merged in CSV file : ' + csv_out)


def csvlist(csv_dir):
    dir_tree = os.walk(csv_dir)
    for dirpath, dirnames, filenames in dir_tree:
        pass

    csv_list = []
    for file in filenames:
        if file.endswith('.csv'):
            csv_list.append(csv_dir + '\\' + file)

    return csv_list


def unique(list1):
    x = np.array(list1)
    x = np.unique(x)
    return x


def getlabels():
    df = pd.read_csv(r'L:/Dataset/dataset.csv', usecols=['label'])
    labelcol = df['label']
    labels = unique(labelcol).tolist()
    return labels


def create_labeldict(df):
    labels = getlabels(df)
    labeldict = {}
    for i in range(0, len(labels)):
        labeldict[i] = labels[i]
    return labeldict


def getcsv(index,dataset_dir):
    csv_list = csvlist(dataset_dir)
    data = pd.read_csv(csv_list[index], low_memory=False)
    # print(data.head())
    return data


def setmulticls_labels(df):
    df["Label"] = df["Label"].apply(
        lambda x: MULTICLASS_DICT[x]
    )
    return df


def setbincls_labels(df):
    df["Label"] = df["Label"].apply(
        lambda x: BINCLASS_DICT[x]
    )
    return df

# getlabels()
