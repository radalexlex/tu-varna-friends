package org.tuvarna.model.relationship;

import org.neo4j.ogm.annotation.*;
import org.tuvarna.model.entity.Person;

@RelationshipEntity("REQUESTED_TO")
public class FriendRequest {

    @Id
    @GeneratedValue
    public long id;

    @StartNode
    public Person personFrom;

    @EndNode
    public Person personTo;

}
