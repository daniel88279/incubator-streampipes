package org.streampipes.wrapper.flink.samples.timetofailure;

import org.streampipes.wrapper.flink.FlinkDeploymentConfig;
import org.streampipes.wrapper.flink.FlinkSepaRuntime;
import org.apache.flink.streaming.api.datastream.DataStream;

import java.util.Map;

/**
 * Created by riemer on 26.10.2016.
 */
public class TimeToFailureProgram extends FlinkSepaRuntime<TimeToFailureParameters> {

    public TimeToFailureProgram(TimeToFailureParameters params) {
        super(params);
    }

    public TimeToFailureProgram(TimeToFailureParameters params, FlinkDeploymentConfig config) {
        super(params, config);
    }


    @Override
    protected DataStream<Map<String, Object>> getApplicationLogic(DataStream<Map<String, Object>>... messageStream) {
        return messageStream[0].flatMap(new TimeToFailureCalculator(params.getHealthIndexMapping(), params.getMtbfValue()));
    }
}