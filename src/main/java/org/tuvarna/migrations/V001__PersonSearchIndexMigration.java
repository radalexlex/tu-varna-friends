package org.tuvarna.migrations;

import ac.simons.neo4j.migrations.core.JavaBasedMigration;
import ac.simons.neo4j.migrations.core.MigrationContext;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;

public class V001__PersonSearchIndexMigration implements JavaBasedMigration {
    @Override
    public void apply(MigrationContext context) {
        Session session = context.getSession();
        try (Transaction tx = session.beginTransaction()) {

            tx.run("""
                    CREATE FULLTEXT INDEX personSearchIndex
                    IF NOT EXISTS
                    FOR (p:Person)
                    ON EACH [p.name, p.facultyNumber]
                    """);

            tx.commit();
        }
    }
}
