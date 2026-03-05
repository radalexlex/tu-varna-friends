package org.tuvarna.resource;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
    public boolean createUser(CreateUserRequest request) {

        if (request == null
                || request.name == null
                || request.name.isBlank()
                || request.facultyNumber == 0) {
            throw new BadRequestException("Invalid user data.");
        }

        try {
            return people.createUser(request.userId, request.name, request.facultyNumber);
        } catch (Exception e) {
            throw new WebApplicationException(
                    "Failed to create user.",
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    @PUT
    @Path("/{userId}/name")
    public boolean updateName(@PathParam("userId") long userId,
                              UpdateNameRequest request) {

        if (request == null || request.newName == null || request.newName.isBlank()) {
            throw new BadRequestException("Invalid name.");
        }

        try {
            return people.updateName(userId, request.newName);
        } catch (Exception e) {
            throw new WebApplicationException(
                    "Failed to update name.",
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GET
    @Path("{userId}/search/{page}")
    public List<PersonDto> searchPeople(
            @PathParam("userId") long userId,
            @PathParam("page") int page,
            @QueryParam("query") String query) {

        if (page < 0) {
            throw new BadRequestException("Page index cannot be negative.");
        }

        if (query == null || query.isBlank()) {
            throw new BadRequestException("Query must not be empty.");
        }

        int skip = page * PAGE_SIZE;

        try {
            return people.searchPeople(userId, query, skip, PAGE_SIZE);
        } catch (Exception e) {
            throw new WebApplicationException(
                    "Failed to search users.",
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GET
    @Path("{userId}/friends/{page}")
    public List<PersonDto> getFriendsOfPerson(
            @PathParam("userId") long userId,
            @PathParam("page") int page) {

        if (page < 0) {
            throw new BadRequestException("Page index cannot be negative.");
        }

        int skip = page * PAGE_SIZE;

        try {
            return people.getFriendsForUser(userId, skip, PAGE_SIZE);
        } catch (Exception e) {
            throw new WebApplicationException(
                    "Failed to fetch friends.",
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GET
    @Path("{userId}/friends-all")
    public List<PersonDto> getAllFriendsOfPerson(
            @PathParam("userId") long userId) {

        try {
            return people.getAllFriendsForUser(userId);
        } catch (Exception e) {
            throw new WebApplicationException(
                    "Failed to fetch all friends.",
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GET
    @Path("{userA}/{userB}/common-friends/{page}")
    public List<PersonDto> getCommonABFriends(
            @PathParam("userA") long userA,
            @PathParam("userB") long userB,
            @PathParam("page") int page) {

        if (page < 0) {
            throw new BadRequestException("Page index cannot be negative.");
        }

        int skip = page * PAGE_SIZE;

        try {
            return people.getCommonFriends(userA, userB, skip, PAGE_SIZE);
        } catch (Exception e) {
            throw new WebApplicationException(
                    "Failed to fetch common friends.",
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GET
    @Path("{userId}/incoming-requests/{page}")
    public List<FriendRequestDto> getIncomingRequests(
            @PathParam("userId") long userId,
            @PathParam("page") int page) {

        if (page < 0) {
            throw new BadRequestException("Page index cannot be negative.");
        }

        int skip = page * PAGE_SIZE;

        try {
            return people.getIncomingRequests(userId, skip, PAGE_SIZE);
        } catch (Exception e) {
            throw new WebApplicationException(
                    "Failed to fetch incoming requests.",
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GET
    @Path("{userId}/outgoing-requests/{page}")
    public List<FriendRequestDto> getOutgoingRequests(
            @PathParam("userId") long userId,
            @PathParam("page") int page) {

        if (page < 0) {
            throw new BadRequestException("Page index cannot be negative.");
        }

        int skip = page * PAGE_SIZE;

        try {
            return people.getOutgoingRequests(userId, skip, PAGE_SIZE);
        } catch (Exception e) {
            throw new WebApplicationException(
                    "Failed to fetch outgoing requests.",
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GET
    @Path("{userId}/blocked/{page}")
    public List<PersonDto> getBlockedUsers(
            @PathParam("userId") long userId,
            @PathParam("page") int page) {

        if (page < 0) {
            throw new BadRequestException("Page index cannot be negative.");
        }

        int skip = page * PAGE_SIZE;

        try {
            return people.getBlockedUsersPerUser(userId, skip, PAGE_SIZE);
        } catch (Exception e) {
            throw new WebApplicationException(
                    "Failed to fetch blocked users.",
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    @POST
    @Path("/send-request")
    public boolean sendFriendRequest(UserAction action) {

        if (action.userA == action.userB) {
            throw new BadRequestException("Cannot send friend request to self.");
        }

        try {
            return people.createFriendshipRequest(action.userA, action.userB);
        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new WebApplicationException(
                    "Failed to send friend request.",
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    @POST
    @Path("/remove-request")
    public boolean removeFriendRequest(UserAction action) {

        try {
            return people.deleteRequest(action.userA, action.userB);
        } catch (Exception e) {
            throw new WebApplicationException(
                    "Failed to remove friend request.",
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    @POST
    @Path("/delete-friend")
    public boolean deleteFriend(UserAction action) {

        try {
            return people.deleteFriend(action.userA, action.userB);
        } catch (Exception e) {
            throw new WebApplicationException(
                    "Failed to delete friend.",
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    @POST
    @Path("/add-blacklist")
    public boolean addUserToBlacklist(BlockAction action) {

        if (action.blocker == action.blocked) {
            throw new BadRequestException("Cannot block yourself.");
        }

        try {
            return people.blockUser(action.blocker, action.blocked);
        } catch (Exception e) {
            throw new WebApplicationException(
                    "Failed to block user.",
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GET
    @Path("{userA}/{userB}/is-blocked")
    public boolean checkIfBlocked(
            @PathParam("userA") long userA,
            @PathParam("userB") long userB) {

        if (userA == userB) {
            throw new BadRequestException("Users must be different.");
        }

        try {
            return people.checkIfBlocked(userA, userB);
        } catch (Exception e) {
            throw new WebApplicationException(
                    "Failed to check block status.",
                    Response.Status.INTERNAL_SERVER_ERROR
            );
        }
    }

    public static class CreateUserRequest {
        public long userId;
        public String name;
        public long facultyNumber;
    }

    public static class UpdateNameRequest {
        public String newName;
    }

    public static class UserAction {
        public long userA;
        public long userB;
    }

    public static class BlockAction {
        public long blocker;
        public long blocked;
    }

}
