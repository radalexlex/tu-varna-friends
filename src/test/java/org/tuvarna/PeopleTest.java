//package org.tuvarna;
//
//import io.quarkus.test.junit.QuarkusTest;
//import jakarta.inject.Inject;
//import jakarta.ws.rs.BadRequestException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.neo4j.ogm.model.Result;
//import org.neo4j.ogm.session.Session;
//import org.neo4j.ogm.session.SessionFactory;
//import org.neo4j.ogm.transaction.Transaction;
//import org.tuvarna.mocks.SessionFactoryTestProducer;
//import org.tuvarna.model.dto.FriendRequestDto;
//import org.tuvarna.model.dto.PersonDto;
//import org.tuvarna.repository.People;
//
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@QuarkusTest
//class PeopleTest {
//
//    @Inject
//    SessionFactory sessionFactory; // proxy
//
//    @Inject
//    People people;
//
//    Session session;
//    Transaction tx;
//    Result result;
//
//    @BeforeEach
//    void setUp() {
//        session = mock(Session.class);
//        tx = mock(Transaction.class);
//        result = mock(Result.class);
//
//        reset(SessionFactoryTestProducer.SESSION_FACTORY);
//
//        when(SessionFactoryTestProducer.SESSION_FACTORY.openSession())
//                .thenReturn(session);
//
//        when(session.beginTransaction()).thenReturn(tx);
//    }
//
//
//    private Iterator<Map<String, Object>> mockSingleRowResult(String key, Object value) {
//        Map<String, Object> row = new HashMap<>();
//        row.put(key, value);
//
//        Iterator<Map<String, Object>> it = List.of(row).iterator();
//        when(result.iterator()).thenReturn(it);
//        return it;
//    }
//
//    private Iterator<Map<String, Object>> mockEmptyResult() {
//        Iterator<Map<String, Object>> it = Collections.<Map<String, Object>>emptyList().iterator();
//        when(result.iterator()).thenReturn(it);
//        return it;
//    }
//
//    @Test
//    void createUser_success() {
//        mockSingleRowResult("a", new Object());
//
//        when(session.query(anyString(), anyMap())).thenReturn(result);
//
//        assertTrue(people.createUser(1L, "A", 10L));
//
//        verify(tx).commit();
//        verify(session).clear();
//    }
//
//    @Test
//    void createUser_fail() {
//        mockEmptyResult();
//
//        when(session.query(anyString(), anyMap())).thenReturn(result);
//
//        assertThrows(BadRequestException.class,
//                () -> people.createUser(1L, "A", 10L));
//    }
//
//    @Test
//    void updateName_success() {
//        mockSingleRowResult("a", new Object());
//
//        when(session.query(anyString(), anyMap())).thenReturn(result);
//
//        assertTrue(people.updateName(1L, "B"));
//        verify(tx).commit();
//    }
//
//    @Test
//    void updateName_fail() {
//        mockEmptyResult();
//
//        when(session.query(anyString(), anyMap())).thenReturn(result);
//
//        assertThrows(BadRequestException.class,
//                () -> people.updateName(1L, "B"));
//    }
//
//    @Test
//    void createFriendshipRequest_created() {
//        mockSingleRowResult("f", new Object());
//
//        when(session.query(anyString(), anyMap())).thenReturn(result);
//
//        assertTrue(people.createFriendshipRequest(1L, 2L));
//    }
//
//    @Test
//    void createFriendshipRequest_notCreated() {
//        mockEmptyResult();
//
//        when(session.query(anyString(), anyMap())).thenReturn(result);
//
//        assertFalse(people.createFriendshipRequest(1L, 2L));
//    }
//
//    @Test
//    void deleteFriend_success() {
//        mockSingleRowResult("f", new Object());
//
//        when(session.query(anyString(), anyMap())).thenReturn(result);
//
//        assertTrue(people.deleteFriend(1L, 2L));
//    }
//
//    @Test
//    void deleteFriend_fail() {
//        mockEmptyResult();
//
//        when(session.query(anyString(), anyMap())).thenReturn(result);
//
//        assertFalse(people.deleteFriend(1L, 2L));
//    }
//
//    @Test
//    void deleteRequest_success() {
//        mockSingleRowResult("r", new Object());
//
//        when(session.query(anyString(), anyMap())).thenReturn(result);
//
//        assertTrue(people.deleteRequest(1L, 2L));
//    }
//
//    @Test
//    void deleteRequest_fail() {
//        mockEmptyResult();
//
//        when(session.query(anyString(), anyMap())).thenReturn(result);
//
//        assertFalse(people.deleteRequest(1L, 2L));
//    }
//
//    @Test
//    void blockUser_success() {
//        mockSingleRowResult("rb", new Object());
//
//        when(session.query(anyString(), anyMap())).thenReturn(result);
//
//        assertTrue(people.blockUser(1L, 2L));
//    }
//
//    @Test
//    void blockUser_fail() {
//        mockEmptyResult();
//
//        when(session.query(anyString(), anyMap())).thenReturn(result);
//
//        assertFalse(people.blockUser(1L, 2L));
//    }
//
//    @Test
//    void checkIfBlocked_true() {
//        Map<String, Object> row = new HashMap<>();
//        row.put("cnt", 3L);
//
//        when(result.iterator()).thenReturn(List.of(row).iterator());
//        when(session.query(anyString(), anyMap())).thenReturn(result);
//
//        assertTrue(people.checkIfBlocked(1L, 2L));
//    }
//
//    @Test
//    void checkIfBlocked_false_noResult() {
//        when(result.iterator()).thenReturn(Collections.emptyIterator());
//        when(session.query(anyString(), anyMap())).thenReturn(result);
//
//        assertFalse(people.checkIfBlocked(1L, 2L));
//    }
//
//    @Test
//    void checkIfBlocked_false_zero() {
//        Map<String, Object> row = new HashMap<>();
//        row.put("cnt", 0L);
//
//        when(result.iterator()).thenReturn(List.of(row).iterator());
//        when(session.query(anyString(), anyMap())).thenReturn(result);
//
//        assertFalse(people.checkIfBlocked(1L, 2L));
//    }
//
//    @Test
//    void searchPeople_returnsList() {
//        PersonDto dto = new PersonDto(1L, "A", 10L);
//
//        List<PersonDto> iterable = List.of(dto);
//        when(session.queryDto(anyString(), anyMap(), eq(PersonDto.class)))
//                .thenReturn(iterable);
//
//        List<PersonDto> res = people.searchPeople(1L, "q", 0, 10);
//
//        assertEquals(1, res.size());
//        assertEquals(1L, res.get(0).id());
//    }
//
//    @Test
//    void getCommonFriends_returnsList() {
//        PersonDto dto = new PersonDto(2L, null, null);
//
//        when(session.queryDto(anyString(), anyMap(), eq(PersonDto.class)))
//                .thenReturn(List.of(dto));
//
//        List<PersonDto> res = people.getCommonFriends(1L, 2L, 0, 10);
//
//        assertEquals(1, res.size());
//    }
//
//    @Test
//    void getIncomingRequests_returnsList() {
//        FriendRequestDto dto = new FriendRequestDto(1L, 2L);
//
//        when(session.queryDto(anyString(), anyMap(), eq(FriendRequestDto.class)))
//                .thenReturn(List.of(dto));
//
//        List<FriendRequestDto> res = people.getIncomingRequests(1L, 0, 10);
//
//        assertEquals(1, res.size());
//    }
//
//    @Test
//    void getOutgoingRequests_returnsList() {
//        FriendRequestDto dto = new FriendRequestDto(1L, 2L);
//
//        when(session.queryDto(anyString(), anyMap(), eq(FriendRequestDto.class)))
//                .thenReturn(List.of(dto));
//
//        List<FriendRequestDto> res = people.getOutgoingRequests(1L, 0, 10);
//
//        assertEquals(1, res.size());
//    }
//
//    @Test
//    void getFriendsForUser_returnsList() {
//        PersonDto dto = new PersonDto(2L, null, null);
//
//        when(session.queryDto(anyString(), anyMap(), eq(PersonDto.class)))
//                .thenReturn(List.of(dto));
//
//        List<PersonDto> res = people.getFriendsForUser(1L, 0, 10);
//
//        assertEquals(1, res.size());
//    }
//
//    @Test
//    void getAllFriendsForUser_returnsList() {
//        PersonDto dto = new PersonDto(2L, null, null);
//
//        when(session.queryDto(anyString(), anyMap(), eq(PersonDto.class)))
//                .thenReturn(List.of(dto));
//
//        List<PersonDto> res = people.getAllFriendsForUser(1L);
//
//        assertEquals(1, res.size());
//    }
//
//    @Test
//    void getBlockedUsersPerUser_returnsList() {
//        PersonDto dto = new PersonDto(2L, null, null);
//
//        when(session.queryDto(anyString(), anyMap(), eq(PersonDto.class)))
//                .thenReturn(List.of(dto));
//
//        List<PersonDto> res = people.getBlockedUsersPerUser(1L, 0, 10);
//
//        assertEquals(1, res.size());
//    }
//}