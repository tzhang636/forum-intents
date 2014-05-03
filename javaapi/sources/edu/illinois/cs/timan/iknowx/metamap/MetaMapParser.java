package edu.illinois.cs.timan.iknowx.metamap;

import edu.illinois.cs.timan.iknowx.datastructure.AbstractPostObject;
import edu.illinois.cs.timan.iknowx.io.SaveLoad;
import edu.illinois.cs.timan.iknowx.metamapobj.MetaMapEV;
import edu.illinois.cs.timan.iknowx.metamapobj.MetaMapObject;
import edu.illinois.cs.timan.iknowx.metamapobj.MetaMapPCM;
import edu.illinois.cs.timan.iknowx.metamapobj.MetaMapPost;
import edu.illinois.cs.timan.iknowx.metamapobj.MetaMapResult;
import edu.illinois.cs.timan.iknowx.metamapobj.MetaMapUtterance;
import edu.illinois.cs.timan.iknowx.util.ApacheNLPWrapper;
import gov.nih.nlm.nls.metamap.Ev;
import gov.nih.nlm.nls.metamap.MetaMapApi;
import gov.nih.nlm.nls.metamap.MetaMapApiImpl;
import gov.nih.nlm.nls.metamap.PCM;
import gov.nih.nlm.nls.metamap.Result;
import gov.nih.nlm.nls.metamap.Utterance;

import java.util.*;

import com.google.gson.Gson;

public class MetaMapParser {
	static MetaMapApi api = new MetaMapApiImpl();
	static ApacheNLPWrapper wrapper = new ApacheNLPWrapper();
	static Gson gson = new Gson();
	
	public static MetaMapPost loadTitle(String metamapJsonFile) {
		String json = SaveLoad.loadFile(metamapJsonFile);
		return gson.fromJson(json, MetaMapPost.class);
	}
	
	public static MetaMapPost loadPost(String metamapJsonFile) {
		String json = SaveLoad.loadFile(metamapJsonFile);
		return gson.fromJson(json, MetaMapPost.class);
	}
	
	public static MetaMapPost parseTitle(AbstractPostObject post) throws Exception {
		MetaMapPost title = new MetaMapPost();
		String[] sents = wrapper.getSentences(post.getTitle());
		title.metamapObjects = new MetaMapObject[sents.length];
		int sentIdx = 0;
		
		for (String sent : sents) {
			List<Result> resultList = new ArrayList<Result>();
			MetaMapObject mo = new MetaMapObject();
			title.metamapObjects[sentIdx] = mo;
			mo.sentenceId = sentIdx;
			mo.id = post.getId();

			resultList.addAll( api.processCitationsFromString(sent) );
			mo.resultList = new MetaMapResult[resultList.size()];
			
			int ridx = 0;
			for (Result result : resultList) {
				MetaMapResult mResult = new MetaMapResult();
				mo.resultList[ridx] = mResult;
				mResult.utterances = new MetaMapUtterance[result.getUtteranceList().size()];
				
				int uidx = 0;
				for (Utterance utterance: result.getUtteranceList()) {
					MetaMapUtterance mUtterance = new MetaMapUtterance();
					mResult.utterances[uidx] = mUtterance;
					mUtterance.posX = utterance.getPosition().getX();
					mUtterance.posY = utterance.getPosition().getY();
					mUtterance.utteranceString = utterance.getString();
					mUtterance.pcms = new MetaMapPCM[utterance.getPCMList().size()];
					
					int pcmIdx = 0;
					for (PCM pcm: utterance.getPCMList()) {
						MetaMapPCM mPCM = new MetaMapPCM();
						mUtterance.pcms[pcmIdx] = mPCM;
						mPCM.phrase = pcm.getPhrase().getPhraseText();
						mPCM.evs = new MetaMapEV[pcm.getCandidateList().size()];

						int eidx = 0;
						for (Ev ev: pcm.getCandidateList()) {
							MetaMapEV mEv = new MetaMapEV();
							mPCM.evs[eidx] = mEv;
							mEv.conceptId = ev.getConceptId();
							mEv.isHead = ev.isHead();
							mEv.conceptName = ev.getConceptName();
							
							mEv.matchedWords = new String[ev.getMatchedWords().size()];
							for (int wmidx = 0; wmidx < ev.getMatchedWords().size(); wmidx++) {
								mEv.matchedWords[wmidx] = ev.getMatchedWords().get(wmidx);
							}
							
							mEv.posXs = new int[ev.getPositionalInfo().size()];
							mEv.posYs = new int[ev.getPositionalInfo().size()];
							for (int pidx = 0; pidx < ev.getPositionalInfo().size(); pidx++) {
								mEv.posXs[pidx] = ev.getPositionalInfo().get(pidx).getX();
								mEv.posYs[pidx] = ev.getPositionalInfo().get(pidx).getY();
							}
							mEv.score = ev.getScore();
							mEv.semTypes = new String[ev.getSemanticTypes().size()];
							for (int semIdx = 0; semIdx < ev.getSemanticTypes().size(); semIdx++) {
								mEv.semTypes[semIdx] = ev.getSemanticTypes().get(semIdx);
							}
							eidx++;
						}
						pcmIdx++;
					}
					uidx++;
				}
				ridx++;
			}
			sentIdx++;
		}
		return title;
	}
	
