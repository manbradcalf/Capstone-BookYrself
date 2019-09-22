package com.bookyrself.bookyrself.views

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

import com.bookyrself.bookyrself.R
import com.bookyrself.bookyrself.data.Contacts.ContactsRepository
import com.bookyrself.bookyrself.data.Events.EventsRepository
import com.bookyrself.bookyrself.data.Profile.ProfileRepo
import com.bookyrself.bookyrself.utils.FragmentViewPager
import com.bookyrself.bookyrself.utils.FragmentViewPagerAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    internal val profileFragment = ProfileFragment()
    internal val searchFragment = SearchFragment()
    internal val eventsFragment = EventsFragment()
    internal val contactsFragment = ContactsFragment()
    internal val eventInvitesFragment = EventInvitesFragment()

    val db = FirebaseDatabase.getInstance("https://bookyrself-staging.firebaseio.com/")
    val firebaseApp = FirebaseApp.initializeApp(this)
    lateinit var adapter: FragmentViewPagerAdapter
    lateinit var viewPager: FragmentViewPager
    lateinit var navigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.enterTransition = null
        setContentView(R.layout.activity_main)
        navigationView = findViewById(R.id.navigation)
        navigationView.setOnNavigationItemSelectedListener(this)
        buildFragmentsList()
    }

    // Remove inter-activity transition to avoid screen tossing on tapping bottom navigation items
    public override fun onPause() {
        super.onPause()
        overridePendingTransition(0, 0)
        window.exitTransition = null
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.navigation_search) {
            viewPager.currentItem = SEARCH_FRAGMENT_INDEX
        } else if (itemId == R.id.navigation_calendar) {
            viewPager.currentItem = CALENDAR_FRAGMENT_INDEX
        } else if (itemId == R.id.navigation_contacts) {
            viewPager.currentItem = CONTACTS_FRAGMENT_INDEX
        } else if (itemId == R.id.navigation_profile) {
            viewPager.currentItem = PROFILE_FRAGMENT_INDEX
        } else if (itemId == R.id.navigation_event_invites_list) {
            viewPager.currentItem = EVENTS_INVITE_LIST
        }
        return true
    }

    private fun buildFragmentsList() {
        viewPager = findViewById(R.id.view_pager)
        adapter = FragmentViewPagerAdapter(this.supportFragmentManager)
        adapter.addFragment(searchFragment, "Search")
        adapter.addFragment(eventsFragment, "Calendar")
        adapter.addFragment(eventInvitesFragment, "Event Invites")
        adapter.addFragment(contactsFragment, "Contacts")
        adapter.addFragment(profileFragment, "Profile")
        viewPager.adapter = adapter
    }

    companion object {

        private const val SEARCH_FRAGMENT_INDEX = 0
        private const val CALENDAR_FRAGMENT_INDEX = 1
        private const val EVENTS_INVITE_LIST = 2
        private const val CONTACTS_FRAGMENT_INDEX = 3
        private const val PROFILE_FRAGMENT_INDEX = 4

        private var EVENT_INVITES_REPO: EventsRepository? = null
        private var CONTACTS_REPO: ContactsRepository? = null
        private var PROFILE_REPO: ProfileRepo? = null

        //TODO: Fix all these !! and find a better way to serve up these repos
        val contactsRepo: ContactsRepository
            get() {
                if (CONTACTS_REPO == null) {
                    CONTACTS_REPO = ContactsRepository()
                }
                return CONTACTS_REPO!!
            }

        val profileRepo: ProfileRepo
            get() {
                if (PROFILE_REPO == null) {
                    PROFILE_REPO = ProfileRepo()
                }
                return PROFILE_REPO!!
            }

        fun getEventsRepo(context: Context): EventsRepository {
            if (EVENT_INVITES_REPO == null) {
                EVENT_INVITES_REPO = EventsRepository(context)
            }
            return EVENT_INVITES_REPO!!
        }
    }
}