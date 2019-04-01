package cz.ackee.sample.model.rest

import cz.ackee.sample.model.SampleItem
import kotlinx.coroutines.Deferred
import retrofit2.http.GET

/**
 * Api description simulating Retrofit description
 */
interface ApiDescription {

    @GET("items")
    fun getData(): Deferred<List<SampleItem>>
}
