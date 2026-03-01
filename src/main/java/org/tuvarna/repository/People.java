package org.tuvarna.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;
import org.tuvarna.model.dto.FriendRequestDto;
import org.tuvarna.model.dto.PersonDto;
import org.tuvarna.model.relationship.FriendRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class People {

    @Inject
    SessionFactory sessionFactory;

    private static <T> List<T> toList(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();
        if (iterable != null) {
            iterable.forEach(list::add);
        }
        return list;
    }

    private static List<FriendRequestDto> fillDto(Iterable<FriendRequest> iterable) {
        List<FriendRequestDto> list = new ArrayList<>();
        if (iterable == null) return list;

        for (FriendRequest r : iterable) {
            list.add(new FriendRequestDto(
                    r.personFrom.userId,
                    r.personTo.userId));
        }
        return list;
    }

    public boolean createFriendshipRequest(long userA, long userB) {
        Session session = sessionFactory.openSession();
        try (Transaction tx = session.beginTransaction()) {

            String cypher = """
                    MERGE (x:Person {id: $a})
                    MERGE (y:Person {id: $b})
                    WITH x, y
                    WHERE NOT EXISTS {(x)-[:BLOCKED]->(y)} AND NOT EXISTS {(y)-[:BLOCKED]->(x)}
                    MERGE (x)-[r:REQUESTED_FRIENDSHIP]->(y)
                    ON CREATE SET r.timeSent = $timeSent
                    WITH x, y, r
                    OPTIONAL MATCH (y)-[ry:REQUESTED_FRIENDSHIP]->(x)
                    WITH x, y, r, ry
                    WHERE ry IS NOT NULL
                    DELETE r, ry
                    MERGE (x)-[f:FRIEND_OF]-(y)
                    RETURN f
                    """;

            Result result = session.query(
                    cypher,
                    Map.of("a", userA,
                            "b", userB,
                            "timeSent", Instant.now()));
            tx.commit();

            boolean newFriendshipWasCreated;
            newFriendshipWasCreated = result.iterator().hasNext();

            return newFriendshipWasCreated;

        } finally {
            session.clear();
        }
    }

    public boolean deleteFriend(long userDeleting, long userDeleted) {
        Session session = sessionFactory.openSession();
        try (Transaction tx = session.beginTransaction()) {

            String cypher = """
                    MATCH (x:Person {id: $a})-[f:FRIEND_OF]-(y:Person {id: $b})
                    WITH x, f, y
                    DELETE f
                    RETURN f
                    """;

            Result result = session.query(
                    cypher,
                    Map.of("a", userDeleting,
                            "b", userDeleted));
            tx.commit();

            return result.iterator().hasNext();
        } finally {
            session.clear();
        }
    }

    public List<PersonDto> getCommonFriends(long userA,
                                            long userB,
                                            int skip,
                                            int limit) {
        Session session = sessionFactory.openSession();
        try {

            String cypher = """
                    MATCH (x:Person {id: $a})-[:FRIEND_OF]-(z:Person)-[:FRIEND_OF]-(y:Person {id: $b})
                    RETURN z.id AS id
                    SKIP $skip LIMIT $limit
                    """;

            Iterable<PersonDto> res =
                    session.queryDto(
                            cypher,
                            Map.of("a", userA,
                                    "b", userB,
                                    "skip", skip,
                                    "limit", limit),
                            PersonDto.class);

            return toList(res);

        } finally {
            session.clear();
        }
    }

    public List<FriendRequestDto> getIncomingRequests(long userId,
                                                      int skip,
                                                      int limit) {
        Session session = sessionFactory.openSession();
        try {

            String cypher = """
                    MATCH (x:Person {id: $userId})<-[r:REQUESTED_FRIENDSHIP]-(y:Person)
                    RETURN x.id AS toUserId, y.id AS fromUserId
                    ORDER BY r.timeSent DESC
                    SKIP $skip LIMIT $limit
                    """;

            Iterable<FriendRequestDto> res =
                    session.queryDto(
                            cypher,
                            Map.of("userId", userId,
                                    "skip", skip,
                                    "limit", limit),
                            FriendRequestDto.class);

            return toList(res);

        } finally {
            session.clear();
        }
    }

    public List<FriendRequestDto> getOutgoingRequests(long userId, int skip, int limit) {
        Session session = sessionFactory.openSession();
        try {

            String cypher = """
                    MATCH (x:Person {id: $userId})-[r:REQUESTED_FRIENDSHIP]->(y:Person)
                    RETURN x.id AS fromUserId, y.id AS toUserId
                    ORDER BY r.timeSent DESC
                    SKIP $skip LIMIT $limit
                    """;

            Iterable<FriendRequestDto> res =
                    session.queryDto(
                            cypher,
                            Map.of("userId", userId,
                                    "skip", skip,
                                    "limit", limit),
                            FriendRequestDto.class);

            return toList(res);

        } finally {
            session.clear();
        }
    }

    public boolean deleteRequest(long userA, long userB) {
        Session session = sessionFactory.openSession();
        try (Transaction tx = session.beginTransaction()) {

            String cypher = """
                    MATCH (a:Person {id: $a})-[r:REQUESTED_FRIENDSHIP]-(b:Person {id: $b})
                    WITH a, r, b
                    DELETE r
                    RETURN r
                    """;

            Result result = session.query(cypher, Map.of(
                    "a", userA,
                    "b", userB));
            tx.commit();

            return result.iterator().hasNext();

        } finally {
            session.clear();
        }
    }

    public List<PersonDto> getFriendsForUser(long userId, int skip, int limit) {
        Session session = sessionFactory.openSession();
        try {
            String cypher = """
                    MATCH (x:Person {id: $a})-[:FRIEND_OF]-(y:Person)
                    RETURN y.id AS id
                    SKIP $skip LIMIT $limit
                    """;

            Iterable<PersonDto> res =
                    session.queryDto(
                            cypher,
                            Map.of(
                                    "a", userId,
                                    "skip", skip,
                                    "limit", limit),
                            PersonDto.class);

            return toList(res);

        } finally {
            session.clear();
        }
    }

    public boolean blockUser(long userBlocker, long userBlocked) {
        Session session = sessionFactory.openSession();
        try (Transaction tx = session.beginTransaction()) {
            String cypher = """
                    MERGE (x:Person {id: $a})
                    MERGE (y:Person {id: $b})
                    MERGE (x)-[rb:BLOCKED]->(y)
                    WITH x, rb, y
                    OPTIONAL MATCH (x)-[rf:FRIEND_OF]-(y)
                    WITH x, rf, y, rb
                    DELETE rf
                    WITH x, y, rb
                    OPTIONAL MATCH (y)-[rl:REQUESTED_FRIENDSHIP]->(x)
                    WITH x, rl, y, rb
                    DELETE rl
                    WITH x, y, rb
                    OPTIONAL MATCH (x)-[rr:REQUESTED_FRIENDSHIP]->(y)
                    WITH x, rr, y, rb
                    DELETE rr
                    WITH rb
                    RETURN rb
                    """;

            Result result = session.query(cypher, Map.of("a", userBlocker, "b", userBlocked));
            tx.commit();

            return result.iterator().hasNext();

        } finally {
            session.clear();
        }
    }

    public List<PersonDto> getBlockedUsersPerUser(long userId, int skip, int limit) {
        Session session = sessionFactory.openSession();

        try {
            String cypher = """
                    MATCH (s:Person {id: $id})-[:BLOCKED]->(b:Person)
                    RETURN b.id AS id
                    SKIP $skip LIMIT $limit
                    """;
            Iterable<PersonDto> res =
                    session.queryDto(
                            cypher,
                            Map.of(
                                    "id", userId,
                                    "skip", skip,
                                    "limit", limit),
                            PersonDto.class);

            return toList(res);

        } finally {
            session.clear();
        }
    }

    public boolean checkIfBlocked(long userBlocking, long userBlocked) {
        Session session = sessionFactory.openSession();

        try {
            String cypher = """
                    MATCH (:Person {id: $blocking})-[:BLOCKED]->(:Person {id: $blocked})
                    RETURN COUNT(*) AS cnt
                    """;

            Result result = session.query(cypher, Map.of("blocking", userBlocking, "blocked", userBlocked));

            return result.iterator().hasNext() && ((long) result.iterator().next().get("cnt")) > 0;

        } finally {
            session.clear();
        }
    }

}
