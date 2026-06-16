package org.tuvarna.resource;


import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.tuvarna.model.dto.FriendRequestDto;
import org.tuvarna.model.dto.ContactHydratedDto;
import org.tuvarna.model.dto.ProfileDto;
import org.tuvarna.repository.People;

import java.util.List;

@Path("/people")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PersonResource {

    private static final int PAGE_SIZE = 15;

    @Inject
    People service;

    private int skip(int page) {
        return page * PAGE_SIZE;
    }

    // ---------------- PROFILE ----------------

    @POST
    public Response saveOrUpdate(ProfileDto dto) {
        service.saveOrUpdateUser(dto);
        return Response.ok().build();
    }

    // ---------------- SEARCH ----------------

    @GET
    @Path("/{userId}/search")
    public List<ContactHydratedDto> search(
            @PathParam("userId") @Min(1) long userId,
            @QueryParam("q") @NotBlank String query,
            @QueryParam("page") @DefaultValue("0") int page) {

        return service.searchPeople(userId, query.trim(), skip(page), PAGE_SIZE);
    }

    // ---------------- FRIENDS ----------------

    @GET
    @Path("/{userId}/friends")
    public List<PersonSummaryDto> friends(
            @PathParam("userId") long userId,
            @QueryParam("page") @DefaultValue("0") int page) {

        return service.getFriends(userId, skip(page), PAGE_SIZE);
    }

    @GET
    @Path("/{userId}/friends/all")
    public List<PersonSummaryDto> allFriends(@PathParam("userId") long userId) {
        return service.getAllFriends(userId);
    }

    @GET
    @Path("/{a}/{b}/common-friends")
    public List<PersonSummaryDto> commonFriends(
            @PathParam("a") long a,
            @PathParam("b") long b,
            @QueryParam("page") @DefaultValue("0") int page) {

        return service.getCommonFriends(a, b, skip(page), PAGE_SIZE);
    }

    // ---------------- REQUESTS ----------------

    @GET
    @Path("/{userId}/requests/incoming")
    public List<FriendRequestDto> incoming(
            @PathParam("userId") long userId,
            @QueryParam("page") int page) {

        return service.getIncomingRequests(userId, skip(page), PAGE_SIZE);
    }

    @GET
    @Path("/{userId}/requests/outgoing")
    public List<FriendRequestDto> outgoing(
            @PathParam("userId") long userId,
            @QueryParam("page") int page) {

        return service.getOutgoingRequests(userId, skip(page), PAGE_SIZE);
    }

    @POST
    @Path("/requests")
    public Response sendRequest(@Valid UserAction action) {
        service.sendFriendRequest(action);
        return Response.ok().build();
    }

    @DELETE
    @Path("/requests")
    public Response removeRequest(@Valid UserAction action) {
        service.removeRequest(action);
        return Response.noContent().build();
    }

    // ---------------- FRIENDSHIP ----------------

    @DELETE
    @Path("/friends")
    public Response deleteFriend(@Valid UserAction action) {
        service.deleteFriend(action);
        return Response.noContent().build();
    }

    // ---------------- BLOCK ----------------

    @POST
    @Path("/blocks")
    public Response block(@Valid BlockAction action) {
        service.block(action);
        return Response.ok().build();
    }

    @GET
    @Path("/{a}/{b}/blocked")
    public boolean isBlocked(@PathParam("a") long a,
                             @PathParam("b") long b) {
        return service.isBlocked(a, b);
    }

    public record UserAction(
            @Min(1) long userA,
            @Min(1) long userB
    ) {}

    public record BlockAction(
            @Min(1) long blocker,
            @Min(1) long blocked
    ) {}
}