package org.tuvarna.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.transaction.Transaction;
import org.tuvarna.model.dto.ContactHydratedDto;
import org.tuvarna.model.dto.ContactSummaryDto;
import org.tuvarna.model.dto.FriendRequestDto;
import org.tuvarna.model.dto.ProfileDto;

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

    public boolean saveOrUpdate(ProfileDto profile) {
        Session session = sessionFactory.openSession();
        try (Transaction tx = session.beginTransaction()) {

            String cypher = """
            MERGE (p:Person {id: $userId})
            SET p.facultyNumber = $facultyNumber,
                p.name = $name,
                p.surname = $surname,
                p.fullName = $name + ' ' + $surname,
                p.keyToImage = $urlImage,
                p.specialty = $specialty,
                p.field = $field,
                p.form = $form,
                p.country = $country,
            RETURN p
        """;

            Result result = session.query(
                    cypher,
                    Map.ofEntries(
                            Map.entry("userId", profile.getUserId()),
                            Map.entry("facultyNumber", profile.getFacultyNumber()),
                            Map.entry("name", profile.getName()),
                            Map.entry("surname", profile.getSurname()),
                            Map.entry("urlImage", profile.getUrlImage()),
                            Map.entry("phoneNumber", profile.getPhoneNumber()),
                            Map.entry("specialty", profile.getSpecialty()),
                            Map.entry("field", profile.getField()),
                            Map.entry("form", profile.getForm()),
                            Map.entry("country", profile.getCountry()),
                            Map.entry("workplace", profile.getWorkplace()),
                            Map.entry("position", profile.getPosition())
                    )
            );


            tx.commit();
            return result.iterator().hasNext();

        } finally {
            session.clear();
        }
    }

    public List<String> getSpecialties(int limit) {
        Session session = sessionFactory.openSession();

        try {
            String cypher = """
            MATCH (p:Person)
            WHERE p.specialty IS NOT NULL
            RETURN DISTINCT p.specialty AS value
            ORDER BY value
            LIMIT $limit
        """;

            Result result = session.query(cypher, Map.of("limit", limit));

            List<String> values = new ArrayList<>();

            while (result.iterator().hasNext()) {
                values.add(result.iterator().next().get("value").toString());
            }

            return values;

        } finally {
            session.clear();
        }
    }

    public List<String> getFields(int limit) {
        Session session = sessionFactory.openSession();

        try {
            String cypher = """
            MATCH (p:Person)
            WHERE p.field IS NOT NULL
            RETURN DISTINCT p.field AS value
            ORDER BY value
            LIMIT $limit
        """;

            Result result = session.query(cypher, Map.of("limit", limit));

            List<String> values = new ArrayList<>();

            while (result.iterator().hasNext()) {
                values.add(result.iterator().next().get("value").toString());
            }

            return values;

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
    public List<ContactHydratedDto> searchPeople(
            long requestingUserId,
            String query,
            int skip,
            int limit) {

        Session session = sessionFactory.openSession();
        try {

            String cypher = """
            CALL db.index.fulltext.queryNodes("personSearchIndex", $query)
            YIELD node AS p, score
            WITH p, score
            LIMIT 200

            MATCH (me:Person {id: $requestingUserId})
            WHERE p.id <> $requestingUserId
              AND NOT EXISTS { (me)-[:BLOCKED]->(p) }
              AND NOT EXISTS { (p)-[:BLOCKED]->(me) }

            OPTIONAL MATCH (me)-[f:FRIEND_OF]-(p)
            WITH me, p, score, COUNT(f) > 0 AS isFriend

            OPTIONAL MATCH (me)-[:FRIEND_OF]-(common)-[:FRIEND_OF]-(p)
            WITH p, score, isFriend, COUNT(DISTINCT common) AS commonFriendCount

            RETURN 
                p.id AS userId,
                p.keyToImage AS keyToImage,
                p.fullName AS fullName,
                isFriend,
                commonFriendCount

            ORDER BY 
                isFriend DESC,
                commonFriendCount DESC,
                score DESC

            SKIP $skip LIMIT $limit
        """;

            Iterable<ContactHydratedDto> res =
                    session.queryDto(
                            cypher,
                            Map.of(
                                    "query", query,
                                    "requestingUserId", requestingUserId,
                                    "skip", skip,
                                    "limit", limit),
                            ContactHydratedDto.class
                    );

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

    public List<ContactSummaryDto> getCommonFriends(long userA,
                                            long userB,
                                            int skip,
                                            int limit) {
        Session session = sessionFactory.openSession();
        try {

            String cypher = """
                    MATCH (x:Person {id: $a})-[:FRIEND_OF]-(z:Person)-[:FRIEND_OF]-(y:Person {id: $b})
                    RETURN
                        z.id AS userId,
                        z.keyToImage AS keyToImage,
                        z.fullName AS fullName
                    SKIP $skip LIMIT $limit
                    """;

            Iterable<ContactSummaryDto> res =
                    session.queryDto(
                            cypher,
                            Map.of("a", userA,
                                    "b", userB,
                                    "skip", skip,
                                    "limit", limit),
                            ContactSummaryDto.class);

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

    public List<ContactSummaryDto> getFriendsForUser(long userId, int skip, int limit) {

        Session session = sessionFactory.openSession();
        try {

            String cypher = """
            MATCH (x:Person {id: $a})-[:FRIEND_OF]-(y:Person)
            RETURN
                y.id AS userId,
                y.keyToImage AS keyToImage,
                y.fullName AS fullName
            SKIP $skip LIMIT $limit
        """;

            Iterable<ContactSummaryDto> res =
                    session.queryDto(
                            cypher,
                            Map.of(
                                    "a", userId,
                                    "skip", skip,
                                    "limit", limit),
                            ContactSummaryDto.class
                    );

            return toList(res);

        } finally {
            session.clear();
        }
    }

    public List<ContactSummaryDto> getAllFriendsForUser(long userId) {
        Session session = sessionFactory.openSession();
        try {
            String cypher = """
                    MATCH (x:Person {id: $a})-[:FRIEND_OF]-(y:Person)
                    RETURN
                        y.id AS userId,
                        y.keyToImage AS keyToImage,
                        y.fullName AS fullName
                    """;

            Iterable<ContactSummaryDto> res =
                    session.queryDto(
                            cypher,
                            Map.of("a", userId),
                            ContactSummaryDto.class);

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

    public List<ContactSummaryDto> getBlockedUsersPerUser(long userId, int skip, int limit) {
        Session session = sessionFactory.openSession();

        try {
            String cypher = """
                    MATCH (s:Person {id: $id})-[:BLOCKED]->(b:Person)
                    RETURN b.id AS id
                    SKIP $skip LIMIT $limit
                    """;
            Iterable<ContactSummaryDto> res =
                    session.queryDto(
                            cypher,
                            Map.of(
                                    "id", userId,
                                    "skip", skip,
                                    "limit", limit),
                            ContactSummaryDto.class);

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



//    private static List<FriendRequestDto> fillDto(Iterable<FriendRequest> iterable) {
//        List<FriendRequestDto> list = new ArrayList<>();
//        if (iterable == null) return list;
//
//        for (FriendRequest r : iterable) {
//            list.add(new FriendRequestDto(
//                    r.personFrom.userId,
//                    r.personTo.userId));
//        }
//        return list;
//    }