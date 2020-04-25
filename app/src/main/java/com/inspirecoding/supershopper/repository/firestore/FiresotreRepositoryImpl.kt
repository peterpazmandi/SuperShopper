package com.inspirecoding.supershopper.repository.firestore

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.inspirecoding.supershopper.model.ListItem
import com.inspirecoding.supershopper.model.User
import com.inspirecoding.supershopper.repository.extension.await
import com.inspirecoding.supershopper.utilities.Result
import java.lang.Exception

private const val TAG = "FiresotreRepositoryImpl"
class FiresotreRepositoryImpl: FiresotreRepository
{
    private val USER_COLLECTION_NAME = "users"
    private val LISTITEM_COLLECTION_NAME = "listItems"

    private val firestoreInstance = FirebaseFirestore.getInstance()
    private val userCollection = firestoreInstance.collection(USER_COLLECTION_NAME)
    private val listItemCollection = firestoreInstance.collection(LISTITEM_COLLECTION_NAME)

    override suspend fun getUserFromFirestore(userId: String): Result<User>?
    {
        try
        {
            return when(val resultDocumentSnapshot = userCollection.document(userId).get().await())
            {
                is Result.Success -> {
                    val user = resultDocumentSnapshot.data.toObject(User::class.java)!!
                    Result.Success(user)
                }
                is Result.Error -> Result.Error(resultDocumentSnapshot.exception)
                is Result.Canceled -> Result.Canceled(resultDocumentSnapshot.exception)
            }
        }
        catch (exception: Exception)
        {
            return Result.Error(exception)
        }
    }
    override suspend fun getListOfFilteredUsersFromFirestore(searchText: String, limit: Long): Result<List<User>>
    {
        try
        {
            return when(val resultsDocumentSnapshot = userCollection
                .whereGreaterThanOrEqualTo("name", searchText)
                .whereLessThanOrEqualTo("name", searchText + '\uf8ff')
                .orderBy("name", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await())
            {
                is Result.Success -> {
                    val usersList = mutableListOf<User>()
                    for(resultDocumentSnapshot in resultsDocumentSnapshot.data)
                    {
                        val user = resultDocumentSnapshot.toObject(User::class.java)
                        usersList.add(user)
                    }
                    Log.d(TAG, "$usersList")
                    Result.Success(usersList)
                }
                is Result.Error -> Result.Error(resultsDocumentSnapshot.exception)
                is Result.Canceled -> Result.Canceled(resultsDocumentSnapshot.exception)
            }
        }
        catch (exception: Exception)
        {
            return Result.Error(exception)
        }
    }
    override suspend fun createUserInFirestore(user: User): Result<Void?>
    {
        return try
        {
            userCollection.document(user.id).set(user).await()
        }
        catch (exception: Exception)
        {
            Result.Error(exception)
        }
    }

    override suspend fun insertItemsList(listItem: ListItem): Result<Void?>
    {
        return try
        {
            listItemCollection.document().set(listItem).await()
        }
        catch (exception: Exception)
        {
            Result.Error(exception)
        }
    }
}