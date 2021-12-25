import numpy as np
from sklearn.linear_model import LogisticRegression

import mlflow
import mlflow.sklearn

import requests

#dataSetId = -6764401216520805130
#token = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IlNlV1JQRlJraWZSNWpwMndOSFliSCJ9.eyJpc3MiOiJodHRwczovL2FwcGdhbGxhYnMudXMuYXV0aDAuY29tLyIsInN1YiI6IlBBbERla0FvbzBYV2pBaWNVOVNRREtneTdCMHkycDJ0QGNsaWVudHMiLCJhdWQiOiJodHRwczovL2FwcGdhbGxhYnMudXMuYXV0aDAuY29tL2FwaS92Mi8iLCJpYXQiOjE2MDEzMjY5MzIsImV4cCI6MTYwMTQxMzMzMiwiYXpwIjoiUEFsRGVrQW9vMFhXakFpY1U5U1FES2d5N0IweTJwMnQiLCJzY29wZSI6InJlYWQ6Y2xpZW50X2dyYW50cyBjcmVhdGU6Y2xpZW50X2dyYW50cyBkZWxldGU6Y2xpZW50X2dyYW50cyIsImd0eSI6ImNsaWVudC1jcmVkZW50aWFscyJ9.eGSMpm1P2rSFFP6s0x4_csKrDSSd8PTko-hHyETSILt9bB6Q0y7u8ky6yOl1piG9RBd5LW7Hy_R_T0bWJjRGEmZ7yUYkaIKafw4oTDpvPOC9T6CeJHcYgaCuroOMSFQhmk9LfZflnl4ODCt21yr4WrI8Teeh8YK5jZ6o8gsk-XrtKISEp04c0GHzBzaMvd5dmBCzSAhFifq3IzvJKkSL5WdXaAsdPgP_BVm5vTYMwTPEm05Cd6E-5S3pPykLO7APKk8s1kLeXSvXnAPkX6y1pbCfZz7dUvHz-fLgMMhx2PUkS_8wM3N2wSZ7rni6MZ3TM7kmqgQy_9SPtOnWSuZfZg"
#clientId = "PAlDekAoo0XWjAicU9SQDKgy7B0y2p2t"
#url = "http://localhost:8080/dataset/readDataSet/?dataSetId="+str(dataSetId)
#myResponse = requests.get(url, headers={'Bearer': token,'Principal':clientId})
#json = myResponse.json()
#csv = json.get("data")
#print(csv)

if __name__ == "__main__":
    X = np.array([-2, -1, 0, 1, 2, 1]).reshape(-1, 1)
    y = np.array([0, 0, 1, 1, 1, 0])
    lr = LogisticRegression()
    lr.fit(X, y)
    score = lr.score(X, y)
    print("Score: %s" % score)
    mlflow.log_metric("score", score)
    mlflow.sklearn.log_model(lr, "model")
    print("Model saved in run %s" % mlflow.active_run().info.run_uuid)
    output = '{"score":%s}' % score
    output += '{"dataSet":%s}' % dataSetId
    output += '{"modelId":%s}' % modelId
    print(output)