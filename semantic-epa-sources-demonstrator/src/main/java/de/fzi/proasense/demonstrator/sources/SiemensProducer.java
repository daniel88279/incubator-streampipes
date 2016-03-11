package de.fzi.proasense.demonstrator.sources;

import java.util.Arrays;
import java.util.List;

import de.fzi.cep.sepa.desc.declarer.EventStreamDeclarer;
import de.fzi.cep.sepa.desc.declarer.SemanticEventProducerDeclarer;
import de.fzi.cep.sepa.model.impl.graph.SepDescription;
import de.fzi.proasense.demonstrator.config.DemonstratorVariables;
import de.fzi.proasense.demonstrator.siemens.streams.SiemensFlowRateSensorStream;

public class SiemensProducer implements SemanticEventProducerDeclarer {

	@Override
	public SepDescription declareModel() {
		return new SepDescription("siemens", "Siemens",
				"Provides streams generated by the siemens sensors of the demonstrator");
	}

	@Override
	public List<EventStreamDeclarer> getEventStreams() {
		return Arrays.asList(new SiemensFlowRateSensorStream(DemonstratorVariables.SIENENS_FLOWRATESENSOR1),
				new SiemensFlowRateSensorStream(DemonstratorVariables.SIENENS_FLOWRATESENSOR2));
//				new SiemensFlowRateSensorStream(DemonstratorVariables.SIEMENS_LEVELSENSOR));
	}

}