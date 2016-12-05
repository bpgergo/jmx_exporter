package io.prometheus.jmx;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.ProtobufExporter;
import io.prometheus.client.exporter.TextExporter;
//import io.prometheus.client.exporter.common.TextFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * todo docs
 */
public class ProtobuffMetricsServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(ProtobuffMetricsServlet.class.getName());
    private CollectorRegistry registry;

    public ProtobuffMetricsServlet() {
        this(CollectorRegistry.defaultRegistry);
    }

    public ProtobuffMetricsServlet(CollectorRegistry registry) {
        this.registry = registry;
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String header = req.getHeader("Accept");
        LOGGER.severe("header accept:"+header);
        if (header.equals(ProtobufExporter.CONTENT_TYPE_PROTOOBUF)) {
            resp.setContentType(ProtobufExporter.CONTENT_TYPE_PROTOOBUF);
            OutputStream out = resp.getOutputStream();
            Integer length = ProtobufExporter.write(out, this.registry.metricFamilySamples());
            if (length != null && length > 0){
                resp.setContentLength(length);
                LOGGER.severe("content lenght:" + length);
            }
            out.flush();
            out.close();
        } else {
            resp.setContentType(TextExporter.CONTENT_TYPE_004);
            PrintWriter writer = resp.getWriter();
            TextExporter.write004(writer, this.registry.metricFamilySamples());
            writer.flush();
            writer.close();
        }
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doGet(req, resp);
    }

}
