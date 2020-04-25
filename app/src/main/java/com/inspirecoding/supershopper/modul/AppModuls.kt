package com.inspirecoding.supershopper.modul

import com.inspirecoding.supershopper.repository.FirebaseViewModel
import com.inspirecoding.supershopper.repository.authentication.AuthRepositoryImpl
import com.inspirecoding.supershopper.repository.firestore.FiresotreRepositoryImpl
import org.koin.dsl.module

val firebaseViewModelModule = module {
    single { FirebaseViewModel(AuthRepositoryImpl(), FiresotreRepositoryImpl()) }
}