package com.zklcsoftware.common.web.initialize;

import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.GraphQLScalarType;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LongScalarType extends GraphQLScalarType {
    public LongScalarType() {
        super("Long", "Long value", new Coercing<Long, Long>() {

            public Long serialize(Object input) {
                return Long.valueOf(String.valueOf(input));
            }

            public Long parseValue(Object input) {
                return this.serialize(Long.valueOf(String.valueOf(input)));
            }

            public Long parseLiteral(Object input) {
                if (!(input instanceof Long)) {
                    throw new CoercingParseLiteralException("Expected AST type 'Long' but was ");
                } else {
                    return Long.valueOf(String.valueOf(input));
                }
            }
        });
    }
}
