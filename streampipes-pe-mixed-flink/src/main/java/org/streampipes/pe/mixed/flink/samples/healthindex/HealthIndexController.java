package org.streampipes.pe.mixed.flink.samples.healthindex;

import org.streampipes.container.util.StandardTransportFormat;
import org.streampipes.sdk.helpers.Labels;
import org.streampipes.wrapper.flink.FlinkDataProcessorDeclarer;
import org.streampipes.wrapper.flink.FlinkDeploymentConfig;
import org.streampipes.wrapper.flink.FlinkDataProcessorRuntime;
import org.streampipes.pe.mixed.flink.samples.FlinkConfig;
import org.streampipes.model.schema.EventSchema;
import org.streampipes.model.SpDataStream;
import org.streampipes.model.schema.EventProperty;
import org.streampipes.model.graph.DataProcessorDescription;
import org.streampipes.model.graph.DataProcessorInvocation;
import org.streampipes.model.output.FixedOutputStrategy;
import org.streampipes.model.output.OutputStrategy;
import org.streampipes.model.staticproperty.FreeTextStaticProperty;
import org.streampipes.model.staticproperty.MappingProperty;
import org.streampipes.model.staticproperty.MappingPropertyUnary;
import org.streampipes.model.staticproperty.PropertyValueSpecification;
import org.streampipes.model.staticproperty.StaticProperty;
import org.streampipes.model.util.SepaUtils;
import org.streampipes.vocabulary.MhWirth;
import org.streampipes.vocabulary.SO;
import org.streampipes.sdk.StaticProperties;
import org.streampipes.sdk.helpers.EpProperties;
import org.streampipes.sdk.helpers.EpRequirements;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class HealthIndexController extends FlinkDataProcessorDeclarer<HealthIndexParameters> {

    private final String frictionCoefficientNominal = "frictionCoefficientNominal";
    private final String frictionCoefficientStdDev = "frictionCoefficientStdDev";
    private final String frictionCoefficientStdDevMultiplier = "frictionCoefficientStdDevMultiplier";

    private final String degradationRateBase = "degradationRateBase";
    private final String degradationRateDivider = "degradationRateDivider";
    private final String degradationValueMultiplier = "degradationvalueMultiplier";
    private final String degradationValueOffset = "degradationValueOffset";

    private final String frictionMappingName = "frictionMapping";
    private final String timestampMappingName = "timestampMapping";
    private final String machineTypeMappingName = "machineTypeMapping";
    private final String frictionCoefficientDegradationRate = "frictioNCoefficientDegradationRate";


    @Override
    public DataProcessorDescription declareModel() {
        List<EventProperty> eventProperties = new ArrayList<EventProperty>();

        EventProperty frictionPropertyRequirement = EpRequirements.domainPropertyReq(MhWirth.FrictionValue);
        EventProperty timestampRequirement = EpRequirements.domainPropertyReq("http://schema.org/DateTime");
        EventProperty machineTypeRequirement = EpRequirements.domainPropertyReq(SO.Text);

        eventProperties.add(EpRequirements.domainPropertyReq(MhWirth.Stddev));
        eventProperties.add(frictionPropertyRequirement);
        eventProperties.add(timestampRequirement);
        eventProperties.add(machineTypeRequirement);

        EventSchema schema1 = new EventSchema();
        schema1.setEventProperties(eventProperties);

        SpDataStream stream1 = new SpDataStream();
        stream1.setEventSchema(schema1);

        DataProcessorDescription desc = new DataProcessorDescription("health_index", "Health Index", "Calculates the health index based on swivel or gearbox friction coefficients.");

        desc.addEventStream(stream1);

        List<StaticProperty> staticProperties = new ArrayList<>();

        MappingProperty frictionValueMapping = new MappingPropertyUnary(URI.create(frictionPropertyRequirement.getElementId()), frictionMappingName, "Friction Coefficient Mapping", "The field containing friction coefficient values.");
        MappingProperty timestampMapping = new MappingPropertyUnary(URI.create(timestampRequirement.getElementId()), timestampMappingName, "Timestamp Mapping", "The field containing the current timestamp.");
        MappingProperty machineTypeMapping = new MappingPropertyUnary(URI.create(machineTypeRequirement.getElementId()), machineTypeMappingName, "Machine Type Mapping", "The field containing an identifier of a machine or a part(e.g., swivel");

        staticProperties.add(frictionValueMapping);
        staticProperties.add(timestampMapping);
        staticProperties.add(machineTypeMapping);

        //TODO remove TODOs for
        FreeTextStaticProperty nominal = StaticProperties.doubleFreeTextProperty(frictionCoefficientNominal, "Nominal Friction Coefficient (sigma_f)", "");
        nominal.setValue("0.018200901668492");
        staticProperties.add(nominal);

        FreeTextStaticProperty coefficientStdDev = StaticProperties.doubleFreeTextProperty(frictionCoefficientStdDev, "Friction Coefficient standard deviation", "");
        coefficientStdDev.setValue("0.006994978");
        staticProperties.add(coefficientStdDev);

        FreeTextStaticProperty coefficientStdDevMultiplier = StaticProperties.integerFreeTextProperty(frictionCoefficientStdDevMultiplier, "Multiplier delta_cx: gamma = delta_cx * sigma_f", "");
        coefficientStdDevMultiplier.setValue("10");
        coefficientStdDevMultiplier.setValueSpecification(new PropertyValueSpecification(1, 100, 1));
        staticProperties.add(coefficientStdDevMultiplier);

        FreeTextStaticProperty rateBase = StaticProperties.integerFreeTextProperty(degradationRateBase, "Degradation Rate Base (Power)", "");
        rateBase.setValue("10");
        rateBase.setValueSpecification(new PropertyValueSpecification(1, 100, 1));
        staticProperties.add(rateBase);

        FreeTextStaticProperty rateDivider = StaticProperties.integerFreeTextProperty(degradationRateDivider, "Degradation Rate Divider", "");
        rateDivider.setValue("100");
        rateDivider.setValueSpecification(new PropertyValueSpecification(5, 100, 5));
        staticProperties.add(rateDivider);


        desc.setStaticProperties(staticProperties);

        List<OutputStrategy> strategies = new ArrayList<OutputStrategy>();
        FixedOutputStrategy outputStrategy = new FixedOutputStrategy();

        List<EventProperty> outputProperties = new ArrayList<EventProperty>();
        outputProperties.add(EpProperties.doubleEp(Labels.empty(), "healthIndex", MhWirth.HealthIndex));
        outputProperties.add(EpProperties.longEp(Labels.empty(), "timestamp", "http://schema.org/DateTime"));
        outputProperties.add(EpProperties.stringEp(Labels.empty(), "machineId", MhWirth.MachineId));

        outputStrategy.setEventProperties(outputProperties);
        strategies.add(outputStrategy);
        desc.setOutputStrategies(strategies);
        desc.setSupportedGrounding(StandardTransportFormat.getSupportedGrounding());

        return desc;
    }

    @Override
    public FlinkDataProcessorRuntime<HealthIndexParameters> getRuntime(DataProcessorInvocation graph) {

        String frictionMapping = SepaUtils.getMappingPropertyName(graph, frictionMappingName);
        String timestampMapping = SepaUtils.getMappingPropertyName(graph, timestampMappingName);
        String machineTypeMapping = SepaUtils.getMappingPropertyName(graph, machineTypeMappingName);

        HealthIndexVariables2 variables = new HealthIndexVariables2();

        variables.setAverage(Double.parseDouble(SepaUtils.getFreeTextStaticPropertyValue(graph, frictionCoefficientNominal)));
        variables.setStddev(Double.parseDouble(SepaUtils.getFreeTextStaticPropertyValue(graph, frictionCoefficientStdDev)));

        variables.setDeltacx(Integer.parseInt(SepaUtils.getFreeTextStaticPropertyValue(graph, frictionCoefficientStdDevMultiplier)));

        variables.setPower(Integer.parseInt(SepaUtils.getFreeTextStaticPropertyValue(graph, degradationRateBase)));
        variables.setDivider(Integer.parseInt(SepaUtils.getFreeTextStaticPropertyValue(graph, degradationRateDivider)));

        HealthIndexParameters staticParam = new HealthIndexParameters (
                graph,
                frictionMapping,
                timestampMapping,
                machineTypeMapping,
                variables);

        return new HealthIndexProgram(staticParam, new FlinkDeploymentConfig(FlinkConfig.JAR_FILE,
                FlinkConfig.INSTANCE.getFlinkHost(), FlinkConfig.INSTANCE.getFlinkPort()));

        //return new HealthIndexProgram(staticParam);
    }
}