	public static MetaMapPost parsePost(AbstractPostObject post) throws Exception {
		List<String> theOptions = new ArrayList<String>();
		if (theOptions.size() > 0) {
			api.setOptions(theOptions);
		}
		
		MetaMapPost metamapPost = new MetaMapPost();
		//for (int i = 0; i < post.getSize(); i++) {
			int sentIdx = 0;
			String text = post.getText(0);
			String [] sents = wrapper.getSentences(text);
			System.out.println("Text : " + text);
			metamapPost = new MetaMapPost();
			metamapPost.metamapObjects = new MetaMapObject[sents.length];

			for (String sent : sents) {
				List<Result> resultList = new ArrayList<Result>();
				MetaMapObject mo = new MetaMapObject();
				metamapPost.metamapObjects[sentIdx] = mo;
				mo.sentenceId = sentIdx;
				mo.id = post.getId();
	
				resultList.addAll( api.processCitationsFromString(sent) );
				mo.resultList = new MetaMapResult[resultList.size()];
				
				int ridx = 0;
				for (Result result : resultList) {
					MetaMapResult mResult = new MetaMapResult();
					mo.resultList[ridx] = mResult;
					mResult.utterances = new MetaMapUtterance[result.getUtteranceList().size()];
					
					int uidx = 0;
					for (Utterance utterance: result.getUtteranceList()) {
						MetaMapUtterance mUtterance = new MetaMapUtterance();
						mResult.utterances[uidx] = mUtterance;
						mUtterance.posX = utterance.getPosition().getX();
						mUtterance.posY = utterance.getPosition().getY();
						mUtterance.utteranceString = utterance.getString();
						mUtterance.pcms = new MetaMapPCM[utterance.getPCMList().size()];
						
						int pcmIdx = 0;
						for (PCM pcm: utterance.getPCMList()) {
							MetaMapPCM mPCM = new MetaMapPCM();
							mUtterance.pcms[pcmIdx] = mPCM;
							mPCM.phrase = pcm.getPhrase().getPhraseText();
							mPCM.evs = new MetaMapEV[pcm.getCandidateList().size()];
	
							int eidx = 0;
							for (Ev ev: pcm.getCandidateList()) {
								MetaMapEV mEv = new MetaMapEV();
								mPCM.evs[eidx] = mEv;
								mEv.conceptId = ev.getConceptId();
								mEv.isHead = ev.isHead();
								mEv.conceptName = ev.getConceptName();
								
								mEv.matchedWords = new String[ev.getMatchedWords().size()];
								for (int wmidx = 0; wmidx < ev.getMatchedWords().size(); wmidx++) {
									mEv.matchedWords[wmidx] = ev.getMatchedWords().get(wmidx);
								}
								
								mEv.posXs = new int[ev.getPositionalInfo().size()];
								mEv.posYs = new int[ev.getPositionalInfo().size()];
								for (int pidx = 0; pidx < ev.getPositionalInfo().size(); pidx++) {
									mEv.posXs[pidx] = ev.getPositionalInfo().get(pidx).getX();
									mEv.posYs[pidx] = ev.getPositionalInfo().get(pidx).getY();
								}
								mEv.score = ev.getScore();
								mEv.semTypes = new String[ev.getSemanticTypes().size()];
								for (int semIdx = 0; semIdx < ev.getSemanticTypes().size(); semIdx++) {
									mEv.semTypes[semIdx] = ev.getSemanticTypes().get(semIdx);
								}
								eidx++;
							}
							pcmIdx++;
						}
						uidx++;
					}
					ridx++;
				}
				sentIdx++;
			}
		//}
		return metamapPost;
	}
}