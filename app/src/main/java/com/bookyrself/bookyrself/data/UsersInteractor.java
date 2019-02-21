package com.bookyrself.bookyrself.data;

import android.support.annotation.NonNull;
import android.util.Log;

import com.bookyrself.bookyrself.models.SerializedModels.User.EventInviteInfo;
import com.bookyrself.bookyrself.models.SerializedModels.User.User;
import com.bookyrself.bookyrself.presenters.BasePresenter;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UsersInteractor {
    private UsersInteractorListener usersInteractorListener;
    private UserDetailInteractorListener userDetailInteractorListener;
    private UsersEventsInteractorListener usersEventsInteractorListener;

    /**
     * Constructors
     */
    public UsersInteractor(UsersInteractorListener listener) {
        this.usersInteractorListener = listener;
    }

    public UsersInteractor(UserDetailInteractorListener listener) {
        this.usersInteractorListener = listener;
        this.userDetailInteractorListener = listener;
    }

    public UsersInteractor(UsersEventsInteractorListener listener) {
        this.usersEventsInteractorListener = listener;
    }

    public void addEventToUser(EventInviteInfo eventInviteInfo, final String userId, final String eventId) {
        FirebaseService.getAPI().addEventToUser(eventInviteInfo, userId, eventId).enqueue(new Callback<EventInviteInfo>() {
            @Override
            public void onResponse(Call<EventInviteInfo> call, Response<EventInviteInfo> response) {
                Log.i("UsersInteractor", String.format("User %s successfully invited to Event %s", userId, eventId));
            }

            @Override
            public void onFailure(Call<EventInviteInfo> call, Throwable t) {
                usersInteractorListener.presentError(t.getMessage());
            }
        });
    }

    public Flowable<User> getUserDetails(final String userId) {
        return FirebaseService.getAPI()
                .getUserDetails(userId)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }

    public void createUser(User user, final String uid) {
        FirebaseService.getAPI().addUser(user, uid).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // response
                usersInteractorListener.userDetailReturned(response.body(), uid);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // failure
                usersInteractorListener.presentError(t.getMessage());
            }
        });
    }

    public void patchUser(User user, final String uID) {
        FirebaseService.getAPI().patchUser(user, uID).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // response
                usersInteractorListener.userDetailReturned(response.body(), uID);
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // failure
                usersInteractorListener.presentError(t.getMessage());
            }
        });
    }

    public void getUserEvents(String userId) {
        FirebaseService.getAPI().getUsersEventInfo(userId).enqueue(new Callback<HashMap<String, EventInviteInfo>>() {
            @Override
            public void onResponse(@NonNull Call<HashMap<String, EventInviteInfo>> call, @NonNull Response<HashMap<String, EventInviteInfo>> response) {
                if (response.body() != null) {
                    usersEventsInteractorListener.eventsReturned(new ArrayList<>(response.body().keySet()));
                } else {
                    usersEventsInteractorListener.noEventsReturned();
                }

            }

            @Override
            public void onFailure(Call<HashMap<String, EventInviteInfo>> call, Throwable t) {
                Log.e("events presenter:", t.getMessage());
                usersEventsInteractorListener.presentError(t.getMessage());
            }
        });
    }

    public void addContactToUser(String contactId, String userId) {
        HashMap<String, Boolean> request = new HashMap<>();
        request.put(contactId, true);
        FirebaseService.getAPI().addContactToUser(request, userId).enqueue(new Callback<HashMap<String, Boolean>>() {
            @Override
            public void onResponse(Call<HashMap<String, Boolean>> call, Response<HashMap<String, Boolean>> response) {
                userDetailInteractorListener.contactSuccessfullyAdded();
            }

            @Override
            public void onFailure(Call<HashMap<String, Boolean>> call, Throwable t) {
                userDetailInteractorListener.presentError(t.getMessage());
            }
        });
    }


    /**
     * Interfaces
     */
    public interface UsersInteractorListener {

        void userDetailReturned(User user, String userId);

        void presentError(String error);

    }

    public interface UserDetailInteractorListener extends UsersInteractorListener, BasePresenter {

        void contactSuccessfullyAdded();
    }

    public interface UsersEventInvitesInteractorListener {

        void eventIdsOfEventsWithPendingInvitesReturned(List<String> eventIds);

        void presentError(String error);

        void noInvitesReturnedForUser();

    }

    public interface UsersEventsInteractorListener {
        void eventsReturned(List<String> eventIds);

        void presentError(String error);

        void noEventsReturned();
    }
}