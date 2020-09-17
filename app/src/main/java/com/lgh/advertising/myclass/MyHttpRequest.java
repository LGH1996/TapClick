package com.lgh.advertising.myclass;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;

public interface MyHttpRequest {
    @GET("repos/LGH1996/ADGORELEASE/releases/latest")
    Observable<LatestMessage> getLatestMessage();
}
