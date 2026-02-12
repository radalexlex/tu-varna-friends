package org.tuvarna.entity;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.List;

@NodeEntity
public class Person {

    @Id
    public Long userId;
    public List<Person> friends;
    @Relationship
    public List<Person> incomingRequesters;
    public List<Person> outgoingRequesters;
    public List<Person> blacklistedUsers;

    public Person() {
    }

    public Person(long userId, List<Person> friends, List<Person> incomingRequesters, List<Person> outgoingRequesters, List<Person> blacklistedUsers) {
        this.userId = userId;
        this.friends = friends;
        this.incomingRequesters = incomingRequesters;
        this.outgoingRequesters = outgoingRequesters;
        this.blacklistedUsers = blacklistedUsers;
    }

}
