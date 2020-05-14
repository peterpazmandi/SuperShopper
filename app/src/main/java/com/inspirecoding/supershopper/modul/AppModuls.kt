package com.inspirecoding.supershopper.modul

import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.inspirecoding.supershopper.repository.SharedPreferencesViewModel
import com.inspirecoding.supershopper.repository.authentication.AuthRepositoryImpl
import com.inspirecoding.supershopper.repository.firestore.FirestoreRepositoryImpl
import com.inspirecoding.supershopper.repository.sharedpreferences.SharedPreferencesRepository
import org.koin.dsl.module

val firebaseViewModelModule = module {
    single { FirebaseViewModel(AuthRepositoryImpl(), FirestoreRepositoryImpl()) }
    single { SharedPreferencesViewModel(SharedPreferencesRepository()) }
}