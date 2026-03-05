package org.tuvarna.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;
import org.tuvarna.model.dto.FriendRequestDto;
import org.tuvarna.model.dto.PersonDto;
import org.tuvarna.model.entity.Person;
import org.tuvarna.model.relationship.FriendRequest;

import java.time.Instant;
import java.util.*;

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

    public boolean createUser(long userId, String name, long facultyNumber) {
        Session session = sessionFactory.openSession();
        try(Transaction tx = session.beginTransaction()) {

            String createCypher = """
                    CREATE (a:Person {id: $userId, name: $name, facultyNumber: $facultyNumber})
                    RETURN a
                    """;

            Result createResult = session.query(
                    createCypher, Map.of("userId", userId, "name", name, "facultyNumber", facultyNumber)
            );

            tx.commit();

            if(createResult.iterator().hasNext()) {
                return true;

            } else {
                throw new BadRequestException("Could not create person {"+userId+" "+facultyNumber+" "+name+"}");
            }
        } finally {
            session.clear();
        }
    }

    public boolean updateName(long userId, String newName) {
        Session session = sessionFactory.openSession();
        try (Transaction tx = session.beginTransaction()) {

            String cypher = """
                    MATCH (a:Person {id: $userId})
                    SET a.name = $name
                    RETURN a
                    """;

            Result updateResult = session.query(
                    cypher, Map.of(
                            "userId", userId,
                            "name", newName));

            tx.commit();

            if(updateResult.iterator().hasNext()) {
                return true;
            } else {
                throw new BadRequestException("Could not update person {"+userId+" "+newName+"}");
            }

        } finally {
            session.clear();
        }
    }

    public boolean createFriendshipRequest(long userA, long userB) {
        Session session = sessionFactory.openSession();
        try (Transaction tx = session.beginTransaction()) {

            String cypher = """
                    MATCH (x:Person {id: $a})
                    MATCH (y:Person {id: $b})
                    WHERE NOT EXISTS {(x)-[:BLOCKED]->(y)}
                      AND NOT EXISTS {(y)-[:BLOCKED]->(x)}
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

    public List<PersonDto> searchPeople(
            long requestingUserId,
            String query,
            int skip,
            int limit) {
        Session session = sessionFactory.openSession();
        try(Transaction tx = session.beginTransaction()) {

            String cypher = """
                    CALL db.index.fulltext.queryNodes("personSearchIndex", $query)
                    YIELD node AS p, score
                    WITH p, score
                    LIMIT 200
                   
                    MATCH (me:Person {id: $requestingUserId})
                    WHERE p.id <> $requestingUserId
                      AND NOT EXISTS { (me)-[:BLOCKED]->(p) }
                      AND NOT EXISTS { (p)-[:BLOCKED]->(me) }
                   
                    OPTIONAL MATCH (me)-[:FRIEND_OF]-(p)
                    WITH me, p, score,
                         CASE WHEN COUNT(p) > 0 THEN 1 ELSE 0 END AS isFriend
                   
                    OPTIONAL MATCH (me)-[:FRIEND_OF]-(common)-[:FRIEND_OF]-(p)
                    WITH p, score, isFriend, COUNT(common) AS mutualFriends
                   
                    RETURN p.id AS id
                    ORDER BY
                        isFriend DESC,
                        mutualFriends DESC,
                        score DESC
                    SKIP $skip LIMIT $limit
                    """;

            Iterable<PersonDto> res = session.queryDto(
                    cypher,
                    Map.of("query", query,
                            "requestingUserId", requestingUserId,
                            "skip", skip,
                            "limit", limit),

                    PersonDto.class);

            return toList(res);

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

    public List<PersonDto> getAllFriendsForUser(long userId) {
        Session session = sessionFactory.openSession();
        try {
            String cypher = """
                    MATCH (x:Person {id: $a})-[:FRIEND_OF]-(y:Person)
                    RETURN y.id AS id
                    """;

            Iterable<PersonDto> res =
                    session.queryDto(
                            cypher,
                            Map.of("a", userId),
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
                    MATCH (x:Person {id: $a})
                    MATCH (y:Person {id: $b})
                    MERGE (x)-[rb:BLOCKED]->(y)
                    WITH x, y, rb
                    OPTIONAL MATCH (x)-[rf:FRIEND_OF]-(y)
                    DELETE rf
                    WITH x, y, rb
                    OPTIONAL MATCH (y)-[rl:REQUESTED_FRIENDSHIP]->(x)
                    DELETE rl
                    WITH x, y, rb
                    OPTIONAL MATCH (x)-[rr:REQUESTED_FRIENDSHIP]->(y)
                    DELETE rr
                    RETURN rb
                    """;

            Result result = session.query(cypher, Map.of(
                    "a", userBlocker,
                    "b", userBlocked));
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

            Result result = session.query("""
                    MATCH (:Person {id: $blocking})-[:BLOCKED]->(:Person {id: $blocked})
                    RETURN COUNT(*) AS cnt
                    """,
                    Map.of("blocking", userBlocking, "blocked", userBlocked));

            if (!result.iterator().hasNext()) {
                return false;
            }

            return ((long) result.iterator().next().get("cnt")) > 0;

        } finally {
            session.clear();
        }
    }

}
