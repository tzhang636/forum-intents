package test;

import java.util.List;

import gov.nih.nlm.nls.metamap.Ev;
import gov.nih.nlm.nls.metamap.Mapping;
import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.Negation;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Result;
import gov.nih.nlm.nls.metamap.Utterance;

public class Test {
	
	public static void main(String[] args) throws Exception {
		MetaMapApi api = new MetaMapApiImpl();
		List<Result> resultList = api.processCitationsFromString("medicine");
		//System.out.println(resultList.size());
		Result result = resultList.get(0);
		//System.out.println(result.getMachineOutput());
		List<Negation> negList = result.getNegations();
		//System.out.println(negList.size());
		//System.out.println(result.getUtteranceList().size());
		for (Utterance utterance : result.getUtteranceList()) {
			for (PCM pcm : utterance.getPCMList()) {
				//System.out.println(pcm.getPhrase().getPhraseText());
				for (Mapping map : pcm.getMappingList()) {
					for (Ev mapEv : map.getEvList()) {
						System.out.println(mapEv.getConceptName() + ": " + mapEv.getSemanticTypes());
					}
				}
				for (Ev ev : pcm.getCandidateList()) {
				}
			}
		}
	}
}