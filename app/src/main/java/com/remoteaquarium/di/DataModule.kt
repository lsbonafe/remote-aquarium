package com.remoteaquarium.di

import com.remoteaquarium.data.datasource.AquariumDataSource
import com.remoteaquarium.data.datasource.MockAquariumDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAquariumDataSource(impl: MockAquariumDataSource): AquariumDataSource
}
