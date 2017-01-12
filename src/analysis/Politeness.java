package analysis;


import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Dependency;
import edu.stanford.nlp.util.CoreMap;
import model.DocumentValues;
import reading.ReadingFile;
import tokenizer.TokenizeCorpus;
import edu.stanford.nlp.parser.nndep.DependencyParser;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class Politeness {



    private final Properties props = new Properties();
    private  StanfordCoreNLP pipeline = null;
    private final  DependencyParser dp = DependencyParser.loadFromModelFile(DependencyParser.DEFAULT_MODEL);

    public List<DocumentValues> createFormatForInput(String pathIn){
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        pipeline=new StanfordCoreNLP(props);

        List<DocumentValues> docsValues = new ArrayList<>();

        try {

            ReadingFile rd= new ReadingFile();
            List<String> docs = rd.read(pathIn);
            int i=1;
            for(String doc: docs){
                System.out.println("Analyzing doc num:"+ i);
                DocumentValues dcVal = new DocumentValues();
                List<String> sentencesDoc = new ArrayList<>();
                List<String> dependencyParse = new ArrayList<>();
                if(!doc.isEmpty()) {
                    // create an empty Annotation just with the given text
                    Annotation document = new Annotation(doc);
                    // run all Annotators on this text
                    pipeline.annotate(document);

                    List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

                    for (CoreMap sentence : sentences) {
                        sentencesDoc.add(sentence.toString());
                        String prediction = dp.predict(sentence).toString();
                        dependencyParse.add(elaborateString(prediction));
                    }
                }
                 dcVal.setSentences(sentencesDoc);
                 dcVal.setParse(dependencyParse);
                 dcVal.setText(doc);

                 docsValues.add(dcVal);

                 i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return docsValues;
    }

    private String elaborateString(String prediction){
        String[] ss= prediction.split("\n");
        String finale="";
        String st= ss[1];
        ss= st.split(",(?![^()]*+\\))");
        String quad= "[";
        String quadF="]";

        if(ss.length==1){
            ss[0]=ss[0].replace("[","");
            ss[0]=ss[0].replace("]","");
            ss[0]=quad+"\""+ss[0]+"\""+quadF;
        }
        else {
            ss[0]=ss[0].replace("[","");
            ss[0]=quad+"\""+ss[0]+"\"";
            ss[ss.length - 1] = ss[ss.length - 1].replace("]", "");
            ss[ss.length - 1] = ss[ss.length - 1].replace(" ", "");
            ss[ss.length - 1] = ",\"" + ss[ss.length - 1] + "\"" + quadF;
            for (int i = 1; i < ss.length - 1; i++) {
                ss[i] = ss[i].replace(" ", "");
                ss[i] = "," + "\"" + ss[i] + "\"";
            }
        }
        for (String s : ss) {
            finale += s;
        }
        return finale;
    }
}