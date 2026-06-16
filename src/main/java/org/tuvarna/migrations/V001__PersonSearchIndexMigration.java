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
            //TODO: Adapt for latest PersonDTO that User-service would use
            tx.run("""
                    CREATE FULLTEXT INDEX personSearchIndex
                    IF NOT EXISTS
                    FOR (p:Person)
                    ON EACH [
                        p.fullName,
                        p.facultyNumber,
                        p.specialty,
                        p.field,
                        p.form
                    ]
                    """);
            tx.commit();
        }
    }
}
