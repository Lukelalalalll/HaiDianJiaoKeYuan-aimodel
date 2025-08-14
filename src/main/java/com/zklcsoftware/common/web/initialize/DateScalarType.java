package com.zklcsoftware.common.web.initialize;

import graphql.schema.*;
import java.text.SimpleDateFormat;
import java.util.Date;

// 需要继承GraphQLScalarType
public class DateScalarType extends GraphQLScalarType {

    public DateScalarType() {
        super("Date", "Date value", new Coercing<String, String>() {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            public String serialize(Object input) {
                return sdf.format(input);
            }

            public String parseValue(Object input) {
                return this.serialize(sdf.format(input));
            }

            public String parseLiteral(Object input) {
                if (!(input instanceof Date)) {
                    throw new CoercingParseLiteralException("Expected AST type 'Date' but was ");
                } else {
                    return sdf.format(input);
                }
            }
        });
    }
}
