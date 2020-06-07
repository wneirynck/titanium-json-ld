package com.apicatalog.jsonld.serialization;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;

import com.apicatalog.jsonld.api.JsonLdError;
import com.apicatalog.jsonld.api.JsonLdOptions.RdfDirection;
import com.apicatalog.jsonld.json.JsonUtils;
import com.apicatalog.jsonld.lang.Keywords;
import com.apicatalog.jsonld.lang.Version;
import com.apicatalog.rdf.RdfLiteral;
import com.apicatalog.rdf.RdfObject;
import com.apicatalog.xml.XsdVocabulary;

public final class RdfToObject {

    // required
    private RdfObject value;
    private RdfDirection rdfDirection;
    private boolean useNativeTypes;
    
    // optional
    private Version processingMode;
    
    private RdfToObject(final RdfObject object, final RdfDirection rdfDirection, final boolean useNativeTypes) {
        this.value = object;
        this.rdfDirection = rdfDirection;
        this.useNativeTypes = useNativeTypes;
        
        this.processingMode = null;
    }
    
    public static final RdfToObject with(final RdfObject object, final RdfDirection rdfDirection, final boolean useNativeTypes) {
        return new RdfToObject(object, rdfDirection, useNativeTypes);
    }
    
    public RdfToObject processingMode(Version processingMode) {
        this.processingMode = processingMode;
        return this;
    }
    
    public JsonObject build() throws JsonLdError {
        
        // 1.
        if (value.isIRI() || value.isBlankNode()) {
            return Json.createObjectBuilder().add(Keywords.ID, value.toString()).build();
        }

        final Map<String, JsonValue> result = new LinkedHashMap<>();
        
        // 2.
        final RdfLiteral literal = value.asLiteral();
        
        // 2.2.
        JsonValue convertedValue = Json.createValue(literal.getValue());
        
        // 2.3.
        String type = null;
        
        // 2.4.
        if (useNativeTypes) {
            
            // 2.4.1.
            if (XsdVocabulary.STRING.equals(literal.getDatatype().toString())) {
                convertedValue = Json.createValue(literal.toString());  //TODO ?!

            // 2.4.2.
            } else if (XsdVocabulary.BOOLEAN.equals(literal.getDatatype().toString())) {
                
                convertedValue = "true".equalsIgnoreCase(literal.getValue()) ? JsonValue.TRUE : JsonValue.FALSE;
                type = XsdVocabulary.BOOLEAN;
            }
            

            
            
            //TODO

        // 2.5.
        } else if (/*TODO processing mode*/false) {
            
            //TODO

        // 2.6.
        } else if (RdfDirection.I18N_DATATYPE == rdfDirection
                    && literal.getDatatype() != null && literal.getDatatype().toString().startsWith("https://www.w3.org/ns/i18n#")
                ) {

            //TODO

        // 2.7. 
        } else if (literal.getLanguage() != null) {
            result.put(Keywords.LANGUAGE, Json.createValue(literal.getLanguage()));
                     
        // 2.8.   
        } else if (!XsdVocabulary.STRING.equals(literal.getDatatype().toString())) {
            type = literal.getDatatype().toString();
        }        
        
        // 2.9.
        result.put(Keywords.VALUE, convertedValue);
        
        // 2.10.
        if (type != null) {
            result.put(Keywords.TYPE, Json.createValue(type));
        }
        
        // 2.11.
        return JsonUtils.toJsonObject(result);
    }
    
}
