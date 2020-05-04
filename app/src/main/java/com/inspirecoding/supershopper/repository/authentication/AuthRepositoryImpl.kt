package com.inspirecoding.supershopper.repository.authentication

import android.content.Context
import android.util.Log
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.inspirecoding.supershopper.MyApp
import com.inspirecoding.supershopper.repository.extension.await
import com.inspirecoding.supershopper.utilities.Result


private const val TAG = "AuthRepositoryImpl"
class AuthRepositoryImpl : AuthRepository
{
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override suspend fun logInUserFromAuthWithEmailAndPassword(email: String, password: String): Result<FirebaseUser?>
    {
        try
        {
            return when(val resultDocumentSnapshot = firebaseAuth.signInWithEmailAndPassword(email, password).await())
            {
                is Result.Success -> {
                    Log.i(TAG, "Result.Success")
                    val firebaseUser = resultDocumentSnapshot.data.user
                    Result.Success(firebaseUser)
                }
                is Result.Error -> {
                    Log.e(TAG, "${resultDocumentSnapshot.exception}")
                    Result.Error(resultDocumentSnapshot.exception)
                }
                is Result.Canceled ->  {
                    Log.e(TAG, "${resultDocumentSnapshot.exception}")
                    Result.Canceled(resultDocumentSnapshot.exception)
                }
            }
        }
        catch (exception: Exception)
        {
            return Result.Error(exception)
        }
    }

    override suspend fun checkUserLoggedIn(): FirebaseUser?
    {
        return firebaseAuth.currentUser
    }

    override suspend fun logOutUser()
    {
        firebaseAuth.signOut()
    }

    override suspend fun reAuthenticateUser(email: String, password: String): Result<Void?>
    {
        try
        {
            val user = firebaseAuth.currentUser
            val credential: AuthCredential = EmailAuthProvider.getCredential(email, password)
            val googleSignInAccount: GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(MyApp.applicationContext())

            if(googleSignInAccount != null)
            {
                val authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.idToken, null)
                return when(val resultDocumentSnapshot = user?.reauthenticate(authCredential)?.await())
                {
                    is Result.Success -> {
                        Result.Success(resultDocumentSnapshot.data)
                    }
                    is Result.Error -> {
                        Log.e(TAG, "${resultDocumentSnapshot.exception}")
                        Result.Error(resultDocumentSnapshot.exception)
                    }
                    is Result.Canceled ->  {
                        Log.e(TAG, "${resultDocumentSnapshot.exception}")
                        Result.Canceled(resultDocumentSnapshot.exception)
                    }
                    else -> Result.Canceled(null)
                }
            }

            Log.d(TAG, "${AccessToken.isCurrentAccessTokenActive()}")
            AccessToken.refreshCurrentAccessTokenAsync()
            val facebookAuthProvider: AuthCredential? = FacebookAuthProvider.getCredential(AccessToken.getCurrentAccessToken().toString())
            if(facebookAuthProvider != null)
            {
                return when(val resultDocumentSnapshot = user?.reauthenticate(facebookAuthProvider)?.await())
                {
                    is Result.Success -> {
                        Result.Success(resultDocumentSnapshot.data)
                    }
                    is Result.Error -> {
                        Log.e(TAG, "${resultDocumentSnapshot.exception}")
                        Result.Error(resultDocumentSnapshot.exception)
                    }
                    is Result.Canceled ->  {
                        Log.e(TAG, "${resultDocumentSnapshot.exception}")
                        Result.Canceled(resultDocumentSnapshot.exception)
                    }
                    else -> Result.Canceled(null)
                }
            }
            else
            {
                return when(val resultDocumentSnapshot = user?.reauthenticate(credential)?.await())
                {
                    is Result.Success -> {
                        Result.Success(resultDocumentSnapshot.data)
                    }
                    is Result.Error -> {
                        Log.e(TAG, "${resultDocumentSnapshot.exception}")
                        Result.Error(resultDocumentSnapshot.exception)
                    }
                    is Result.Canceled ->  {
                        Log.e(TAG, "${resultDocumentSnapshot.exception}")
                        Result.Canceled(resultDocumentSnapshot.exception)
                    }
                    else -> Result.Canceled(null)
                }
            }
        }
        catch (exception: Exception)
        {
            return Result.Error(exception)
        }
    }

    override suspend fun registerUserFromAuthWithEmailAndPassword(email: String, password: String, context: Context): Result<FirebaseUser?>
    {
        try
        {
            return when(val resultDocumentSnapshot = firebaseAuth.createUserWithEmailAndPassword(email, password).await())
            {
                is Result.Success -> {
                    Log.i(TAG, "Result.Success")
                    val firebaseUser = resultDocumentSnapshot.data.user
                    Result.Success(firebaseUser)
                }
                is Result.Error -> {
                    Log.e(TAG, "${resultDocumentSnapshot.exception}")
                    Result.Error(resultDocumentSnapshot.exception)
                }
                is Result.Canceled ->  {
                    Log.e(TAG, "${resultDocumentSnapshot.exception}")
                    Result.Canceled(resultDocumentSnapshot.exception)
                }
            }
        }
        catch (exception: Exception)
        {
            return Result.Error(exception)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Void?>
    {
        return try
        {
            firebaseAuth.sendPasswordResetEmail(email).await()
        }
        catch (exception: Exception)
        {
            Result.Error(exception)
        }
    }
    override suspend fun updatePassword(newPassword: String): Result<Void?>
    {
        return try
        {
            if(firebaseAuth.currentUser != null)
            {
                firebaseAuth.currentUser?.updatePassword(newPassword)!!.await()
            }
            else
            {
                Result.Canceled(Exception())
            }
        }
        catch (exception: Exception)
        {
            Result.Error(exception)
        }
    }

    override suspend fun updateEmail(email: String): Result<Void?>
    {
        return try
        {
            if(firebaseAuth.currentUser != null)
            {
                firebaseAuth.currentUser?.updateEmail(email)!!.await()
            }
            else
            {
                Result.Canceled(Exception())
            }
        }
        catch (exception: Exception)
        {
            Result.Error(exception)
        }
    }

    override suspend fun signInWithCredential(authCredential: AuthCredential): Result<AuthResult?>
    {
        return firebaseAuth.signInWithCredential(authCredential).await()
    }
}