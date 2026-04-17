package org.tuvarna.resource;


import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.tuvarna.model.dto.FriendRequestDto;
import org.tuvarna.model.dto.PersonDto;
import org.tuvarna.repository.People;

import java.util.List;

@Path("/people")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonResource {

    private static final int PAGE_SIZE = 15;

    @Inject
    People people;

    @POST
    @Path("/create")
    public boolean createUser(@Valid CreateUserRequest request) {
        return people.createUser(
                request.userId,
                request.name,
                request.facultyNumber
        );
    }

    @PUT
    @Path("/{userId}/name")
    public boolean updateName(
            @PathParam("userId") @Min(1) long userId,
            @Valid UpdateNameRequest request) {

        return people.updateName(userId, request.newName);
    }

    @GET
    @Path("{userId}/search/{page}")
    public List<PersonDto> searchPeople(
            @PathParam("userId") @Min(1) long userId,
            @PathParam("page") @Min(0) int page,
            @QueryParam("query") @NotBlank String query) {

        int skip = page * PAGE_SIZE;
        return people.searchPeople(userId, query.trim(), skip, PAGE_SIZE);
    }

    @GET
    @Path("{userId}/friends/{page}")
    public List<PersonDto> getFriendsOfPerson(
            @PathParam("userId") @Min(1) long userId,
            @PathParam("page") @Min(0) int page) {

        int skip = page * PAGE_SIZE;
        return people.getFriendsForUser(userId, skip, PAGE_SIZE);
    }

    @GET
    @Path("{userId}/friends-all")
    public List<PersonDto> getAllFriendsOfPerson(
            @PathParam("userId") @Min(1) long userId) {

        return people.getAllFriendsForUser(userId);
    }

    @GET
    @Path("{userA}/{userB}/common-friends/{page}")
    public List<PersonDto> getCommonABFriends(
            @PathParam("userA") @Min(1) long userA,
            @PathParam("userB") @Min(1) long userB,
            @PathParam("page") @Min(0) int page) {

        int skip = page * PAGE_SIZE;
        return people.getCommonFriends(userA, userB, skip, PAGE_SIZE);
    }

    @GET
    @Path("{userId}/incoming-requests/{page}")
    public List<FriendRequestDto> getIncomingRequests(
            @PathParam("userId") @Min(1) long userId,
            @PathParam("page") @Min(0) int page) {

        int skip = page * PAGE_SIZE;
        return people.getIncomingRequests(userId, skip, PAGE_SIZE);
    }

    @GET
    @Path("{userId}/outgoing-requests/{page}")
    public List<FriendRequestDto> getOutgoingRequests(
            @PathParam("userId") @Min(1) long userId,
            @PathParam("page") @Min(0) int page) {

        int skip = page * PAGE_SIZE;
        return people.getOutgoingRequests(userId, skip, PAGE_SIZE);
    }

    @GET
    @Path("{userId}/blocked/{page}")
    public List<PersonDto> getBlockedUsers(
            @PathParam("userId") @Min(1) long userId,
            @PathParam("page") @Min(0) int page) {

        int skip = page * PAGE_SIZE;
        return people.getBlockedUsersPerUser(userId, skip, PAGE_SIZE);
    }

    @POST
    @Path("/send-request")
    public boolean sendFriendRequest(@Valid UserAction action) {

        if (action.userA == action.userB) {
            throw new BadRequestException("Cannot send friend request to self.");
        }

        return people.createFriendshipRequest(action.userA, action.userB);
    }

    @POST
    @Path("/remove-request")
    public boolean removeFriendRequest(@Valid UserAction action) {
        return people.deleteRequest(action.userA, action.userB);
    }

    @POST
    @Path("/delete-friend")
    public boolean deleteFriend(@Valid UserAction action) {
        return people.deleteFriend(action.userA, action.userB);
    }

    @POST
    @Path("/add-blacklist")
    public boolean addUserToBlacklist(@Valid BlockAction action) {

        if (action.blocker == action.blocked) {
            throw new BadRequestException("Cannot block yourself.");
        }

        return people.blockUser(action.blocker, action.blocked);
    }

    @GET
    @Path("{userA}/{userB}/is-blocked")
    public boolean checkIfBlocked(
            @PathParam("userA") @Min(1) long userA,
            @PathParam("userB") @Min(1) long userB) {

        if (userA == userB) {
            throw new BadRequestException("Users must be different.");
        }

        return people.checkIfBlocked(userA, userB);
    }

    // ===== DTOs with validation =====

    public static class CreateUserRequest {

        @Min(1)
        public long userId;

        @NotBlank
        public String name;

        @Min(1)
        public long facultyNumber;
    }

    public static class UpdateNameRequest {

        @NotBlank
        public String newName;
    }

    public static class UserAction {

        @Min(1)
        public long userA;

        @Min(1)
        public long userB;
    }

    public static class BlockAction {

        @Min(1)
        public long blocker;

        @Min(1)
        public long blocked;
    }
}