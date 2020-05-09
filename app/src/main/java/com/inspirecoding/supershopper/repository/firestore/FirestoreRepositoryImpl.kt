package com.inspirecoding.supershopper.repository.firestore

import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.inspirecoding.supershopper.model.Friend
import com.inspirecoding.supershopper.model.FriendRequest
import com.inspirecoding.supershopper.model.ShoppingList
import com.inspirecoding.supershopper.model.User
import com.inspirecoding.supershopper.repository.extension.await
import com.inspirecoding.supershopper.utilities.Result
import java.io.File
import java.lang.Exception

private const val TAG = "FiresotreRepositoryImpl"
class FirestoreRepositoryImpl: FirestoreRepository
{
    private val USER_COLLECTION_NAME = "users"
    private val SHOPPINGLIST_COLLECTION_NAME = "shoppingList"
    private val FRIENDS_COLLECTION_NAME = "friends"
    private val FRIENDSREQUEST_COLLECTION_NAME = "friend_requests"

    private val firestoreInstance = FirebaseFirestore.getInstance()
    private var imageStorage  = FirebaseStorage.getInstance()
    private val userCollection = firestoreInstance.collection(USER_COLLECTION_NAME)
    private val shoppingListCollection = firestoreInstance.collection(SHOPPINGLIST_COLLECTION_NAME)
    private val friendsCollection = firestoreInstance.collection(FRIENDS_COLLECTION_NAME)
    private val friendsRequestCollection = firestoreInstance.collection(FRIENDSREQUEST_COLLECTION_NAME)

