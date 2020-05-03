package com.inspirecoding.supershopper.repository.authentication

import android.content.Context
import android.util.Log
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.inspirecoding.supershopper.repository.extension.await
import com.inspirecoding.supershopper.utilities.Result
import java.lang.Exception

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

    override suspend fun signInWithCredential(authCredential: AuthCredential): Result<AuthResult?>
    {
        return firebaseAuth.signInWithCredential(authCredential).await()
    }
}