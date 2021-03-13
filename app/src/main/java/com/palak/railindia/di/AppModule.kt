package com.palak.railindia.di

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {


    @Singleton
    @Provides
    fun provideFirebaseDatabase() : FirebaseDatabase {
        return Firebase.database
    }

    @Singleton
    @Provides
    @ComponentDataRef
    fun provideFirebaseDatabaseComponentDataReference(firebaseDatabase: FirebaseDatabase) : DatabaseReference {
        return firebaseDatabase.getReference("componentData")
    }

}


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ComponentDataRef
