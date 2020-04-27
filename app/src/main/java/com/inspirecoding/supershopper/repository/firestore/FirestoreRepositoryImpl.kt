package com.inspirecoding.supershopper.repository.firestore

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.inspirecoding.supershopper.model.ShoppingList
import com.inspirecoding.supershopper.model.User
import com.inspirecoding.supershopper.repository.extension.await
import com.inspirecoding.supershopper.utilities.Result
import java.lang.Exception

private const val TAG = "FiresotreRepositoryImpl"
class FirestoreRepositoryImpl: FirestoreRepository
{
    private val USER_COLLECTION_NAME = "users"
    private val SHOPPINGLIST_COLLECTION_NAME = "shoppingList"

    private val firestoreInstance = FirebaseFirestore.getInstance()
    private val userCollection = firestoreInstance.collection(USER_COLLECTION_NAME)
    private val shoppingListCollection = firestoreInstance.collection(SHOPPINGLIST_COLLECTION_NAME)

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

    override suspend fun insertShoppingList(shoppingList: ShoppingList): Result<Void?>
    {
        return try
        {
            shoppingListCollection.document().set(shoppingList).await()
        }
        catch (exception: Exception)
        {
            Result.Error(exception)
        }
    }

    override suspend fun updateShoppingList(shoppingList: ShoppingList): Result<Void?>
    {
        return shoppingListCollection.document(shoppingList.id).set(shoppingList).await()
    }

    override fun getCurrentUserShoppingListsRealTime(currentUser: User): MutableLiveData<Map<DocumentChange, ShoppingList>>
    {
        val shoppingListLiveData = MutableLiveData<Map<DocumentChange, ShoppingList>>()

        try
        {
            shoppingListCollection
                .whereArrayContains("friendsSharedWith", currentUser.id)
                .addSnapshotListener { resultDocumentSnapshot, firebaseFirestoreException ->
                    resultDocumentSnapshot?.let {
                        val mapOfResult = mutableMapOf<DocumentChange, ShoppingList>()
                        for (document in resultDocumentSnapshot.documentChanges)
                        {
                            when (document.type)
                            {
                                DocumentChange.Type.ADDED -> {
                                    mapOfResult.put(document, createShoppingList(document))
                                    Log.d(TAG, "${mapOfResult}")
                                }

                                DocumentChange.Type.MODIFIED -> {
                                    mapOfResult.put(document, createShoppingList(document))
                                }

                                DocumentChange.Type.REMOVED -> {
                                    mapOfResult.put(document, createShoppingList(document))
                                }
                            }
                        }
                        val sortedMap = mapOfResult.toList().sortedByDescending {
                            (_, value) -> value.timeStamp
                        }.toMap()

                        shoppingListLiveData.value = sortedMap
                    }
            }
        }
        catch  (exception: Exception)
        {
            return shoppingListLiveData
        }

        Log.d(TAG, "__::${shoppingListLiveData.value}")
        return shoppingListLiveData
    }

    fun createShoppingList(documentChange: DocumentChange): ShoppingList
    {
        val shoppingList = documentChange.document.toObject(ShoppingList::class.java)
        shoppingList.id = documentChange.document.id

        Log.d(TAG, "${documentChange.type}: ${shoppingList}")

        return shoppingList
    }











}