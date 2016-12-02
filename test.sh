echo "protobuf---------------------------------"
curl  -vv -s -i -H "Accept: application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited" localhost:5556/metrics
echo "text-------------------------------------"
curl  -vv -s -i localhost:5556/metrics

