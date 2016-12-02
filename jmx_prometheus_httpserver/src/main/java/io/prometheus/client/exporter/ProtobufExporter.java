package io.prometheus.client.exporter;

import com.google.protobuf.CodedOutputStream;
import io.prometheus.client.Collector;
import io.prometheus.client.Metrics;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.logging.Logger;


/**
 * Created by pbarna on 11/29/16.
 */
public class ProtobufExporter {

    private static final Logger LOGGER = Logger.getLogger(ProtobufExporter.class.getName());

    /**
     * Write out the protobuf version the given MetricFamilySamples.
     */
    public static Integer write(OutputStream out, Enumeration<Collector.MetricFamilySamples> mfs) throws IOException {
        Integer contentLength = 0;
        for (Collector.MetricFamilySamples metricFamilySamples: Collections.list(mfs)) {
            Metrics.MetricFamily.Builder metrics = Metrics.MetricFamily.newBuilder();
            metrics.setHelp(metricFamilySamples.help);
            metrics.setType(convertType(metricFamilySamples.type));
            for (Collector.MetricFamilySamples.Sample sample: metricFamilySamples.samples) {

                Metrics.Metric.Builder metric = Metrics.Metric.newBuilder();
                //todo what to do with sample.name ?
                if (sample.labelNames.size() > 0) {
                    for (int i = 0; i < sample.labelNames.size(); ++i) {
                        Metrics.LabelPair.Builder label = Metrics.LabelPair.newBuilder();
                        label.setName(sample.labelNames.get(i));
                        label.setValue(sample.labelValues.get(i));
                        metric.addLabel(label.build());
                   }
                }
                switch (metricFamilySamples.type) {
                    case GAUGE:
                        metric.setGauge(Metrics.Gauge.newBuilder().setValue(sample.value).build());
                    case COUNTER:
                        metric.setCounter(Metrics.Counter.newBuilder().setValue(sample.value).build());
                }
                metrics.addMetric(metric.build());
            }
            //see https://github.com/prometheus/prometheus/blob/master/vendor/github.com/matttproud/golang_protobuf_extensions/pbutil/encode.go
            //or see implementation of the writeDelimitedTo method
            Metrics.MetricFamily messageFragment = metrics.build();
            int serialized = messageFragment.getSerializedSize();
            contentLength += CodedOutputStream.computeRawVarint32Size(serialized) + serialized;
            messageFragment.writeDelimitedTo(out);
        }
        return contentLength;
    }


    /**
     * Content-type for protobuf version
     */
    public final static String CONTENT_TYPE_PROTOOBUF =
            "application/vnd.google.protobuf; proto=io.prometheus.client.MetricFamily; encoding=delimited";

    static Metrics.MetricType convertType(Collector.Type t){
        switch (t) {
            case GAUGE:
                return Metrics.MetricType.GAUGE;
            case COUNTER:
                return Metrics.MetricType.COUNTER;
            case SUMMARY:
                return Metrics.MetricType.SUMMARY;
            case HISTOGRAM:
                return Metrics.MetricType.HISTOGRAM;
            default:
                return Metrics.MetricType.UNTYPED;
        }

    }

}
