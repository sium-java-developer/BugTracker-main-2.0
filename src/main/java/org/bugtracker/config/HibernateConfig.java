package org.bugtracker.config;

import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.boot.model.naming.PhysicalNamingStrategy; // Use the interface
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// For standard snake_case (e.g., my_table_name, my_column_name),
// you can use Spring Boot's default by setting the property in application.properties:
// spring.jpa.hibernate.naming.physical-strategy=org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy
// In that case, this custom bean would not be needed.

@Configuration(proxyBeanMethods = false)
public class HibernateConfig {

    /**
     * Provides a custom physical naming strategy that converts camelCase names
     * to UPPER_SNAKE_CASE with a specific rule for inserting underscores.
     * An underscore is inserted between a lowercase char, an uppercase char, and another lowercase char.
     * Example: "myTableEntity" becomes "MY_TABLE_ENTITY".
     * Note: This rule is specific and might not cover all camelCase to snake_case scenarios robustly.
     * For a more general camelCase to UPPER_SNAKE_CASE, a different approach might be needed.
     */
    @Bean
    public PhysicalNamingStrategy customUpperSnakeCasePhysicalNamingStrategy() { // Return the interface
        return new PhysicalNamingStrategy() { // Implement the interface directly
            @Override
            public Identifier toPhysicalCatalogName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
                return convertToUpperSnakeCase(logicalName);
            }

            @Override
            public Identifier toPhysicalSchemaName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
                return convertToUpperSnakeCase(logicalName);
            }

            @Override
            public Identifier toPhysicalTableName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
                return convertToUpperSnakeCase(logicalName);
            }

            @Override
            public Identifier toPhysicalSequenceName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
                return convertToUpperSnakeCase(logicalName);
            }

            @Override
            public Identifier toPhysicalColumnName(Identifier logicalName, JdbcEnvironment jdbcEnvironment) {
                return convertToUpperSnakeCase(logicalName);
            }

            private Identifier convertToUpperSnakeCase(final Identifier name) {
                if (name == null || name.getText() == null || name.getText().isEmpty()) {
                    return name;
                }
                // The original logic:
                StringBuilder builder = new StringBuilder(name.getText().replace('.', '_'));
                for (int i = 1; i < builder.length() - 1; i++) {
                    if (isUnderscoreRequired(builder.charAt(i - 1), builder.charAt(i), builder.charAt(i + 1))) {
                        builder.insert(i++, '_');
                    }
                }
                return Identifier.toIdentifier(builder.toString().toUpperCase());

                // Alternative: A more general approach for camelCase to snake_case (before uppercasing)
                // if the specific rule above is too restrictive:
                /*
                String text = name.getText();
                String regex = "([a-z])([A-Z]+)";
                String replacement = "$1_$2";
                String snakeCaseText = text.replaceAll(regex, replacement);
                return Identifier.toIdentifier(snakeCaseText.toUpperCase());
                */
            }

            // Original underscore requirement logic
            private boolean isUnderscoreRequired(final char before, final char current, final char after) {
                return Character.isLowerCase(before) && Character.isUpperCase(current) && Character.isLowerCase(after);
            }
        };
    }
}