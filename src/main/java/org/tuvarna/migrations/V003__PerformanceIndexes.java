package org.tuvarna.migrations;

import ac.simons.neo4j.migrations.core.JavaBasedMigration;
import ac.simons.neo4j.migrations.core.MigrationContext;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;

public class V003__PerformanceIndexes implements JavaBasedMigration {

    @Override
    public void apply(MigrationContext context) {
        Session session = context.getSession();

        try (Transaction tx = session.beginTransaction()) {

            tx.run("""
                CREATE INDEX person_facultyNumber IF NOT EXISTS
                FOR (p:Person) ON (p.facultyNumber);
            """);

            tx.run("""
                CREATE INDEX person_fullName IF NOT EXISTS
                FOR (p:Person) ON (p.fullName);
            """);

            tx.run("""
                CREATE INDEX person_specialty IF NOT EXISTS
                FOR (p:Person) ON (p.specialty);
            """);

            tx.run("""
                CREATE INDEX person_field IF NOT EXISTS
                FOR (p:Person) ON (p.field);
            """);

            tx.commit();
        }
    }
}