package com.bookyrself.bookyrself.interactors;

import android.util.Log;

import com.bookyrself.bookyrself.models.SerializedModels.User.User;
import com.bookyrself.bookyrself.services.FirebaseService;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactsInteractor {

    private final ContactsInteractorListener listener;
    private final FirebaseService service;

    public ContactsInteractor(ContactsInteractorListener listener) {
        this.service = new FirebaseService();
        this.listener = listener;
    }

    /**
     * Methods
     */
    public void getContactIds(String userId) {
        service.getAPI().getUserContacts(userId).enqueue(new Callback<HashMap<String, Boolean>>() {
            @Override
            public void onResponse(Call<HashMap<String, Boolean>> call, Response<HashMap<String, Boolean>> response) {
                listener.contactsReturned(response.body());
            }

            @Override
            public void onFailure(Call<HashMap<String, Boolean>> call, Throwable t) {
                listener.presentError(t.getMessage());
                Log.e("getContactIds: ", t.getMessage());
            }
        });
    }

    public void getUsersAsContacts(final List<String> userIds) {
        if (userIds != null) {
            for (int user = 0; user < userIds.size(); user++) {
                final int position = user;
                service.getAPI().getUserDetails(userIds.get(user)).enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.body() != null) {
                            listener.userReturned(userIds.get(position), response.body());
                        } else {
                            Log.e("ContactsInteractor", "User with userId " + userIds.get(position) + "not found");
                        }
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Log.e("ContactsInteractor", t.getMessage());
                    }
                });
            }
        } else {
            listener.noUsersReturned();
        }
    }

    public interface ContactsInteractorListener {

        void contactsReturned(HashMap<String, Boolean> contacts);

        void userReturned(String id, User user);

        void noUsersReturned();

        void presentError(String error);
    }
}
