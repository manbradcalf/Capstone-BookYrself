package com.bookyrself.bookyrself.data.Events;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;

import com.bookyrself.bookyrself.data.ResponseModels.EventDetail.EventDetail;
import com.bookyrself.bookyrself.data.ResponseModels.User.EventInviteInfo;
import com.bookyrself.bookyrself.services.FirebaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Response;

public class EventsRepo implements EventDataSource {

    private HashMap<String, EventDetail> eventsWithPendingInvites;
    private HashMap<String, EventDetail> allUsersEvents;
    private boolean cacheIsDirty;
    private DatabaseReference db;


    public EventsRepo() {
        this.eventsWithPendingInvites = new HashMap<>();
        this.allUsersEvents = new HashMap<>();
        this.cacheIsDirty = true;
        if (FirebaseAuth.getInstance().getUid() != null) {
            this.db = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(FirebaseAuth.getInstance().getUid())
                    .child("events");


            this.db.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    cacheIsDirty = true;
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    cacheIsDirty = true;
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                    cacheIsDirty = true;
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    cacheIsDirty = true;
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    cacheIsDirty = true;
                }
            });

        }
    }

    @Override
    public Flowable<Pair<String, EventDetail>> getAllEvents(String userId) {
        if (cacheIsDirty) {
            cacheIsDirty = false;
            return FirebaseService.getAPI()
                    .getUsersEventInvites(userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .flatMapIterable(HashMap::entrySet)
                    .flatMap(eventInvite -> FirebaseService.getAPI()
                            .getEventData(eventInvite.getKey())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .map(eventDetail -> {
                                allUsersEvents.put(eventInvite.getKey(), eventDetail);
                                return new Pair<>(eventInvite.getKey(), eventDetail);
                            }));
        } else if (allUsersEvents == null) {

            throw new NoSuchElementException();

        } else {

            return Flowable.fromIterable(allUsersEvents.entrySet())
                    .map(stringEventDetailEntry ->
                            new Pair<>(stringEventDetailEntry.getKey(), stringEventDetailEntry.getValue()));
        }
    }

    @Override
    public Flowable<Pair<String, EventDetail>> getEventsWithPendingInvites(String userId) {

        if (cacheIsDirty) {
            cacheIsDirty = false;
            return FirebaseService.getAPI()
                    .getUsersEventInvites(userId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(HashMap::entrySet)

                    // Filter out the invites so only pending invites remain
                    .map(entries -> {
                        HashMap<String, EventInviteInfo> pendingInvites = new HashMap<>();
                        for (Map.Entry<String, EventInviteInfo> entry : entries) {
                            if (isInvitePendingResponse(entry)) {
                                pendingInvites.put(entry.getKey(), entry.getValue());
                            }
                        }
                        if (pendingInvites.isEmpty()) {
                            throw new NoSuchElementException();
                        } else {
                            return pendingInvites;
                        }
                    })

                    // Now check if the list of pending invites is empty
                    .firstOrError()
                    .toFlowable()
                    .flatMapIterable(HashMap::entrySet)

                    // Fetch event info for each pending invite
                    .flatMap(eventInvite -> FirebaseService.getAPI()
                            .getEventData(eventInvite.getKey())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .map(eventDetail -> {
                                eventsWithPendingInvites.put(eventInvite.getKey(), eventDetail);
                                return new Pair<>(eventInvite.getKey(), eventDetail);
                            }));

        } else if (eventsWithPendingInvites == null) {

            throw new NoSuchElementException();

        } else {

            return Flowable.fromIterable(eventsWithPendingInvites.entrySet())
                    .map(stringEventDetailEntry ->
                            new Pair<>(stringEventDetailEntry.getKey(), stringEventDetailEntry.getValue()));

        }
    }

    @Override
    public Flowable<Boolean> acceptEventInvite(String userId, String eventId) {
        return FirebaseService.getAPI()
                .acceptInvite(true, userId, eventId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(aBoolean ->
                        FirebaseService.getAPI()
                                .setEventUserAsAttending(true, userId, eventId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread()))
                .doOnNext(aBoolean -> {
                    // Remove from pending invitations cache
                    eventsWithPendingInvites.remove(eventId);
                });
    }

    @Override
    public Flowable<Response<Void>> rejectEventInvite(String userId, String eventId) {
        return FirebaseService.getAPI().
                rejectInvite(true, userId, eventId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(aBoolean ->
                        FirebaseService.getAPI()
                                .setEventUserAsAttending(false, userId, eventId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread()))
                .flatMap(aBoolean ->
                        FirebaseService.getAPI()
                                .removeUserFromEvent(eventId, userId)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .doOnNext(voidResponse -> {
                                    allUsersEvents.remove(eventId);
                                    eventsWithPendingInvites.remove(eventId);
                                }));
    }

    private Boolean isInvitePendingResponse(Map.Entry<String, EventInviteInfo> eventInvite) {

        if (eventInvite != null) {
            // All fields are false by default, so if all fields remain false,
            // It means the user hasn't interacted at all with the invite,
            // hence the invite is pending
            return !eventInvite.getValue().getIsInviteAccepted()
                    && !eventInvite.getValue().getIsInviteRejected()
                    && !eventInvite.getValue().getIsHost();
        } else {
            return false;
        }
    }
}