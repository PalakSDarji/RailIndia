package com.palak.railindia.di

import android.app.Application
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.palak.railindia.db.AppDb
import com.palak.railindia.db.ComponentDao
import com.palak.railindia.db.EntryDao
import com.palak.railindia.network.EntryService
import com.palak.railindia.network.EntryServiceImpl
import com.palak.railindia.repo.ComponentRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.text.SimpleDateFormat
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideAppDb(application: Application): AppDb {
        return AppDb.getDb(application.applicationContext)
    }

    @Singleton
    @Provides
    fun provideComponentDao(appDb: AppDb): ComponentDao {
        return appDb.componentDao()
    }

    @Singleton
    @Provides
    fun provideEntryDao(appDb: AppDb): EntryDao {
        return appDb.entryDao()
    }

    @Singleton
    @Provides
    fun provideEntryService(
        @EntryDataRef databaseReference: DatabaseReference,
        @DateSDF dateFormat: SimpleDateFormat, componentRepo: ComponentRepo
    ): EntryService {

        return EntryServiceImpl(databaseReference, dateFormat,componentRepo)
    }

    @Singleton
    @Provides
    fun provideFirebaseDatabase(): FirebaseDatabase {
        return Firebase.database
    }

    @Singleton
    @Provides
    @ComponentDataRef
    fun provideFirebaseDatabaseComponentDataReference(firebaseDatabase: FirebaseDatabase): DatabaseReference {
        return firebaseDatabase.getReference("componentData")
    }

    @Singleton
    @Provides
    @EntryDataRef
    fun provideFirebaseDatabaseEntryDataRef(firebaseDatabase: FirebaseDatabase): DatabaseReference {
        return firebaseDatabase.getReference("entryData")
    }

    @Singleton
    @Provides
    @TimeStampSDF
    fun provideTimeStampSdfTime(): SimpleDateFormat {
        return SimpleDateFormat("dd_MM_yyyy_hh_mm_ss")
    }

    @Singleton
    @Provides
    @DateSDF
    fun provideDateSdfTime(): SimpleDateFormat {
        return SimpleDateFormat("dd MMM yyyy")
    }

    @Singleton
    @Provides
    @MonthSDF
    fun provideMonthSdfTime(): SimpleDateFormat {
        return SimpleDateFormat("MMM yyyy")
    }

    @Singleton
    @Provides
    @DateMonthSDF
    fun provideDateMonthSdfTime(): SimpleDateFormat {
        return SimpleDateFormat("dd/MM")
    }
}


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ComponentDataRef

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class EntryDataRef

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DateSDF

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MonthSDF

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DateMonthSDF

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TimeStampSDF