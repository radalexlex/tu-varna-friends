package org.tuvarna.model.relationship;

import io.smallrye.common.constraint.NotNull;
import org.neo4j.ogm.annotation.*;
import org.tuvarna.model.entity.Person;

import java.time.Instant;

@RelationshipEntity("REQUESTED_FRIENDSHIP")
public class FriendRequest {

    @Id
    @GeneratedValue
    public Long id;

    @StartNode
    public Person personFrom;

    @EndNode
    public Person personTo;

    @NotNull
    public Instant timeSent;

    public FriendRequest() {
    }

    public FriendRequest(Person personFrom, Person personTo) {
        this.personFrom = personFrom;
        this.personTo = personTo;
        this.timeSent = Instant.now(); // set automatically
    }

}
