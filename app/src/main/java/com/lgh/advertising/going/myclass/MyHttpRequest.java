package com.lgh.advertising.going.myclass;

import com.lgh.advertising.going.mybean.LatestMessage;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.HTTP;

public interface MyHttpRequest {

    @HTTP(method = "GET", path = "https://api.github.com/repos/LGH1996/ADGORELEASE/releases/latest")
    Observable<LatestMessage> getLatestMessage();

    @HTTP(method = "GET", path = "https://gitee.com/lingh1996/ADGO/raw/master/shareContent")
    Observable<String> getShareContent();

    @HTTP(method = "GET", path = "https://gitee.com/lingh1996/ADGO/raw/master/privacyAgreement")
    Observable<String> getPrivacyAgreement();

}
