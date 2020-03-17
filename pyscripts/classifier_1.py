import socket
from sklearn import metrics
from sklearn.metrics import classification_report, confusion_matrix
import joblib
import pandas as pd
from csv import reader
import numpy as np
from sklearn.preprocessing import StandardScaler

# setup connection
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_addr = ('127.0.0.1', 3500)
sock.bind(server_addr)
sock.listen(5)
count = 0

BINCLASS_DICT = {'Benign': 0, 'Bot': 1, 'DoS attacks-Hulk': 1, 'DoS attacks-SlowHTTPTest': 1, 'SQL Injection': 1,
                 'DDoS attacks-LOIC-HTTP': 1, 'Infilteration': 1, 'DoS attacks-GoldenEye': 1,
                 'DoS attacks-Slowloris': 1, 'Brute Force -Web': 1, 'Brute Force -XSS': 1,
                 'FTP-BruteForce': 1, 'SSH-Bruteforce': 1, 'DDOS attack-HOIC': 1, 'DDOS attack-LOIC-UDP': 1}
COLS = 'flow_id,src_ip,src_port,dst_ip,dst_port,protocol,timestamp,flow_duration,tot_fwd_pkts,' \
       'tot_bwd_pkts,totlen_fwd_pkts,' \
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
COLS = list(COLS.split(sep=","))
# load model
# reload saved model
filename = r'L:\MLModels\KNNsimple_model.sav'
classifier = joblib.load(filename)


def classify(chunk):
    y_pred = classifier.predict(chunk)
    # print(confusion_matrix(chunk, y_pred))
    # print(classification_report(chunk, y_pred))
    # print("Accuracy:", metrics.accuracy_score(chunk, y_pred))
    print(y_pred)

    return y_pred


def process(msg):
    msg_list = list(msg.split(sep=','))

    chunk = pd.DataFrame([msg_list], columns=COLS)
    chunk = chunk.drop(labels=['flow_id', 'src_ip', 'src_port', 'dst_ip', 'dst_port', 'timestamp', 'label'], axis=1)

    chunk.iloc[:, :-1] = chunk.iloc[:, :-1].astype(np.float64)
    # cols_to_cast = chunk.columns.difference(['Timestamp', 'Label'])
    # dataframe[cols_to_cast] = dataframe[cols_to_cast].astype(dtype='float64')

    chunk = chunk.replace([np.inf, -np.inf], np.nan)
    chunk = chunk.replace('Infinity', np.nan)
    chunk = chunk.fillna(chunk.mean())
    chunk = chunk.fillna(0)
    scaler = StandardScaler()
    chunk.iloc[:, :-1] = scaler.fit_transform(chunk.iloc[:, :-1])

    chunk = chunk.round(decimals=3)

    if ',,' in chunk:
        chunk = chunk.replace(',,', ',NaN,')
    return chunk


while True:
    conn, addr = sock.accept()
    print("Got connection from", addr)

    msg = conn.recv(1024).decode("UTF-8")

    if msg != '':
        chunk = process(msg)
        print(chunk.iloc[0, :])
        result = classify(chunk)
        # if result[0] == 1:
        sendmsg=result[0]
        conn.send(sendmsg)
        # print(msg)
        count = count + 1
        print(count)

    msg = ''
