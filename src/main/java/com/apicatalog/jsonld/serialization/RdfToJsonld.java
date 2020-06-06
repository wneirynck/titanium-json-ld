package com.apicatalog.jsonld.serialization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonValue;

import com.apicatalog.iri.IRI;
import com.apicatalog.jsonld.api.JsonLdError;
import com.apicatalog.jsonld.api.JsonLdOptions.RdfDirection;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.rdf.RdfDataset;
import com.apicatalog.rdf.RdfGraph;
import com.apicatalog.rdf.RdfGraphName;
import com.apicatalog.rdf.RdfTriple;
import com.apicatalog.rdf.lang.RdfVocabulary;

public final class RdfToJsonld {

    // required
    private RdfDataset dataset;
    
    // optional
    private boolean ordered;
    private RdfDirection rdfDirection;
    private boolean useNativeTypes;
    private boolean useRdfType;
    
    // runtime
    private Map<String, Map<String, JsonValue>> defaultGraph;
    private Map<String, Map<String, Map<String, JsonValue>>> graphMap;
    private Map<String, Map<String, JsonValue>> compoundLiteralSubjects;
    private Map<IRI, Boolean> referenceOnce;
    
    private RdfToJsonld(final RdfDataset dataset) {
        this.dataset = dataset;
        
        // default values
        this.ordered = false;
        this.rdfDirection = null;
        this.useNativeTypes = false;
        this.useRdfType = false;
    }
    
    public static final RdfToJsonld with(final RdfDataset dataset) {
        return new RdfToJsonld(dataset);
    }
    
    public RdfToJsonld ordered() {
        return ordered(true);
    }
    
    public RdfToJsonld ordered(boolean ordered) {
        this.ordered = ordered;
        return this;
    }
    
    public RdfToJsonld rdfDirection(RdfDirection rdfDirection) {
        this.rdfDirection = rdfDirection;
        return this;
    }
    
    public RdfToJsonld useNativeTypes() {
        return useNativeTypes(false);
    }
    
    public RdfToJsonld useNativeTypes(boolean useNativeTypes) {
        this.useNativeTypes = useNativeTypes;
        return this;
    }
    
    public RdfToJsonld useRdfType() {
        return useRdfType(true);
    }
    
    public RdfToJsonld useRdfType(boolean useRdfType) {
        this.useRdfType = useRdfType;
        return this;
    }
    
    public JsonArray build() throws JsonLdError {
        
        // 1.
        defaultGraph = new LinkedHashMap<>();
        
        // 2.
        graphMap = new LinkedHashMap<>();
        graphMap.put(Keywords.DEFAULT, defaultGraph);
        
        // 3.
        referenceOnce = new LinkedHashMap<>();
        
        // 4.
        compoundLiteralSubjects = new LinkedHashMap<>();
        
        // 5.
        step5(Keywords.DEFAULT, dataset.getDefaultGraph());
        
        for (RdfGraphName graphName : dataset.getGraphNames()) {
            step5(graphName.toString(), dataset.getGraph(graphName));            
        }
        
        // 6.
        for (Entry<String, Map<String, Map<String, JsonValue>>> entry : graphMap.entrySet()) {
            
            final String name = entry.getKey();
            final Map<String, Map<String, JsonValue>> graphObject = entry.getValue();
            
            // 6.1.
            //TODO
            
            // 6.2.
            if (!graphObject.containsKey("rdf:nil")) {
                continue;
            }
            
            // 6.3.
            
            
            //TODO            
        }
        //TODO
        
        // 7.
        final JsonArrayBuilder result = Json.createArrayBuilder();
        
        // 8.
        final List<String> subjects = new ArrayList<>(defaultGraph.keySet());
        
        if (ordered) {
            Collections.sort(subjects);
        }
        
        for (String subject : subjects) {
            
            final Map<String, JsonValue> node = defaultGraph.get(subject);
        
            // 8.1.
            //TODO
            
            // 8.2.
            //TODO usages?! 
            result.add(JsonUtils.toJsonObject(node));
            
        }
        //TODO
        
        // 9.
        return result.build();
    }
    
    private void step5(String name, RdfGraph graph) throws JsonLdError {
        
        // 5.2.
        if (!graphMap.containsKey(name)) {
            graphMap.put(name, new LinkedHashMap<>());
        }
        
        // 5.3.
        if (!compoundLiteralSubjects.containsKey(name)) {
            compoundLiteralSubjects.put(name, new LinkedHashMap<>());
        }

        // 5.4.
        if (!Keywords.DEFAULT.equals(name) && !defaultGraph.containsKey(name)) {
            Map<String, JsonValue> map = new LinkedHashMap<>();
            map.put(Keywords.ID, Json.createValue(name));
            defaultGraph.put(name, map);
        }
        
        // 5.5.
        Map<String, Map<String, JsonValue>> nodeMap = graphMap.get(name);
        
        // 5.6.
        Map<String, JsonValue> compoundMap = compoundLiteralSubjects.get(name);
        
        // 5.7.
        for (RdfTriple triple : graph.toList()) {
                     
            String subject = triple.getSubject().toString();
            String predicate = triple.getPredicate().toString();
            
            // 5.7.1.
            if (!nodeMap.containsKey(subject)) {
                Map<String, JsonValue> map = new LinkedHashMap<>();
                map.put(Keywords.ID, Json.createValue(subject));
                
                nodeMap.put(triple.getSubject().toString(), map);
            }
            
            // 5.7.2.
            Map<String, JsonValue> node = nodeMap.get(subject);
            
            // 5.7.3.
            if (RdfDirection.COMPOUND_LITERAL == rdfDirection 
                    && RdfVocabulary.DIRECTION.equals(predicate)) {
                
                compoundMap.put(subject, JsonValue.TRUE);
            }
            
            // 5.7.4.
            if ((triple.getObject().isBlankNode() || triple.getObject().isIRI())
                    && !nodeMap.containsKey(triple.getObject().toString())) {
                
                Map<String, JsonValue> map = new LinkedHashMap<>();
                map.put(Keywords.ID, Json.createValue(triple.getObject().toString()));
                nodeMap.put(triple.getObject().toString(), map);
                
            }
            
            // 5.7.5.
            if (!useRdfType && RdfVocabulary.TYPE.equals(predicate) && !triple.getObject().isBlankNode()) {
                //TODO
                continue;
            }
            
            // 5.7.6.
            final Map<String, JsonValue> value = RdfToObject.with(triple.getObject(), rdfDirection, useNativeTypes).build();
            
            // 5.7.7.
            if (!node.containsKey(predicate)) {
                node.put(predicate, JsonValue.EMPTY_JSON_ARRAY);
            }
            
            // 5.7.8.
            //TODO
            
            // 5.7.9.
            if (triple.getObject().isIRI() && RdfVocabulary.NIL.equals(triple.getObject().asIRI().toString())) {
                //TODO
                
            // 5.7.10.
            } else if (triple.getObject().isIRI() && referenceOnce.containsKey(triple.getObject().asIRI())) {
                referenceOnce.put(triple.getObject().asIRI(), Boolean.FALSE);

            // 5.7.11.
            } else if (triple.getObject().isBlankNode()) {
                
                // 5.7.11.1.
                
                
            }
            
            //TODO
            
        }        
    }
}
