db = connect('mongodb://localhost/mil');

db.pspconf.insertOne(
    {
        "_id": "4585625",
        "pspConfiguration": {
            "pspBroker": "97735020584",
            "pspId": "AGID_01",
            "pspPassword": "pwd_AgID"
        }
    }
);

printjson( db.pspconf.find( {} ) );