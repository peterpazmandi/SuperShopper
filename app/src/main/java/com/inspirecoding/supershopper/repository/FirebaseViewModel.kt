package com.inspirecoding.supershopper.repository

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentChange
import com.inspirecoding.supershopper.MyApp
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.enums.FriendshipStatus
import com.inspirecoding.supershopper.fragments.*
import com.inspirecoding.supershopper.model.User
import com.inspirecoding.supershopper.repository.authentication.AuthRepository
import com.inspirecoding.supershopper.repository.firestore.FirestoreRepository
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import com.inspirecoding.supershopper.utilities.Result
import com.inspirecoding.supershopper.fragments.LoginFragment
import com.inspirecoding.supershopper.model.Friend
import com.inspirecoding.supershopper.model.FriendRequest
import com.inspirecoding.supershopper.model.ShoppingList
import kotlinx.coroutines.delay

private const val TAG = "FirebaseViewModel"
class FirebaseViewModel(val authRepository: AuthRepository, val firestoreRepository: FirestoreRepository): ViewModel()
{
    private var callbackManager: CallbackManager? = null

    private lateinit var googleSingInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1


    private val _toast = MutableLiveData<Pair<Boolean, String>>()
    val toast: LiveData<Pair<Boolean, String>>
        get() = _toast

    private val _spinner = MutableLiveData<Boolean>(false)
    val spinner: LiveData<Boolean>
        get() = _spinner

    private val _securityCheck = MutableLiveData<Pair<Boolean, String>>()
    val securityCheck: LiveData<Pair<Boolean, String>>
        get() = _securityCheck

    private val _currentUserMLD = MutableLiveData<User>(User())
    val currentUserLD: LiveData<User>
        get() = _currentUserMLD

    private val _usersListMLD = MutableLiveData<List<User>>()
    val usersListLD: LiveData<List<User>>
        get() = _usersListMLD


