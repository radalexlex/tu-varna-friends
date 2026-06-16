//package org.tuvarna;
//
//import io.quarkus.test.InjectMock;
//import io.quarkus.test.junit.QuarkusTest;
//import io.restassured.http.ContentType;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.tuvarna.model.dto.PersonDto;
//import org.tuvarna.repository.People;
//
//import java.util.List;
//
//import static io.restassured.RestAssured.given;
//import static org.hamcrest.Matchers.*;
//import static org.mockito.ArgumentMatchers.*;
//
//@QuarkusTest
//class PersonResourceTest {
//
//    @InjectMock
//    People people;
//
//    @Test
//    void createUser_success() {
//        Mockito.when(people.createUser(anyLong(), anyString(), anyLong()))
//                .thenReturn(true);
//
//        given()
//                .contentType(ContentType.JSON)
//                .body("""
//                        {
//                          "userId": 1,
//                          "name": "John",
//                          "facultyNumber": 100
//                        }
//                        """)
//                .when()
//                .post("/people/create")
//                .then()
//                .statusCode(200)
//                .body(is("true"));
//    }
//
//    @Test
//    void createUser_validationFails() {
//        given()
//                .contentType(ContentType.JSON)
//                .body("""
//                        {
//                          "userId": 0,
//                          "name": "",
//                          "facultyNumber": 0
//                        }
//                        """)
//                .when()
//                .post("/people/create")
//                .then()
//                .statusCode(400)
//                .body("title", equalTo("Constraint Violation"))
//                .body("violations.size()", greaterThan(0));
//    }
//
//    @Test
//    void updateName_success() {
//        Mockito.when(people.updateName(eq(1L), anyString()))
//                .thenReturn(true);
//
//        given()
//                .contentType(ContentType.JSON)
//                .body("""
//                          {"newName":"Alice"}
//                        """)
//                .when()
//                .put("/people/1/name")
//                .then()
//                .statusCode(200)
//                .body(is("true"));
//    }
//
//    @Test
//    void updateName_invalidUserId() {
//        given()
//                .contentType(ContentType.JSON)
//                .body(
//                        """
//                                {"newName":"Alice"}
//                                """)
//                .when()
//                .put("/people/0/name")
//                .then()
//                .statusCode(400)
//                .body("title", equalTo("Constraint Violation"))
//                .body("violations.size()", greaterThan(0));
//    }
//
//    @Test
//    void searchPeople_success() {
//        Mockito.when(people.searchPeople(eq(1L), eq("john"), anyInt(), anyInt()))
//                .thenReturn(List.of(new PersonDto(2L, "John", 10L)));
//
//        given()
//                .queryParam("query", "john")
//                .when()
//                .get("/people/1/search/0")
//                .then()
//                .statusCode(200)
//                .body("$.size()", is(1))
//                .body("[0].id", is(2));
//    }
//
//    @Test
//    void getFriends_success() {
//        Mockito.when(people.getFriendsForUser(eq(1L), anyInt(), anyInt()))
//                .thenReturn(List.of(new PersonDto(2L, "Bob", 10L)));
//
//        given()
//                .when()
//                .get("/people/1/friends/0")
//                .then()
//                .statusCode(200)
//                .body("$.size()", is(1));
//    }
//
//    @Test
//    void getAllFriends_success() {
//        Mockito.when(people.getAllFriendsForUser(1L))
//                .thenReturn(List.of(
//                        new PersonDto(2L, "Bob", 10L),
//                        new PersonDto(3L, "Alice", 11L)
//                ));
//
//        given()
//                .when()
//                .get("/people/1/friends-all")
//                .then()
//                .statusCode(200)
//                .body("$.size()", is(2));
//    }
//
//    @Test
//    void sendFriendRequest_success() {
//        Mockito.when(people.createFriendshipRequest(1L, 2L))
//                .thenReturn(true);
//
//        given()
//                .contentType(ContentType.JSON)
//                .body("""
//                        {"userA":1,"userB":2}
//                        """)
//                .when()
//                .post("/people/send-request")
//                .then()
//                .statusCode(200)
//                .body(is("true"));
//    }
//
//    @Test
//    void sendFriendRequest_self_shouldFail() {
//        given()
//                .contentType(ContentType.JSON)
//                .body("""
//                        {"userA":1,"userB":1}
//                        """)
//                .when()
//                .post("/people/send-request")
//                .then()
//                .statusCode(400)
//                .body("error", equalTo("Cannot send friend request to self."));
//    }
//
//    @Test
//    void removeFriendRequest_success() {
//        Mockito.when(people.deleteRequest(1L, 2L))
//                .thenReturn(true);
//
//        given()
//                .contentType(ContentType.JSON)
//                .body("""
//                        {"userA":1,"userB":2}
//                        """)
//                .when()
//                .post("/people/remove-request")
//                .then()
//                .statusCode(200)
//                .body(is("true"));
//    }
//
//    @Test
//    void deleteFriend_success() {
//        Mockito.when(people.deleteFriend(1L, 2L))
//                .thenReturn(true);
//
//        given()
//                .contentType(ContentType.JSON)
//                .body("""
//                        {"userA":1,"userB":2}
//                        """)
//                .when()
//                .post("/people/delete-friend")
//                .then()
//                .statusCode(200)
//                .body(is("true"));
//    }
//
//    @Test
//    void blockUser_success() {
//        Mockito.when(people.blockUser(1L, 2L))
//                .thenReturn(true);
//
//        given()
//                .contentType(ContentType.JSON)
//                .body("""
//                          {"blocker":1,"blocked":2}
//                        """)
//                .when()
//                .post("/people/add-blacklist")
//                .then()
//                .statusCode(200)
//                .body(is("true"));
//    }
//
//    @Test
//    void blockUser_self_shouldFail() {
//        given()
//                .contentType(ContentType.JSON)
//                .body("""
//                          {"blocker":1,"blocked":1}
//                        """)
//                .when()
//                .post("/people/add-blacklist")
//                .then()
//                .statusCode(400)
//                .body("error", equalTo("Cannot block yourself."));
//    }
//
//    @Test
//    void checkIfBlocked_success() {
//        Mockito.when(people.checkIfBlocked(1L, 2L))
//                .thenReturn(true);
//
//        given()
//                .when()
//                .get("/people/1/2/is-blocked")
//                .then()
//                .statusCode(200)
//                .body(is("true"));
//    }
//
//    @Test
//    void checkIfBlocked_sameUser_shouldFail() {
//        given()
//                .when()
//                .get("/people/1/1/is-blocked")
//                .then()
//                .statusCode(400)
//                .body("error", equalTo("Users must be different."));
//    }
//}