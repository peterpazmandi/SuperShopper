package com.inspirecoding.supershopper.repository.firestore

import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentChange
import com.inspirecoding.supershopper.model.ListItem
import com.inspirecoding.supershopper.model.ShoppingList
import com.inspirecoding.supershopper.model.User
import com.inspirecoding.supershopper.utilities.Result

interface FirestoreRepository
{
    suspend fun getUserFromFirestore(userId: String): Result<User>?
    suspend fun getListOfFilteredUsersFromFirestore(searchText: String, limit: Long): Result<List<User>>
    suspend fun createUserInFirestore(user: User): Result<Void?>
    suspend fun updateNameOFUserInFirestore(user: User): Result<Void?>
    suspend fun updateProfilePictureOFUserInFirestore(user: User): Result<Void?>

    suspend fun insertShoppingList(shoppingList: ShoppingList): Result<Void?>
    suspend fun updateShoppingList(shoppingList: ShoppingList): Result<Void?>
    suspend fun deleteShoppingList(shoppingListId: String): Result<Void?>

    fun getCurrentUserShoppingListsRealTime(currentUser: User): MutableLiveData<Map<DocumentChange, ShoppingList>>
    fun getShoppingListRealTime(shoppingListId: String): MutableLiveData<ShoppingList>
}