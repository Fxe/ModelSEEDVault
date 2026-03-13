package org.modelseeed.rast;

//import java.util.regex.Pattern;
//import java.util.regex.Matcher;
import java.util.*;
import java.io.IOException;

public class RASTClient {

    private final RPCClient rpcClient;
    private final List<Map<String, Object>> stages;

    public RASTClient() {
        this.rpcClient = new RPCClient("https://tutorial.theseed.org/services/genome_annotation");

        this.stages = new ArrayList<>();

        Map<String, Object> stage1 = new HashMap<>();
        stage1.put("name", "annotate_proteins_kmer_v2");
        stage1.put("kmer_v2_parameters", new HashMap<String, Object>());
        this.stages.add(stage1);

        Map<String, Object> stage2 = new HashMap<>();
        stage2.put("name", "annotate_proteins_similarity");

        Map<String, Object> similarityParams = new HashMap<>();
        similarityParams.put("annotate_hypothetical_only", 1);

        stage2.put("similarity_parameters", similarityParams);
        this.stages.add(stage2);
    }
    
    public List<Map<String, Object>> getStages() {
      return this.stages;
    }

    public static List<String> splitAnnotation(String annotation) {
        if (annotation != null && !annotation.isEmpty()) {
            return Arrays.asList(annotation.split("; | / | @"));
        }
        return Collections.emptyList();
    }

     /**
    @SuppressWarnings("unchecked")
    public Object annotateGenome(Map<String, Object> featureMap, boolean splitTerms) throws IOException {
        List<Map<String, Object>> pFeatures = new ArrayList<>();

        List<Map<String, Object>> res = f(pFeatures);

        if (res != null && !res.isEmpty()) {
            Map<String, Object> firstResult = res.get(0);
            Object featuresObj = firstResult.get("features");

            if (featuresObj instanceof List<?>) {
                List<?> featureResults = (List<?>) featuresObj;

                for (Object obj : featureResults) {
                    if (!(obj instanceof Map<?, ?>)) {
                        continue;
                    }

                    Map<String, Object> annotatedFeature = (Map<String, Object>) obj;
                    String id = (String) annotatedFeature.get("id");
                    Feature feature = genome.getFeatureById(id);

                    if (feature != null && annotatedFeature.containsKey("function")) {
                        String rastFunction = (String) annotatedFeature.get("function");

                        if (splitTerms) {
                            for (String function : splitAnnotation(rastFunction)) {
                                feature.addOntologyTerm("RAST", function);
                            }
                        } else {
                            feature.addOntologyTerm("RAST", rastFunction);
                        }
                    }
                }
            }

            return firstResult.get("analysis_events");
        }

        return null;
    }
    **/

      /**
    public AnnotateGenomeResult annotateGenomeFromFasta(String filepath) throws IOException {
        return annotateGenomeFromFasta(filepath, "\\|");
    }**/

    public List<Map<String, Object>> annotateProteinSequence(String proteinId, String proteinSeq) throws IOException, InterruptedException {
        List<Map<String, Object>> pFeatures = new ArrayList<>();

        Map<String, Object> feature = new HashMap<>();
        feature.put("id", proteinId);
        feature.put("protein_translation", proteinSeq);
        pFeatures.add(feature);

        return this.f(pFeatures);
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> annotateProteinBatch(Map<String, String> proteinBatch) throws IOException, InterruptedException {
        Map<String, String> proteinAnnotations = new HashMap<>();
        List<Map<String, Object>> features = new ArrayList<>();

        for (Map.Entry<String, String> entry : proteinBatch.entrySet()) {
            Map<String, Object> feature = new HashMap<>();
            feature.put("id", entry.getKey());
            feature.put("protein_translation", entry.getValue());
            features.add(feature);
        }

        List<Object> params = new ArrayList<>();

        Map<String, Object> featuresParam = new HashMap<>();
        featuresParam.put("features", features);

        Map<String, Object> stagesParam = new HashMap<>();
        stagesParam.put("stages", this.stages);

        params.add(featuresParam);
        params.add(stagesParam);

        List<Map<String, Object>> result =
            (List<Map<String, Object>>) rpcClient.call("GenomeAnnotation.run_pipeline", params);

        if (result != null && !result.isEmpty()) {
            Map<String, Object> firstResult = result.get(0);
            Object featuresObj = firstResult.get("features");

            if (featuresObj instanceof List<?>) {
                List<?> annotatedFeatures = (List<?>) featuresObj;

                for (Object obj : annotatedFeatures) {
                    if (!(obj instanceof Map<?, ?>)) {
                        continue;
                    }

                    Map<String, Object> feature = (Map<String, Object>) obj;
                    if (feature.containsKey("function")) {
                        proteinAnnotations.put(
                            (String) feature.get("id"),
                            (String) feature.get("function")
                        );
                    }
                }
            }
        }

        return proteinAnnotations;
    }

    public Map<String, String> annotateProteinSequences(Map<String, String> proteinSeqs, int chunkSize)
        throws IOException, InterruptedException {

        Map<String, String> proteinAnnotations = new HashMap<>();
        Map<String, String> chunk = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : proteinSeqs.entrySet()) {
            chunk.put(entry.getKey(), entry.getValue());

            if (chunk.size() >= chunkSize) {
                proteinAnnotations.putAll(annotateProteinBatch(chunk));
                chunk.clear();
            }
        }

        if (!chunk.isEmpty()) {
            proteinAnnotations.putAll(annotateProteinBatch(chunk));
        }

        return proteinAnnotations;
    }

    public Map<String, String> annotateProteinSequences(Map<String, String> proteinSeqs) throws IOException, InterruptedException {
        return annotateProteinSequences(proteinSeqs, 5000);
    }

    public List<Map<String, Object>> f1(String proteinId, String proteinSeq) throws IOException, InterruptedException {
        List<Map<String, Object>> pFeatures = new ArrayList<>();

        Map<String, Object> feature = new HashMap<>();
        feature.put("id", proteinId);
        feature.put("protein_translation", proteinSeq);
        pFeatures.add(feature);

        return this.f(pFeatures);
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> f(List<Map<String, Object>> pFeatures) throws IOException, InterruptedException {
        List<Object> params = new ArrayList<>();

        Map<String, Object> featuresParam = new HashMap<>();
        featuresParam.put("features", pFeatures);

        Map<String, Object> stagesParam = new HashMap<>();
        stagesParam.put("stages", stages);

        params.add(featuresParam);
        params.add(stagesParam);

        return (List<Map<String, Object>>) rpcClient.call("GenomeAnnotation.run_pipeline", params);
    }

    /**
    public static class AnnotateGenomeResult {
        private final MSGenome genome;
        private final Object analysisEvents;

        public AnnotateGenomeResult(MSGenome genome, Object analysisEvents) {
            this.genome = genome;
            this.analysisEvents = analysisEvents;
        }

        public MSGenome getGenome() {
            return genome;
        }

        public Object getAnalysisEvents() {
            return analysisEvents;
        }
    }
    **/
}