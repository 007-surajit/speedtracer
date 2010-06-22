/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.speedtracer.breaky.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.coreext.client.DataBag;
import com.google.gwt.coreext.client.JsIntegerMap;
import com.google.speedtracer.breaky.client.JsonSchema.JsonSchemaResults;

/**
 * Validates a Speed Tracer JSON dump file.
 */
public class DumpValidator {
  private JsIntegerMap<JsonSchema> idMap = JsIntegerMap.create();

  private final JavaScriptObject schemas = SpeedtracerSchemas.getSchemas();

  public DumpValidator() {
    fillIdMap();
    JsonSchemaValidator.hookResolver(createResolverHook());
  }

  /**
   * Determine the schema that corresponds to the integer type.
   * 
   * @param id the type
   * @return the corresponding {@link JsonSchema}
   */
  public final JsonSchema getSchema(int id) {
    // Custom Events are special cased here because unlike all other types, they
    // occupy a range of possible type IDs.
    if (id < -1) {
      return getSchema("CUSTOM_EVENT");
    }
    
    return idMap.get(id);
  }

  /**
   * Determine the schema of an object by looking at its "type" field.
   * 
   * @param obj the object with a "type" field
   * @return the corresponding {@link JsonSchema}
   */
  public final JsonSchema getSchema(JavaScriptObject obj) {
    if (DataBag.hasOwnProperty(obj, "type")) {
      return getSchema(DataBag.getIntProperty(obj, "type"));
    } else {
      return null;
    }
  }
  
  public final JsonSchema getSchema(String name) {
    return DataBag.getJSObjectProperty(schemas, name).cast();
  }

  /**
   * Native method for listing all available schemas (Helper method for building
   * the idMap).
   * 
   * @return JsArray<String> of schemas
   */
  public final native JsArrayString listSchemas() /*-{
    var schemas = this.@com.google.speedtracer.breaky.client.DumpValidator::schemas;
    ret = [];
    for(schema in schemas) {
      ret.push(schema);
    }
    return ret;
  }-*/;

  /**
   * Validate a Speedtracer dump object.
   * 
   * @param obj a speedtracer dump object to be validated
   * @return {@link JsonSchemaResults} object indicating that the entire object
   *         is valid or containing the error that caused it to be invalid.
   */
  public JsonSchemaResults validate(JavaScriptObject obj) {
    JsonSchema concreteSchema = getSchema(obj);
    if (concreteSchema == null) {
      return JsonSchemaResults.create("", "No schema found for "
          + obj.toString());
    }

    JsonSchemaResults results = JsonSchemaValidator.validate(obj, concreteSchema);
    if (!results.isValid()) {
      return results;
    }

    if (DataBag.hasOwnProperty(obj, "children")) {
      JsArray<JavaScriptObject> children = DataBag.getJSObjectProperty(obj,
          "children");
      for (int i = 0; i < children.length() && results.isValid(); i++) {
        // TODO(conroy): make child validation incremental?
        results = this.validate(children.get(i));
      }
    }
    return results;
  }

  /**
   * The speedtracer version of jsonschema uses a global function callback in
   * order to resolve references.
   * 
   * TODO(conroy): make the hook more flexible/non-global
   * 
   * @param the jsonschema-b4 JSONSchema object
   */
  private native JavaScriptObject createResolverHook() /*-{
    var me = this;
    return function(reference) {
      return me.@com.google.speedtracer.breaky.client.DumpValidator::schemas[reference];
    };
  }-*/;

  /**
   * In our schema set, if a schema has a fully constrained type property, then
   * it is a concrete rather than an abstract type. The ID Map let's us quickly
   * validate based on the concrete type as objects come in.
   */
  private void fillIdMap() {
    JsArrayString schemaNames = listSchemas();
    for (int i = 0; i < schemaNames.length(); i++) {
      JsonSchema schema = (JsonSchema) DataBag.getJSObjectProperty(schemas,
          schemaNames.get(i));
      JavaScriptObject properties = schema.getProperties();

      if (DataBag.hasOwnProperty(properties, "type")) {
        JsonSchema dumpType = DataBag.getJSObjectProperty(properties, "type");

        if ((DataBag.hasOwnProperty(dumpType, "minimum") && DataBag.hasOwnProperty(
            dumpType, "maximum"))
            && dumpType.getMinimum() == dumpType.getMaximum()) {
          idMap.put(dumpType.getMinimum(), schema);
        }
      }
    }
  }
}
