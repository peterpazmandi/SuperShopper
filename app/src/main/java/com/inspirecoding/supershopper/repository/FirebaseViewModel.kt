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
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.fragments.*
import com.inspirecoding.supershopper.model.User
import com.inspirecoding.supershopper.repository.authentication.AuthRepository
import com.inspirecoding.supershopper.repository.firestore.FirestoreRepository
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import com.inspirecoding.supershopper.utilities.Result
import com.inspirecoding.supershopper.fragments.LoginFragment
import com.inspirecoding.supershopper.model.ShoppingList

private const val TAG = "FirebaseViewModel"
class FirebaseViewModel(val authRepository: AuthRepository, val firestoreRepository: FirestoreRepository): ViewModel()
{
    private var callbackManager: CallbackManager? = null

    private lateinit var googleSingInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1


    private val _toast = MutableLiveData<String?>()
    val toast: LiveData<String?>
        get() = _toast

    private val _spinner = MutableLiveData<Boolean>(false)
    val spinner: LiveData<Boolean>
        get() = _spinner

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
                            _toast.value = fragment.getString(R.string.registration_successful)
                        }
                    }
                    is Result.Error -> {
                        Log.e(TAG, "${result.exception.message}")

                        _spinner.value = false
                        _toast.value = result.exception.message
                    }
                    is Result.Canceled -> {
                        Log.e(TAG, "${result.exception!!.message}")

                        _spinner.value = false
                        _toast.value = fragment.getString(R.string.request_canceled)
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
                    _toast.value = result.exception.message

                    _spinner.value = false
                }
                is Result.Canceled -> {
                    _toast.value = fragment.getString(R.string.request_canceled)

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
                    Toast.makeText(fragment.context, fragment.getString(R.string.check_your_emails_to_reset_your_password), Toast.LENGTH_SHORT).show()
                }
                is Result.Error -> {
                    _toast.value = result.exception.message
                }
                is Result.Canceled -> {
                    _toast.value = fragment.getString(R.string.request_canceled)
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
        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult>
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
                            _toast.value = result.exception.message

                            _spinner.value = false
                        }
                        is Result.Canceled -> {
                            Log.d(TAG, "Result.Canceled")
                            _toast.value = activity.applicationContext.getString(R.string.request_canceled)

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
                            _toast.value = result.exception.message

                            _spinner.value = false
                        }
                        is Result.Canceled -> {
                            Log.d(TAG, "Result.Canceled")
                            _toast.value = fragment.getString(R.string.request_canceled)

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
    fun logOutUser()
    {
        viewModelScope.launch {
            authRepository.logOutUser()
        }
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
                _toast.value = result.exception.message

                _spinner.value = false
            }
            is Result.Canceled -> {
                _toast.value = fragment?.getString(R.string.request_canceled)

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
                _toast.value = result.exception.message
                null
            }
            is Result.Canceled -> {
                null
            }
            else -> null
        }
    }

    fun getListOfFilteredUsersFromFirestore(searchText: String, limit: Long)
    {
        viewModelScope.launch {
            when(val result = firestoreRepository.getListOfFilteredUsersFromFirestore(searchText, limit))
            {
                is Result.Success -> {
                    _usersListMLD.value = result.data
                }
                is Result.Error -> {

                }
                is Result.Canceled -> {

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
                        _toast.value = fragment.getString(R.string.registration_successful)
                    }
                    is LoginFragment -> {
                        Log.d(TAG, "Result - ${user.name}")
                        _toast.value = fragment.getString(R.string.login_successful)
                    }
                }
                _currentUserMLD.value = user
                navigateToMainFragment(fragment)

                _spinner.value = false
            }
            is Result.Error -> {
                _toast.value = result.exception.message

                _spinner.value = false
            }
            is Result.Canceled -> {
                _toast.value = fragment.getString(R.string.request_canceled)

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
                    _toast.value = result.exception.message
                }
                is Result.Canceled -> {
                    _toast.value = fragment.getString(R.string.request_canceled)
                }
            }
        }
    }

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


    fun insertListItemInFirestore(shoppingList: ShoppingList, fragment: Fragment)
    {
        viewModelScope.launch {
            when(val result = firestoreRepository.insertShoppingList(shoppingList))
            {
                is Result.Success -> {
                    Log.d(TAG, "Success")
                }
                is Result.Error -> {
                    _toast.value = result.exception.message

                    _spinner.value = false
                }
                is Result.Canceled -> {
                    _toast.value = fragment.getString(R.string.request_canceled)

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
                    _toast.value = fragment.getString(R.string.item_updated)
                }
                is Result.Error -> {
                    _toast.value = result.exception.message
                }
                is Result.Canceled -> {
                    _toast.value = fragment.getString(R.string.request_canceled)
                }
            }
        }
    }


    fun getCurrentUserShoppingListsRealTime(currentUser: User): MutableLiveData<Map<DocumentChange, ShoppingList>>
    {
        return firestoreRepository.getCurrentUserShoppingListsRealTime(currentUser)
    }
    fun getShoppingListRealTime(shoppingListId: String): MutableLiveData<Map<DocumentChange, ShoppingList>>
    {
        return firestoreRepository.getShoppingListRealTime(shoppingListId)
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