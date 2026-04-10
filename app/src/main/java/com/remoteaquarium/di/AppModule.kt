package com.remoteaquarium.di

import android.content.Context
import android.hardware.SensorManager
import com.remoteaquarium.data.repository.AquariumRepositoryImpl
import com.remoteaquarium.domain.repository.AquariumRepository
import com.remoteaquarium.presentation.sensor.DeviceSensorDataProvider
import com.remoteaquarium.presentation.sensor.SensorDataProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindAquariumRepository(impl: AquariumRepositoryImpl): AquariumRepository

    @Binds
    @Singleton
    abstract fun bindSensorDataProvider(impl: DeviceSensorDataProvider): SensorDataProvider

    companion object {
        @Provides
        @Singleton
        fun provideSensorManager(@ApplicationContext context: Context): SensorManager =
            context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
}
