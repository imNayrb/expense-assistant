package com.imnayrb.expenseassistant.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.imnayrb.expenseassistant.expense.Category;

/** Piccole utility per costruire gli JSON Schema dei tool, così restano leggibili nel codice. */
final class JsonSchemas {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonSchemas() {
    }

    static ObjectNode object() {
        ObjectNode schema = MAPPER.createObjectNode();
        schema.put("type", "object");
        schema.putObject("properties");
        schema.putArray("required");
        return schema;
    }

    static ObjectNode property(ObjectNode schema, String name, String type, String description, boolean required) {
        ObjectNode property = ((ObjectNode) schema.get("properties")).putObject(name);
        property.put("type", type);
        property.put("description", description);
        if (required) {
            ((ArrayNode) schema.get("required")).add(name);
        }
        return property;
    }

    static ObjectNode dateProperty(ObjectNode schema, String name, String description, boolean required) {
        // Le descrizioni dei tool sono lette da Claude, quindi restano in inglese come i prompt di sistema.
        return property(schema, name, "string", description + " Format: YYYY-MM-DD.", required)
                .put("format", "date");
    }

    static ObjectNode categoryProperty(ObjectNode schema, String name, boolean required) {
        ObjectNode property = property(schema, name, "string", "Expense category code.", required);
        ArrayNode values = property.putArray("enum");
        for (Category category : Category.values()) {
            values.add(category.name());
        }
        return property;
    }
}
