package com.bookyrself.bookyrself.presenters

import android.content.Context
import android.util.Log
import com.bookyrself.bookyrself.data.events.EventsRepository
import com.bookyrself.bookyrself.data.profile.ProfileRepo
import com.bookyrself.bookyrself.data.serverModels.EventDetail.EventDetail
import com.bookyrself.bookyrself.data.serverModels.User.User
import com.bookyrself.bookyrself.services.FirebaseService
import com.bookyrself.bookyrself.views.activities.MainActivity
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class ProfileFragmentPresenter
/**
 * Construction
 */
(private val listener: ProfilePresenterListener, context: Context) : BasePresenter {
    private val profileRepo: ProfileRepo = MainActivity.profileRepo
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var userId: String? = null

    /**
     * Methods
     */
    fun updateUser(user: User, userId: String) {
        compositeDisposable.add(
                FirebaseService.instance.updateUser(user, userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { listener.profileInfoReady(userId, user) },
                                { throwable -> throwable.message?.let { listener.presentError(it) } }
                        ))
    }

    private fun loadProfile() {
        compositeDisposable.add(
                profileRepo.getProfileInfo(userId!!).subscribe(
                        { user ->
                            // Notify view the profile is ready
                            listener.profileInfoReady(userId, user)
                        },
                        { throwable ->
                            if (throwable is NoSuchElementException) {
                                listener.presentError("We were unable to find your profile")
                            } else {
                                throwable.message?.let { listener.presentError(it) }
                            }
                        }))
    }

    private fun loadEventDetails() {

    }

    override fun subscribe() {
        if (FirebaseAuth.getInstance().uid != null) {
            when (isNewSignUp()) {
                true -> {
                    // Don't do anything, we've already called createUser in onActivityResult in Fragment
                    listener.showCreatingUserLoadingToast()
                }
                false -> {
                    // load the profile
                    userId = FirebaseAuth.getInstance().uid
                    loadProfile()
                    loadEventDetails()
                }
            }
        } else {
            // No uid in Firebase Auth, user must be signed out
            listener.showSignedOutEmptyState()
        }
    }

    override fun unsubscribe() {
        compositeDisposable.clear()
    }


    fun markDateAsUnavailable(userId: String, date: String) {
        FirebaseService.instance.setDateUnavailableForUser(true, userId, date)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
    }

    // This shit sucks because it can be off by 1 milli
    fun isNewSignUp(): Boolean {
        val metadata = FirebaseAuth.getInstance().currentUser!!.metadata
        return metadata!!.creationTimestamp == metadata.lastSignInTimestamp
    }

    /**
     * PresenterListener Definition
     */
    interface ProfilePresenterListener : BasePresenterListener {
        fun profileInfoReady(userId: String?, user: User)
        fun eventReady(eventId: String, event: EventDetail)
        fun presentError(error: String)
        fun showCreatingUserLoadingToast()
    }
}