    //Email
    fun registerUserFromAuthWithEmailAndPassword(name: String, email: String, password: String, fragment: Fragment)
    {
        _spinner.value = true
        viewModelScope.launch {
            fragment.context?.let {context ->
                when(val result = authRepository.registerUserFromAuthWithEmailAndPassword(email, password, context.applicationContext))
                {
                    is Result.Success -> {
                        Log.e(TAG, "Result.Success")
                        result.data?.let {firebaseUser ->
                            createUserInFirestore(createUserObject(firebaseUser, name), fragment)

                            _spinner.value = false
                            setToastMessage(MyApp.applicationContext().getString(R.string.registration_successful))
                        }
                    }
                    is Result.Error -> {
                        Log.e(TAG, "${result.exception.message}")

                        _spinner.value = false
                        result.exception.message?.let { message ->
                            setToastMessage(message)
                        }
                    }
                    is Result.Canceled -> {
                        Log.e(TAG, "${result.exception!!.message}")

                        _spinner.value = false
                        setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))
                    }
                }
            }
        }
    }
    fun logInUserFromAuthWithEmailAndPassword(email: String, password: String, fragment: Fragment)
    {
        _spinner.value = true
        viewModelScope.launch {
            when (val result = authRepository.logInUserFromAuthWithEmailAndPassword(email, password))
            {
                is Result.Success -> {
                    Log.i(TAG, "SignIn - Result.Success - User: ${result.data}")
                    result.data?.let { firebaseUser ->
                        Log.i(TAG, "SignIn - Result.Success - User: ${result.data}")
                        getCurrentUserFromFirestore(firebaseUser.uid, fragment)
                    }
                }
                is Result.Error -> {
                    result.exception.message?.let { message ->
                        setToastMessage(message)
                    }

                    _spinner.value = false
                }
                is Result.Canceled -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))

                    _spinner.value = false
                }
            }
        }
    }
    fun sendPasswordResetEmail(email: String, fragment: Fragment)
    {
        viewModelScope.launch {
            when(val result = authRepository.sendPasswordResetEmail(email))
            {
                is Result.Success -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.check_your_emails_to_reset_your_password))
                    onToastMessageDone()
                }
                is Result.Error -> {
                    result.exception.message?.let {  message ->
                        setToastMessage(message)
                        onToastMessageDone()
                    }
                }
                is Result.Canceled -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))
                    onToastMessageDone()
                }
            }
        }
    }
    fun updatePassword(newPassword: String)
    {
        viewModelScope.launch {
            when(val result = authRepository.updatePassword(newPassword))
            {
                is Result.Success -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.your_password_has_been_updated))
                    onToastMessageDone()
                }
                is Result.Error -> {
                    result.exception.message?.let {  message ->
                        setToastMessage(message)
                        onToastMessageDone()
                    }
                }
                is Result.Canceled -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))
                    onToastMessageDone()
                }
            }
        }
    }
    fun updateEmail(email: String)
    {
        viewModelScope.launch {
            when(val result = authRepository.updateEmail(email))
            {
                is Result.Success -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.your_email_has_been_updated))
                    onToastMessageDone()
                }
                is Result.Error -> {
                    result.exception.message?.let {  message ->
                        setToastMessage(message)
                        onToastMessageDone()
                    }
                }
                is Result.Canceled -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))
                    onToastMessageDone()
                }
            }
        }
    }
    fun logOut()
    {
        viewModelScope.launch {
            authRepository.logOutUser()
            setToastMessage(MyApp.applicationContext().getString(R.string.you_have_successfully_logged_out))
        }
    }
    fun reAuthenticateUser(email: String, password: String, source: String)
    {
        _spinner.value = true
        viewModelScope.launch {
            when (val result = authRepository.reAuthenticateUser(email, password))
            {
                is Result.Success -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.the_security_check_was_successful))
                    securityCheckDone(source)

                    _spinner.value = false
                }
                is Result.Error -> {
                    result.exception.message?.let { message ->
                        setToastMessage(message)
                    }

                    _spinner.value = false
                }
                is Result.Canceled -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))

                    _spinner.value = false
                }
            }
        }
    }

    //Facebook
    fun signInWithFacebook(activity: Activity, fragment: Fragment)
    {
        _spinner.value = true

        callbackManager = CallbackManager.Factory.create()

        LoginManager.getInstance().logInWithReadPermissions(activity, Arrays.asList("email", "public_profile"))
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult>
        {
            override fun onSuccess(result: LoginResult?)
            {
                val credential = FacebookAuthProvider.getCredential(result?.accessToken?.token!!)
                viewModelScope.launch {
                    when(val result = authRepository.signInWithCredential(credential))
                    {
                        is Result.Success -> {
                            Log.d(TAG, "Result.Success - ${result.data?.user?.uid}")
                            result.data?.user?.let { user ->
                                val userExists = getUserFromFirestore(user.uid)
                                if(userExists == null)
                                {
                                    val _user = user.displayName?.let {
                                        createUserObject(user, it)
                                    }
                                    _user?.let {
                                        createUserInFirestore(_user, fragment)
                                    }
                                }
                                else
                                {
                                    _currentUserMLD.value = userExists
                                    navigateToMainFragment(fragment)
                                }
                            }
                        }
                        is Result.Error -> {
                            Log.e(TAG, "Result.Error - ${result.exception.message}")
                            result.exception.message?.let { message ->
                                setToastMessage(message)
                            }

                            _spinner.value = false
                        }
                        is Result.Canceled -> {
                            Log.d(TAG, "Result.Canceled")
                            setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))

                            _spinner.value = false
                        }
                    }
                }
            }

            override fun onCancel()
            {
                viewModelScope.launch {

                }
            }

            override fun onError(error: FacebookException?)
            {
                viewModelScope.launch {
                }
            }
        })
    }

    //Google
    fun signInWithGoogle(activity: Activity)
    {
        _spinner.value = true
        val googleSignInOptions: GoogleSignInOptions = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSingInClient = GoogleSignIn.getClient(activity, googleSignInOptions)

        val intent = googleSingInClient.signInIntent
        activity.startActivityForResult(intent, RC_SIGN_IN)
    }
    private fun handleSignInResult (completedTask: Task<GoogleSignInAccount>, fragment: Fragment)
    {
        viewModelScope.launch {
            try
            {
                val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
                account?.let {
                    val credential: AuthCredential = GoogleAuthProvider.getCredential(account.idToken, null)
                    when(val result = authRepository.signInWithCredential(credential))
                    {
                        is Result.Success -> {
                            Log.d(TAG, "Result.Success - ${result.data?.user?.uid}")
                            result.data?.user?.let {user ->
                                val userExists = getUserFromFirestore(user.uid)
                                if(userExists == null)
                                {
                                    val _user = user.displayName?.let {
                                        createUserObject(user, it)
                                    }
                                    _user?.let {
                                        createUserInFirestore(_user, fragment)
                                    }
                                }
                                else
                                {
                                    _currentUserMLD.value = userExists
                                    navigateToMainFragment(fragment)
                                }
                            }

                            _spinner.value = false
                        }
                        is Result.Error -> {
                            Log.e(TAG, "Result.Error - ${result.exception.message}")
                            result.exception.message?.let { message ->
                                setToastMessage(message)
                            }

                            _spinner.value = false
                        }
                        is Result.Canceled -> {
                            Log.d(TAG, "Result.Canceled")
                            setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))

                            _spinner.value = false
                        }
                    }
                }
            }
            catch (exception: Exception)
            {
                Toast.makeText(fragment.context?.applicationContext, "Sign In Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun checkUserLoggedIn(): FirebaseUser?
    {
        var _firebaseUser: FirebaseUser? = null
        viewModelScope.launch {
            _firebaseUser = authRepository.checkUserLoggedIn()
        }
        return _firebaseUser
    }

    suspend fun getCurrentUserFromFirestore(userId: String, fragment: Fragment? = null)
    {
        when(val result = firestoreRepository.getUserFromFirestore(userId))
        {
            is Result.Success -> {
                val _user = result.data
                _currentUserMLD.value = _user
                Log.d(TAG, "${_user.id}")
                fragment?.let {
                    navigateToMainFragment(fragment)
                }

                _spinner.value = false
            }
            is Result.Error -> {
                result.exception.message?.let { message ->
                    setToastMessage(message)
                }

                _spinner.value = false
            }
            is Result.Canceled -> {
                setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))

                _spinner.value = false
            }
        }
    }
    suspend fun getUserFromFirestore(userId: String): User?
    {
        return when(val result = firestoreRepository.getUserFromFirestore(userId))
        {
            is Result.Success -> {
                result.data
            }
            is Result.Error -> {
                result.exception.message?.let { message ->
                    setToastMessage(message)
                }
                null
            }
            is Result.Canceled -> {
                setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))
                null
            }
            else -> null
        }
    }
    fun updateNameOfUserInFirestore(user: User)
    {
        viewModelScope.launch {
            when(val result = firestoreRepository.updateNameOFUserInFirestore(user))
            {
                is Result.Success -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.your_name_has_been_updated))
                    onToastMessageDone()
                    _currentUserMLD.value = user
                }
                is Result.Error -> {
                    result.exception.message?.let {  message ->
                        setToastMessage(message)
                        onToastMessageDone()
                    }
                }
                is Result.Canceled -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))
                    onToastMessageDone()
                }
            }
        }
    }
    fun updateProfilePictureOfUserInFirestore(user: User)
    {
        viewModelScope.launch {
            when(val result = firestoreRepository.uploadProfilePictureOfUserToStorage(user))
            {
                is Result.Success -> {
                    result.data?.metadata?.reference?.downloadUrl?.let { uploadTask ->
                        uploadTask.addOnSuccessListener { _result ->
                            user.profilePicture = _result.toString()
                            Log.d(TAG, "1_ ${user}")
                            if(user.profilePicture.isNotEmpty())
                            {
                                updateProfilePictureUserInFirestore(user)
                            }
                        }
                    }
                }
                is Result.Error -> {
                    result.exception.message?.let {  message ->
                        setToastMessage(message)
                        onToastMessageDone()
                    }
                }
                is Result.Canceled -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))
                    onToastMessageDone()
                }
            }
        }
    }

    fun updateProfilePictureUserInFirestore(user: User)
    {
        viewModelScope.launch {
            when(val result = firestoreRepository.updateProfilePictureUserInFirestore(user))
            {
                is Result.Success -> {
                    Log.d(TAG, "2_ ${user}")
                    setToastMessage(MyApp.applicationContext().getString(R.string.your_profile_picture_has_been_updated))
                    _currentUserMLD.value = user
                }
                is Result.Error -> {
                    result.exception.message?.let {  message ->
                        setToastMessage(message)
                    }
                }
                is Result.Canceled -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))
                }
            }
        }
    }

    fun getListOfFilteredUsersFromFirestore(searchText: String, limit: Long)
    {
        _spinner.value = true
        viewModelScope.launch {
            // Set a delay to reduce the number of searches
            delay(1_000)

            when(val result = firestoreRepository.getListOfFilteredUsersFromFirestore(searchText, limit))
            {
                is Result.Success -> {
                    _usersListMLD.value = result.data
                    _spinner.value = false
                }
                is Result.Error -> {
                    setToastMessage(result.exception.toString())
                    _spinner.value = false
                }
                is Result.Canceled -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))
                    _spinner.value = false
                }
            }
        }
    }

    private suspend fun createUserInFirestore(user: User, fragment: Fragment)
    {
        Log.d(TAG, "Result - ${user.name}")
        when(val result = firestoreRepository.createUserInFirestore(user))
        {
            is Result.Success -> {
                when(fragment)
                {
                    is RegisterFragment -> {
                        setToastMessage(MyApp.applicationContext().getString(R.string.registration_successful))
                    }
                    is LoginFragment -> {
                        Log.d(TAG, "Result - ${user.name}")
                        setToastMessage(MyApp.applicationContext().getString(R.string.login_successful))
                    }
                }
                _currentUserMLD.value = user
                navigateToMainFragment(fragment)

                _spinner.value = false
            }
            is Result.Error -> {
                result.exception.message?.let { message ->
                    setToastMessage(message)
                }

                _spinner.value = false
            }
            is Result.Canceled -> {
                setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))

                _spinner.value = false
            }
        }
    }

    fun createUserObject(firebaseUser: FirebaseUser, name: String, profilePicture: String = ""): User
    {
        val currentUser = User(
            id =  firebaseUser.uid,
            name = name,
            profilePicture = profilePicture
        )

        return currentUser
    }

    fun setCurrentUser(userId: String, fragment: Fragment)
    {
        Log.i(TAG, "$userId _1")
        viewModelScope.launch {
            Log.i(TAG, "$userId _2")
            when (val result = firestoreRepository.getUserFromFirestore(userId))
            {
                is Result.Success -> {
                    Log.i(TAG, "$userId _3")
                    _currentUserMLD.value = result.data
                }
                is Result.Error -> {
                    result.exception.message?.let { message ->
                        setToastMessage(message)
                    }
                }
                is Result.Canceled -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))
                }
            }
        }
    }


    fun insertShoppingList(shoppingList: ShoppingList, fragment: Fragment)
    {
        viewModelScope.launch {
            when(val result = firestoreRepository.insertShoppingList(shoppingList))
            {
                is Result.Success -> {
                    Log.d(TAG, "Success")
                }
                is Result.Error -> {
                    result.exception.message?.let { message ->
                        setToastMessage(message)
                    }

                    _spinner.value = false
                }
                is Result.Canceled -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))

                    _spinner.value = false
                }
            }
        }
    }
    fun updateShoppingList(shoppingList: ShoppingList, fragment: Fragment)
    {
        viewModelScope.launch {
            Log.d(TAG, "$shoppingList")
            when (val result = firestoreRepository.updateShoppingList(shoppingList))
            {
                is Result.Success -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.item_updated))
                }
                is Result.Error -> {
                    result.exception.message?.let { message ->
                        setToastMessage(message)
                    }
                }
                is Result.Canceled -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))
                }
            }
        }
    }
    fun deleteShoppingList(shoppingListId: String)
    {
        viewModelScope.launch {
            when (val result = firestoreRepository.deleteShoppingList(shoppingListId))
            {
                is Result.Success -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.shopping_list_deleted))
                }
                is Result.Error -> {
                    result.exception.message?.let { message ->
                        setToastMessage(message)
                    }
                }
                is Result.Canceled -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))
                }
            }
        }
    }


    fun getCurrentUserShoppingListsRealTime(currentUser: User): MutableLiveData<Map<DocumentChange, ShoppingList>>
    {
        return firestoreRepository.getCurrentUserShoppingListsRealTime(currentUser)
    }
    fun getShoppingListRealTime(shoppingListId: String): MutableLiveData<ShoppingList>
    {
        return firestoreRepository.getShoppingListRealTime(shoppingListId)
    }

    // Friend
    fun getFriend(requestOwnerId: String, requestPartnerId: String): LiveData<Friend?>
    {
        val friend = MutableLiveData<Friend?>()

        viewModelScope.launch {
            when(val result = firestoreRepository.getFriend(requestOwnerId, requestPartnerId))
            {
                is Result.Success -> {
                    friend.value = result.data
                }
                is Result.Error -> {
                    result.exception.message?.let { message ->
                        setToastMessage(message)
                    }
                }
                is Result.Canceled -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))
                }
            }
        }

        return friend
    }

    // Friend requests
    fun getFriendRequest(requestOwnerId: String, requestPartnerId: String): LiveData<FriendRequest?>
    {
        val friendRequest = MutableLiveData<FriendRequest?>()

        viewModelScope.launch {
            when(val result = firestoreRepository.getFriendRequest(requestOwnerId, requestPartnerId))
            {
                is Result.Success -> {
                    friendRequest.value = result.data
                }
                is Result.Error -> {
                    result.exception.message?.let { message ->
                        setToastMessage(message)
                    }
                }
                is Result.Canceled -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))
                }
            }
        }

        return friendRequest
    }
    fun insertFriendRequest(friendRequest: FriendRequest)
    {
        _spinner.value = true
        viewModelScope.launch {
            when(val result = firestoreRepository.insertFriendRequest(friendRequest))
            {
                is Result.Success -> {

                    Log.d(TAG, "Success")

                    _spinner.value = false
                }
                is Result.Error -> {
                    result.exception.message?.let { message ->
                        setToastMessage(message)
                    }

                    _spinner.value = false
                }
                is Result.Canceled -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))

                    _spinner.value = false
                }
            }
        }
    }
    fun deleteFriendRequest(friendRequest: FriendRequest)
    {
        _spinner.value = true
        viewModelScope.launch {
            when(val result = firestoreRepository.deleteFriendRequest(friendRequest))
            {
                is Result.Success -> {
                    Log.d(TAG, "Success")

                    _spinner.value = false
                }
                is Result.Error -> {
                    result.exception.message?.let { message ->
                        setToastMessage(message)
                    }

                    _spinner.value = false
                }
                is Result.Canceled -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))

                    _spinner.value = false
                }
            }
        }
    }

    fun getFriendsFromFirestore(friendshipOwnerId: String): MutableLiveData<MutableList<Pair<Friend, User>>>
    {
        _spinner.value = true
        val listOfFriendsAndUsersLD = MutableLiveData<MutableList<Pair<Friend, User>>>()
        val listOfFriendsAndUsers = mutableListOf<Pair<Friend, User>>()

        viewModelScope.launch {
            when (val result = firestoreRepository.getListOfFriends(friendshipOwnerId))
            {
                is Result.Success -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.friend_deleted))
                    for(friend in result.data)
                    {
                        val user = getUserFromFirestore(friend.friendId)
                        user?.let {_user ->
                            listOfFriendsAndUsers.add(Pair(friend, _user))
                        }
                    }
                    _spinner.value = false
                }
                is Result.Error -> {
                    result.exception.message?.let { message ->
                        setToastMessage(message)
                    }
                    _spinner.value = false
                }
                is Result.Canceled -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))
                    _spinner.value = false
                }
            }
            listOfFriendsAndUsersLD.value = listOfFriendsAndUsers
        }
        return listOfFriendsAndUsersLD
    }

    fun deleteFriendFromFirestore (friendId: String): LiveData<FriendshipStatus>
    {
        val friendshipStatus = MutableLiveData<FriendshipStatus>()

        _spinner.value = true
        viewModelScope.launch {
            when (val result = firestoreRepository.deleteFriendFromFirestore(friendId))
            {
                is Result.Success -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.friend_deleted))
                    _spinner.value = false
                    friendshipStatus.value = FriendshipStatus.DELETED
                }
                is Result.Error -> {
                    result.exception.message?.let { message ->
                        setToastMessage(message)
                    }
                    _spinner.value = false
                    friendshipStatus.value = FriendshipStatus.ERROR
                }
                is Result.Canceled -> {
                    setToastMessage(MyApp.applicationContext().getString(R.string.request_canceled))
                    _spinner.value = false
                    friendshipStatus.value = FriendshipStatus.ERROR
                }
            }
        }

        return friendshipStatus
    }











    fun clearLastResultOfFriends()
    {
        firestoreRepository.clearLastResultOfFriends()
    }


    fun createFriendRequestInstance(date: Date, friendshipStatus: FriendshipStatus, requestOwnerId: String, requestPartnerId: String) = FriendRequest(
        date = date,
        friendshipStatus = friendshipStatus,
        requestOwnerId = requestOwnerId,
        requestPartnerId = requestPartnerId
    )




    private fun navigateToMainFragment(fragment: Fragment)
    {
        fragment.view?.let { view ->
            Log.d(TAG, "${fragment}")
            val navController: NavController = Navigation.findNavController(view)
            when(fragment)
            {
                is RegisterFragment -> {
                    val action = RegisterFragmentDirections.actionRegisterFragmentToMainFragment()
                    navController.navigate(action)

                    Log.d(TAG, "actionRegisterFragmentToMainFragment")
                    _spinner.value = false
                }
                is LoginFragment -> {
                    val action = LoginFragmentDirections.actionLoginFragmentToMainFragment()
                    navController.navigate(action)

                    Log.d(TAG, "actionLoginFragmentToMainFragment")
                    _spinner.value = false
                }
                is SplashFragment -> {
                    val action = SplashFragmentDirections.actionSplashFragmentToMainFragment()
                    navController.navigate(action)

                    Log.d(TAG, "actionSplashFragmentToMainFragment")
                    _spinner.value = false
                }
            }
        }
    }

    fun startSecurityCheck(source: String)
    {
        val pair = Pair(false, source)
        _securityCheck.value = pair
    }
    fun securityCheckDone(source: String)
    {
        val pair = Pair(true, source)
        _securityCheck.value = pair
    }
    fun clearSecurityCheck()
    {
        val pair = Pair(false, "")
        _securityCheck.value = pair
    }
    private fun setToastMessage(message: String)
    {
        val pair = Pair(true, message)
        _toast.value = pair
        onToastMessageDone()
    }
    private fun onToastMessageDone()
    {
        val pair = Pair(false, "")
        _toast.value = pair
    }
    fun hideSpinner()
    {
        _spinner.value = false
    }


    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, fragment: Fragment)
    {
        Log.d(TAG, "data: $data")

        callbackManager?.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RC_SIGN_IN)
        {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task, fragment)
        }
    }
}