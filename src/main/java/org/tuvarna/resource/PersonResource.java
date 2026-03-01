package org.tuvarna.resource;


import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.tuvarna.repository.People;

@Path("/people")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonResource {

    private static final int PAGE_SIZE = 15;

    @Inject
    People people;

    @GET
    @Path("{userId}/friends/{page}")
    public Response getFriendsOfPerson(
            @PathParam("userId") long userId,
            @PathParam("page") int page) {

        if (page < 0) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Page index cannot be negative.")
                    .build();
        }

        int skip = page * PAGE_SIZE;

        try {
            return Response.ok(
                    people.getFriendsForUser(userId, skip, PAGE_SIZE)
            ).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("{userA}/{userB}/common-friends/{page}")
    public Response getCommonABFriends(
            @PathParam("userA") long userA,
            @PathParam("userB") long userB,
            @PathParam("page") int page) {

        if (page < 0) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        int skip = page * PAGE_SIZE;

        try {
            return Response.ok(
                    people.getCommonFriends(userA, userB, skip, PAGE_SIZE)
            ).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("{userId}/incoming-requests/{page}")
    public Response getIncomingRequests(
            @PathParam("userId") long userId,
            @PathParam("page") int page) {

        if (page < 0) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        int skip = page * PAGE_SIZE;

        try {
            return Response.ok(
                    people.getIncomingRequests(userId, skip, PAGE_SIZE)
            ).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("{userId}/outgoing-requests/{page}")
    public Response getOutgoingRequests(
            @PathParam("userId") long userId,
            @PathParam("page") int page) {

        if (page < 0) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        int skip = page * PAGE_SIZE;

        try {
            return Response.ok(
                    people.getOutgoingRequests(userId, skip, PAGE_SIZE)
            ).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("{userId}/blocked/{page}")
    public Response getBlockedUsers(
            @PathParam("userId") long userId,
            @PathParam("page") int page) {

        if (page < 0) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        int skip = page * PAGE_SIZE;

        try {
            return Response.ok(
                    people.getBlockedUsersPerUser(userId, skip, PAGE_SIZE)
            ).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @POST
    @Path("/send-request")
    public Response sendFriendRequest(UserAction action) {

        if (action.userA == action.userB) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Cannot send friend request to self.")
                    .build();
        }

        try {
            return Response.ok(people.createFriendshipRequest(action.userA, action.userB)).build();
        } catch (BadRequestException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @POST
    @Path("/remove-request")
    public Response removeFriendRequest(UserAction action) {
        try {
            return Response.ok(people.deleteRequest(action.userA, action.userB)).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @POST
    @Path("/delete-friend")
    public Response deleteFriend(UserAction action) {
        try {
            return Response.ok(people.deleteFriend(action.userA, action.userB)).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @POST
    @Path("/add-blacklist")
    public Response addUserToBlacklist(BlockAction action) {

        if (action.blocker == action.blocked) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Cannot block yourself.")
                    .build();
        }

        try {
            return Response.ok(people.blockUser(action.blocker, action.blocked)).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Path("{userA}/{userB}/is-blocked")
    public Response checkIfBlocked(
            @PathParam("userA") long userA,
            @PathParam("userB") long userB) {

        if (userA == userB) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Users must be different.")
                    .build();
        }

        try {
            return Response.ok(
                    people.checkIfBlocked(userA, userB)
            ).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
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
