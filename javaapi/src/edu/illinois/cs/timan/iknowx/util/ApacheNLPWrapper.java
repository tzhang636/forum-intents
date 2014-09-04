package edu.illinois.cs.timan.iknowx.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.parser.ParserFactory;
import opennlp.tools.parser.ParserModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;

/**
 * Uses Apache NLP library
 * @author hcho33
 *
 */
public class ApacheNLPWrapper 
{
	private Tokenizer tokenizer;
	private POSTaggerME tagger;
	public ChunkerME chunker;
	private Parser parser;
	SentenceDetectorME sentenceDetector;

	
	private final String TOKENIZER_MODEL = "en-token.bin";
	private final String CHUNKER_MODEL = "en-chunker.bin";
	private final String POS_MODEL = "en-pos-maxent.bin";
	private final String PARSER_MODEL = "en-parser-chunking.bin";
	private final String SENT_DETECT_MODEL = "en-sent.bin";
	private final String modelDir = "global_libs/models/";

	public ApacheNLPWrapper()
	{
		loadSentDetectionAPI();
		loadTokenizerAPI();
		loadPOSAPI();
		loadChunkerAPI();
		//loadParserAPI(); // parser not supported for now
	}

	public static void main(String [] args)
	{
		ApacheNLPWrapper wrapper = new ApacheNLPWrapper();
		String [] arr = wrapper.tokenize("I didn't think to ask about the price of the regular effexor, without the xr.");
		for (String a : arr)
			System.out.println(a + " ");
	}
	private void loadSentDetectionAPI() 
	{
		InputStream modelIn = null;
		try {
			modelIn = new FileInputStream(modelDir + SENT_DETECT_MODEL);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			SentenceModel model = new SentenceModel(modelIn);
			sentenceDetector = new SentenceDetectorME(model);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				}
				catch (IOException e) {
				}
			}
		}

	}
	
	public String [] getSentences(String contentToSentencify)
	{
		contentToSentencify = contentToSentencify.replaceAll("\\.", "\\. ");
		return sentenceDetector.sentDetect(contentToSentencify);
	}

	public String [] tokenize(String stringToTokenize)
	{
		return tokenizer.tokenize(stringToTokenize);
	}

	public String [] tagPOS(String [] sentence)
	{
		return tagger.tag(sentence);
	}

	public String [] chunkSentence(String [] sentence, String [] pos)
	{
		return chunker.chunk(sentence, pos);
	}

	private void loadParserAPI()
	{
		InputStream modelIn = null;
		try {
			modelIn = new FileInputStream(modelDir + PARSER_MODEL);
			ParserModel model = new ParserModel(modelIn);
			parser = ParserFactory.create(model);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				}
				catch (IOException e) {
				}
			}
		}	
		Parse topParses[] = ParserTool.parseLine("The quick brown fox jumps over the lazy dog ."
				, parser, 1);
	}

	private void loadChunkerAPI() 
	{
		InputStream modelIn = null;
		ChunkerModel model = null;

		try {
			modelIn = new FileInputStream(modelDir + CHUNKER_MODEL);
			model = new ChunkerModel(modelIn);
		} catch (IOException e) {
			// Model loading failed, handle the error
			e.printStackTrace();
		} finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				} catch (IOException e) {
				}
			}
		}
		chunker = new ChunkerME(model);
	}

	private void loadPOSAPI() 
	{
		InputStream modelIn = null;

		try {
			modelIn = new FileInputStream(modelDir + POS_MODEL);
			POSModel model = new POSModel(modelIn);
			tagger = new POSTaggerME(model);
		}
		catch (IOException e) {
			// Model loading failed, handle the error
			e.printStackTrace();
		}
		finally {
			if (modelIn != null) {
				try {
					modelIn.close();
				}
				catch (IOException e) {
				}
			}
		}
	}

	private void loadTokenizerAPI() 
	{
		InputStream modelIn = null;
		try {
			modelIn = new FileInputStream(modelDir + TOKENIZER_MODEL);
			TokenizerModel model = new TokenizerModel(modelIn);
			tokenizer = new TokenizerME(model);	

		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally 
		{
			if (modelIn != null) {
				try {
					modelIn.close();
				}
				catch (IOException e) {
				}
			}
		}
	}
}
