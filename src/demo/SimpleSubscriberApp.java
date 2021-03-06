package demo;
/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/


import java.io.File;
import java.nio.file.Files;
import java.util.Properties;

import org.apache.edgent.connectors.mqtt.MqttConfig;
import org.apache.edgent.connectors.mqtt.MqttStreams;
import org.apache.edgent.console.server.HttpServer;
import org.apache.edgent.providers.development.DevelopmentProvider;
import org.apache.edgent.topology.TStream;
import org.apache.edgent.topology.Topology;

/**
 * A simple MQTT subscriber topology application.
 */
public class SimpleSubscriberApp {
    private final Properties props;
    private final String topic;

    public static void main(String[] args) throws Exception {
        if (args.length != 1)
            throw new Exception("missing pathname to mqtt.properties file");
        SimpleSubscriberApp subscriber = new SimpleSubscriberApp(args[0]);
        subscriber.run();
    }

    /**
     * @param mqttPropsPath pathname to properties file
     */
    SimpleSubscriberApp(String mqttPropsPath) throws Exception {
        props = new Properties();
        props.load(Files.newBufferedReader(new File(mqttPropsPath).toPath()));
        topic = props.getProperty("mqtt.topic");
    }
    
    private MqttConfig createMqttConfig() {
        MqttConfig mqttConfig = MqttConfig.fromProperties(props);
        return mqttConfig;
    }
    
    /**
     * Create a topology for the subscriber application and run it.
     */
    private void run() throws Exception {
        DevelopmentProvider tp = new DevelopmentProvider();
        
        // build the application/topology
        
        Topology t = tp.newTopology("mqttSampleSubscriber");
        
        // System.setProperty("javax.net.debug", "ssl"); // or "all"; "help" for full list

        // Create the MQTT broker connector
        MqttConfig mqttConfig = createMqttConfig();
        MqttStreams mqtt = new MqttStreams(t, () -> mqttConfig);
        
        // Subscribe to the topic and create a stream of messages
        TStream<String> msgs = mqtt.subscribe(topic, 0/*qos*/);
        TStream<String> msgCopy = msgs.asString();
        // Process the received msgs - just print them out
        msgs.sink(tuple -> System.out.println(
                String.format("received: %s", tuple)));
        TStream<Integer> temps = msgCopy.map(v -> (int) Double.parseDouble(v));

        mqtt.publish(temps.asString(), "mqttSampleTopic2", 0, false);

        
        // run the application / topology
        System.out.println("Console URL for the job: "
                + tp.getServices().getService(HttpServer.class).getConsoleUrl());
        tp.submit(t);
    }

}
