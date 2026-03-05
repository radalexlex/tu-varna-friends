package org.tuvarna.model.entity;

import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.tuvarna.model.relationship.FriendRequest;

import java.util.List;

@NodeEntity
public class Person {

    @Id
    public Long userId;

    @Index
    public String name;

    @Index
    public Long facultyNumber;

    @Relationship(value = "FRIEND_OF", direction = Relationship.Direction.UNDIRECTED)
    public List<Person> friends;

    @Relationship(value = "BLOCKED", direction = Relationship.Direction.OUTGOING)
    public List<Person> blacklistedUsers;

    @Relationship(value = "REQUESTED_FRIENDSHIP", direction = Relationship.Direction.INCOMING)
    public List<FriendRequest> incomingRequests;

    @Relationship(value = "REQUESTED_FRIENDSHIP", direction = Relationship.Direction.OUTGOING)
    public List<FriendRequest> outgoingRequests;


    public Person() {
    }

}
