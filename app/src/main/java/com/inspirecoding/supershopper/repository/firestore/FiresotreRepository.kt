package com.inspirecoding.supershopper.repository.firestore

import com.inspirecoding.supershopper.model.ListItem
import com.inspirecoding.supershopper.model.User
import com.inspirecoding.supershopper.utilities.Result

interface FiresotreRepository
{
    suspend fun getUserFromFirestore(userId: String): Result<User>?
    suspend fun getListOfFilteredUsersFromFirestore(searchText: String, limit: Long): Result<List<User>>
    suspend fun createUserInFirestore(user: User): Result<Void?>

    suspend fun insertItemsList(listItem: ListItem) : Result<Void?>
}