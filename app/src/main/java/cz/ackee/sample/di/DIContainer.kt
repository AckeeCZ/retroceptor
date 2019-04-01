package cz.ackee.sample.di

import com.squareup.moshi.Moshi
import cz.ackee.ackroutine.CoroutineOAuthManager
import cz.ackee.ackroutine.OAuthCallInterceptor
import cz.ackee.retrofitadapter.AckroutineCallAdapterFactory
import cz.ackee.sample.App
import cz.ackee.sample.interactor.ApiInteractorImpl
import cz.ackee.sample.interactor.IApiInteractor
import cz.ackee.sample.model.Logouter
import cz.ackee.sample.model.rest.ApiDescription
import cz.ackee.sample.model.rest.AuthApiDescription
import io.appflate.restmock.RESTMockServer
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Simple DI container that provides dependencies
 */
class DIContainer(val app: App) {

    val oAuthManager = CoroutineOAuthManager(
        context = app,
        refreshTokenAction = { authApiDescription.refreshAccessToken(it) },
        onRefreshTokenFailed = { logouter.logout() }
    )

    val callAdapterFactory: AckroutineCallAdapterFactory = AckroutineCallAdapterFactory(OAuthCallInterceptor(oAuthManager))

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
            .build())
        .addCallAdapterFactory(callAdapterFactory)
        .build()
        .create(ApiDescription::class.java)

    val apiInteractor: IApiInteractor = ApiInteractorImpl(oAuthManager, apiDescription, authApiDescription)
}
