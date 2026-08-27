package com.comparator.model.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@JsonDeserialize(using = ComparisonExecuteRequest.Deserializer.class)
public record ComparisonExecuteRequest(
        @NotEmpty(message = "At least one key column must be specified")
        List<String> keyColumns,

        @Valid
        List<ToleranceConfig> tolerances,

        Boolean caseSensitive
) {
    public ComparisonExecuteRequest(List<String> keyColumns) {
        this(keyColumns, List.of(), true);
    }

    public ComparisonExecuteRequest(List<String> keyColumns, List<ToleranceConfig> tolerances) {
        this(keyColumns, tolerances != null ? tolerances : List.of(), true);
    }

    public static class Deserializer extends JsonDeserializer<ComparisonExecuteRequest> {
        @Override
        public ComparisonExecuteRequest deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode root = p.getCodec().readTree(p);
            if (root == null || root.isNull()) {
                return new ComparisonExecuteRequest(List.of(), List.of(), true);
            }

            if (root.isArray()) {
                List<String> keys = new ArrayList<>();
                for (JsonNode item : root) {
                    if (item.isTextual()) {
                        keys.add(item.asText());
                    }
                }
                return new ComparisonExecuteRequest(keys, List.of(), true);
            }

            if (root.isObject()) {
                List<String> keyColumns = new ArrayList<>();
                if (root.has("keyColumns") && root.get("keyColumns").isArray()) {
                    for (JsonNode item : root.get("keyColumns")) {
                        if (item.isTextual()) {
                            keyColumns.add(item.asText());
                        }
                    }
                }

                List<ToleranceConfig> tolerances = new ArrayList<>();
                if (root.has("tolerances") && root.get("tolerances").isArray()) {
                    ObjectMapper mapper = (ObjectMapper) p.getCodec();
                    for (JsonNode item : root.get("tolerances")) {
                        tolerances.add(mapper.treeToValue(item, ToleranceConfig.class));
                    }
                }

                Boolean caseSensitive = root.has("caseSensitive")
                        ? root.get("caseSensitive").asBoolean()
                        : (root.has("caseInsensitive") ? !root.get("caseInsensitive").asBoolean() : true);

                return new ComparisonExecuteRequest(keyColumns, tolerances, caseSensitive);
            }

            return new ComparisonExecuteRequest(List.of(), List.of(), true);
        }
    }
}
