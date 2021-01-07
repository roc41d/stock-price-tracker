package bolt;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import util.Constants;


import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ComputeGainSignalBolt extends BaseRichBolt {

    private PrintWriter writer;
    private OutputCollector collector;

    @Override
    public void prepare(Map<String, Object> stormConf, TopologyContext topologyContext, OutputCollector collector) {
        String fileName = stormConf.get("fileToWrite").toString();
        this.collector = collector;

        try {
            this.writer = new PrintWriter(fileName, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Error opening file ["+ fileName +"]");
        }
    }

    @Override
    public void execute(Tuple input) {
        String company = input.getStringByField(Constants.COMPANY_NAME_FIELD);
        String timestamp = input.getStringByField(Constants.TIMESTAMP_FIELD);
        double price = input.getDoubleByField(Constants.PRICE_FIELD);
        double prevClose = input.getDoubleByField(Constants.PREV_CLOSE_FIELD);

        boolean gain = true;

        if (price <= prevClose) {
            gain = false;
        }

        this.collector.emit(input, new Values(company, timestamp, price, gain));
        this.writer.println(company+ ","+ timestamp+","+price+","+gain);
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
        outputFieldsDeclarer.declare(new Fields(this.getOutputFields()));
    }

    private List<String> getOutputFields() {
        List<String> listOfOutputFields = Arrays.asList(
                Constants.COMPANY_NAME_FIELD,
                Constants.TIMESTAMP_FIELD,
                Constants.PRICE_FIELD,
                Constants.GAIN_FIELD
        );

        return listOfOutputFields;
    }
}
