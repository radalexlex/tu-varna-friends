package org.tuvarna.migrations;

import ac.simons.neo4j.migrations.core.JavaBasedMigration;
import ac.simons.neo4j.migrations.core.MigrationContext;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;

public class V002__IdPersonConstraint implements JavaBasedMigration {

    @Override
    public void apply(MigrationContext context) {
        Session session = context.getSession();
        try (Transaction tx = session.beginTransaction()) {

            tx.run("""
                CREATE CONSTRAINT person_id IF NOT EXISTS
                FOR (p:Person) REQUIRE p.id IS UNIQUE;
                """);

            tx.commit();
        }
    }
}
