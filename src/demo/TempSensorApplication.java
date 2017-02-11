package demo;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.edgent.providers.direct.DirectProvider;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;

import java.io.File;
import java.nio.file.Files;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.edgent.connectors.mqtt.MqttConfig;
import org.apache.edgent.connectors.mqtt.MqttStreams;
import org.apache.edgent.console.server.HttpServer;
import org.apache.edgent.providers.development.DevelopmentProvider;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;

public class TempSensorApplication {
    public static void main(String[] args) throws Exception {
        TempSensor sensor = new TempSensor();
        DirectProvider dp = new DirectProvider();
        Topology topology = dp.newTopology();
        TStream<Double> tempReadings = topology.poll(sensor, 1, TimeUnit.MILLISECONDS);
        TStream<Double> filteredReadings = tempReadings.filter(reading -> reading < 50 || reading > 80);

        filteredReadings.print();
                
        
        dp.submit(topology);
    }
}