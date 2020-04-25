package com.inspirecoding.supershopper.repository.authentication

import android.content.Context
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.inspirecoding.supershopper.utilities.Result

interface AuthRepository
{
    suspend fun logInUserFromAuthWithEmailAndPassword(email: String, password: String): Result<FirebaseUser?>

    suspend fun checkUserLoggedIn(): FirebaseUser?
    suspend fun logOutUser()

    suspend fun registerUserFromAuthWithEmailAndPassword(email: String, password: String, context: Context): Result<FirebaseUser?>

    suspend fun sendPasswordResetEmail(email: String): Result<Void?>

    suspend fun signInWithCredential(authCredential: AuthCredential): Result<AuthResult?>
}