package org.tuvarna.resource;


import jakarta.inject.Inject;
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

    private static final int PAGE_SIZE = 30;

    @Inject
    People people;

    @GET
    @Path("{userId}/friends/{page}")
    public List<PersonDto> getFriendsOfPerson(
            @PathParam("userId") long userId,
            @PathParam("page") int page) {
        int skip = page * PAGE_SIZE;

        return people.getFriendsForUser(userId, skip, PAGE_SIZE);
    }

    @GET
    @Path("{userA}/{userB}/common-friends/{page}")
    public List<PersonDto> getCommonABFriends(
            @PathParam("userA") long userA,
            @PathParam("userB") long userB,
            @PathParam("page") int page) {
        int skip = page * PAGE_SIZE;

        return people.getCommonFriends(userA, userB, skip, PAGE_SIZE);
    }

    @GET
    @Path("{userId}/incoming-requests/{page}")
    public List<FriendRequestDto> getIncomingRequests(
            @PathParam("userId") long userId,
            @PathParam("page") int page) {
        int skip = page * PAGE_SIZE;

        return people.getIncomingRequests(userId, skip, PAGE_SIZE);
    }

    @GET
    @Path("{userId}/outgoing-requests/{page}")
    public List<FriendRequestDto> getOutgoingRequests(
            @PathParam("userId") long userId,
            @PathParam("page") int page) {
        int skip = page * PAGE_SIZE;

        return people.getOutgoingRequests(userId, skip, PAGE_SIZE);
    }

    @GET
    @Path("{userId}/blocked/{page}")
    public List<PersonDto> getBlockedUsers(
            @PathParam("userId") long userId,
            @PathParam("page") int page) {
        int skip = page * PAGE_SIZE;

        return people.getBlockedUsersPerUser(userId, skip, PAGE_SIZE);
    }

    @POST
    @Path("/send-request")
    public boolean sendFriendRequest(UserAction action) {
        if (action.userA == action.userB) {
            throw new BadRequestException("Cannot send request to self.");
        }
        return people.createFriendshipRequest(action.userA, action.userB);
    }

    @POST
    @Path("/remove-request")
    public boolean removeFriendRequest(UserAction action) {
        return people.deleteRequest(action.userA, action.userB);
    }

    @POST
    @Path("/delete-friend")
    public boolean deleteFriend(UserAction action) {
        return people.deleteFriend(action.userA, action.userB);
    }

    @POST
    @Path("/add-blacklist")
    public boolean addUserToBlacklist(BlockAction action) {
        if (action.blocker == action.blocked) {
            throw new BadRequestException("Cannot block self.");
        }
        return people.blockUser(action.blocker, action.blocked);
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
