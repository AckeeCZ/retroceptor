package io.ackeecz.sample.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.ackeecz.sample.App
import io.ackeecz.sample.detail.DetailViewModel
import io.ackeecz.sample.interactor.ApiInteractor
import io.ackeecz.sample.interactor.ApiInteractorImpl
import io.ackeecz.sample.login.LoginViewModel
import io.ackeecz.sample.model.Logouter
import io.ackeecz.sample.model.rest.ApiDescription
import io.ackeecz.sample.model.rest.AuthApiDescription
import io.appflate.restmock.RESTMockServer
import io.github.ackeecz.retroceptor.OAuthManager
import io.github.ackeecz.retroceptor.OAuthRefreshCallInterceptor
import io.github.ackeecz.retroceptor.retrofitadapter.RetroceptorCallAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Simple DI container that provides dependencies
 */

class DIContainer(app: App) : ViewModelProvider.Factory {

    val oAuthManager = OAuthManager(
        context = app,
        refreshTokenAction = { credentials -> authApiDescription.refreshAccessToken(credentials?.refreshToken) },
        onRefreshTokenFailed = { logouter.logout() }
    )

    val callAdapterFactory: RetroceptorCallAdapterFactory = RetroceptorCallAdapterFactory(OAuthRefreshCallInterceptor(oAuthManager))

    val logouter = Logouter(app)

    val retrofitBuilder: Retrofit.Builder
        get() = Retrofit.Builder()
            .baseUrl(RESTMockServer.getUrl())
            .addConverterFactory(MoshiConverterFactory.create())

    val authApiDescription = retrofitBuilder
        .addCallAdapterFactory(callAdapterFactory)
        .build()
        .create(AuthApiDescription::class.java)

    val apiDescription: ApiDescription = retrofitBuilder
        .client(
            OkHttpClient.Builder()
                .addNetworkInterceptor(oAuthManager.provideAuthInterceptor())
                .build()
        )
        .addCallAdapterFactory(callAdapterFactory)
        .build()
        .create(ApiDescription::class.java)

    val apiInteractor: ApiInteractor = ApiInteractorImpl(oAuthManager, apiDescription, authApiDescription)

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when (modelClass) {
            LoginViewModel::class.java -> LoginViewModel(apiInteractor)
            DetailViewModel::class.java -> DetailViewModel(apiInteractor, oAuthManager, logouter)
            else -> throw IllegalArgumentException("No ViewModel registered for $modelClass")
        } as T
    }
}
