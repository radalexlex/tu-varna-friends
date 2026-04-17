package org.tuvarna.mocks;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.enterprise.inject.Produces;
import org.mockito.Mockito;
import org.neo4j.ogm.session.SessionFactory;

@Alternative
@Priority(1)
@ApplicationScoped
public class SessionFactoryTestProducer {

    public static final SessionFactory SESSION_FACTORY = Mockito.mock(SessionFactory.class);

    @Produces
    @ApplicationScoped
    public SessionFactory sessionFactory() {
        return SESSION_FACTORY;
    }
}