    private var lastResultOfFriends: DocumentSnapshot? = null
    private var lastResultOfReceivedFriendsRequests: DocumentSnapshot? = null

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
                .get().await())
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

    override suspend fun updateNameOFUserInFirestore(user: User): Result<Void?>
    {
        return try
        {
            userCollection.document(user.id)
                .update("name", user.name)
                .await()
        }
        catch (exception: Exception)
        {
            Result.Error(exception)
        }
    }
    override suspend fun updateProfilePictureUserInFirestore(user: User): Result<Void?>
    {
        return try
        {
            userCollection.document(user.id)
                .update("profilePicture", user.profilePicture)
                .await()
        }
        catch (exception: Exception)
        {
            Result.Error(exception)
        }
    }
    override suspend fun uploadProfilePictureOfUserToStorage(user: User): Result<UploadTask.TaskSnapshot?>
    {
        return try
        {
            val image = Uri.fromFile(File(user.profilePicture))
            val storageRef = imageStorage.reference
            val profileImageReference = storageRef.child("profileImages/${image.lastPathSegment}")
            profileImageReference.putFile(image).await()
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
        return try
        {
            shoppingListCollection.document(shoppingList.id).set(shoppingList).await()
        }
        catch (exception: Exception)
        {
            Result.Error(exception)
        }
    }

    override suspend fun deleteShoppingList(shoppingListId: String): Result<Void?>
    {
        return try
        {
            shoppingListCollection.document(shoppingListId).delete().await()
        }
        catch (exception: Exception)
        {
            Result.Error(exception)
        }
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

    override fun getShoppingListRealTime(shoppingListId: String): MutableLiveData<ShoppingList>
    {
        val shoppingListLiveData = MutableLiveData<ShoppingList>()

        try
        {
            shoppingListCollection.document(shoppingListId)
                .addSnapshotListener { resultDocumentSnapshot, firebaseFirestoreException ->
                    resultDocumentSnapshot?.let {document ->
                        val shoppingList = document.toObject(ShoppingList::class.java)
                        Log.d(TAG, "1_ ${shoppingList}")
                        shoppingList?.let { _shoppingList ->
                            shoppingListLiveData.value = shoppingList
                        }
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

    // Friend
    override suspend fun getListOfFriends(friendshipOwnerId: String): Result<List<Friend>>
    {
        val resultsDocumentSnapshot: Result<QuerySnapshot>

        if (lastResultOfFriends == null)
        {
            resultsDocumentSnapshot = friendsCollection
                .whereEqualTo("friendshipOwnerId", friendshipOwnerId)
                .orderBy("friendName", Query.Direction.ASCENDING)
                .limit(10)
                .get().await()
        }
        else
        {
            Log.d(TAG, "getFriendsFromFirestore_3: $lastResultOfFriends")
            resultsDocumentSnapshot = friendsCollection
                .whereEqualTo("friendshipOwnerId", friendshipOwnerId)
                .orderBy("friendName", Query.Direction.ASCENDING)
                .startAfter(lastResultOfFriends as DocumentSnapshot)
                .limit(10)
                .get().await()
        }

        return when (resultsDocumentSnapshot) {
            is Result.Success -> {
                val usersList = mutableListOf<Friend>()
                for(resultDocumentSnapshot in resultsDocumentSnapshot.data)
                {
                    val friend = resultDocumentSnapshot.toObject(Friend::class.java)
                    friend.id = resultDocumentSnapshot.id
                    usersList.add(friend)
                }
                Log.d(TAG, "$usersList")

                if(resultsDocumentSnapshot.data.documents.size != 0)
                {
                    lastResultOfFriends = resultsDocumentSnapshot.data.documents[resultsDocumentSnapshot.data.documents.size -1]
                }
                Result.Success(usersList)
            }
            is Result.Error -> Result.Error(resultsDocumentSnapshot.exception)
            is Result.Canceled -> Result.Canceled(resultsDocumentSnapshot.exception)
        }
    }
    override suspend fun getFriend(friendshipOwnerId: String, friendId: String): Result<Friend>
    {
        var friend: Friend = Friend()
        try
        {
            return when(val resultsDocumentSnapshot = friendsCollection
                .whereEqualTo("friendshipOwnerId", friendshipOwnerId)
                .whereEqualTo("friendId", friendId)
                .get().await())
            {
                is Result.Success -> {
                    for(resultDocumentSnapshot in resultsDocumentSnapshot.data)
                    {
                        friend = resultDocumentSnapshot.toObject(Friend::class.java)
                        friend.id = resultDocumentSnapshot.id
                    }
                    Result.Success(friend)
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
    override suspend fun insertFriend(friend: Friend): Result<Void?>
    {
        return try
        {
            friendsCollection.document().set(friend).await()
        }
        catch (exception: Exception)
        {
            Result.Error(exception)
        }
    }
    override suspend fun deleteFriend(friend: Friend): Result<Void?>
    {
        return try
        {
            friendsCollection.document(friend.id).delete().await()
        }
        catch (exception: Exception)
        {
            Result.Error(exception)
        }
    }

    // Friend requests
    override suspend fun getFriendRequest(requestOwnerId: String, requestPartnerId: String): Result<FriendRequest>
    {
        Log.d(TAG, "$requestOwnerId, $requestPartnerId")
        var friendRequest: FriendRequest = FriendRequest()
        try
        {
            return when(val resultsDocumentSnapshot = friendsRequestCollection
                .whereEqualTo("requestOwnerId", requestOwnerId)
                .whereEqualTo("requestPartnerId", requestPartnerId)
                .get().await())
            {
                is Result.Success -> {
                    for(resultDocumentSnapshot in resultsDocumentSnapshot.data)
                    {
                        friendRequest = resultDocumentSnapshot.toObject(FriendRequest::class.java)
                        friendRequest.id = resultDocumentSnapshot.id
                    }
                    Log.d(TAG, "$friendRequest")
                    Result.Success(friendRequest)
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
    override suspend fun getReceiverFriendRequest(requestPartnerId: String): Result<List<FriendRequest>>
    {
        val resultsDocumentSnapshot: Result<QuerySnapshot>

        if (lastResultOfReceivedFriendsRequests == null)
        {
            resultsDocumentSnapshot = friendsRequestCollection
                .whereEqualTo("requestPartnerId", requestPartnerId)
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(10)
                .get().await()
        }
        else
        {
            resultsDocumentSnapshot = friendsRequestCollection
                .whereEqualTo("requestPartnerId", requestPartnerId)
                .orderBy("date", Query.Direction.DESCENDING)
                .startAfter(lastResultOfReceivedFriendsRequests as DocumentSnapshot)
                .limit(10)
                .get().await()
        }

        return when (resultsDocumentSnapshot) {
            is Result.Success -> {
                val friendRequestsList = mutableListOf<FriendRequest>()
                for(resultDocumentSnapshot in resultsDocumentSnapshot.data)
                {
                    val friend = resultDocumentSnapshot.toObject(FriendRequest::class.java)
                    friend.id = resultDocumentSnapshot.id
                    friendRequestsList.add(friend)
                }
                Log.d(TAG, "$friendRequestsList")

                if(resultsDocumentSnapshot.data.documents.size != 0)
                {
                    lastResultOfReceivedFriendsRequests = resultsDocumentSnapshot.data.documents[resultsDocumentSnapshot.data.documents.size -1]
                }
                Result.Success(friendRequestsList)
            }
            is Result.Error -> Result.Error(resultsDocumentSnapshot.exception)
            is Result.Canceled -> Result.Canceled(resultsDocumentSnapshot.exception)
        }
    }

    override suspend fun insertFriendRequest(friendRequest: FriendRequest): Result<Void?>
    {
        return try
        {
            friendsRequestCollection.document().set(friendRequest).await()
        }
        catch (exception: Exception)
        {
            Result.Error(exception)
        }
    }
    override suspend fun deleteFriendRequest(friendRequest: FriendRequest): Result<Void?>
    {
        return try
        {
            friendsRequestCollection.document(friendRequest.id).delete().await()
        }
        catch (exception: Exception)
        {
            Result.Error(exception)
        }
    }
    override suspend fun deleteFriendFromFirestore(friendId: String): Result<Void?>
    {
        return try
        {
             friendsCollection.document(friendId).delete().await()
        }
        catch (exception: Exception)
        {
            Result.Error(exception)
        }
    }




    override fun clearLastResultOfFriends()
    {
        lastResultOfFriends = null
    }

    override fun clearLastResultOfFriendsRequests()
    {
        lastResultOfReceivedFriendsRequests = null
    }

    fun createShoppingList(documentChange: DocumentChange): ShoppingList
    {
        val shoppingList = documentChange.document.toObject(ShoppingList::class.java)
        shoppingList.id = documentChange.document.id

        Log.d(TAG, "${documentChange.type}: ${shoppingList}")

        return shoppingList
    }
    fun createFriend(documentChange: DocumentChange): Friend
    {
        val friend = documentChange.document.toObject(Friend::class.java)
        friend.id = documentChange.document.id

        Log.d(TAG, "${documentChange.type}: ${friend}")

        return friend
    }








}