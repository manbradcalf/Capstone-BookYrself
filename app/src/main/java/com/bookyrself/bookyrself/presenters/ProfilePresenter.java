package com.bookyrself.bookyrself.presenters;

import com.bookyrself.bookyrself.models.SearchResponseUsers._source;
import com.bookyrself.bookyrself.services.FirebaseService;
import com.bookyrself.bookyrself.models.User.*;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfilePresenter {
    private final ProfilePresenterListener listener;
    private final FirebaseService service;

    /**
     * Contract / Listener
     */
    public interface ProfilePresenterListener {
        void checkAuth();

        void profileInfoReady(_source response);

        void presentToast(String message);

        void loadingState();

        void successfulAuth();
    }

    /**
     * Construction
     */
    public ProfilePresenter(ProfilePresenterListener listener) {
        this.listener = listener;
        this.service = new FirebaseService();
    }

    /**
     * Methods
     */
    public void createUser(User user, String UID) {
        service.getAPI().addUser(user, UID).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                // response
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                // failure
            }
        });
    }

    public void uploadPhoto() {

    }

    public void updateCityState() {

    }

    public void updateTags() {

    }

    public void updateWebsite() {

    }

    public void viewContacts() {

    }

    public void updateBio() {

    }


}