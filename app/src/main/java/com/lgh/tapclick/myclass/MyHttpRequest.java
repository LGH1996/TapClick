package com.lgh.tapclick.myclass;

import com.lgh.tapclick.mybean.LatestMessage;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.HTTP;

public interface MyHttpRequest {
    @HTTP(method = "GET", path = "https://api.github.com/repos/LGH1996/TapClick/releases/latest")
    Observable<LatestMessage> getLatestMessage();

    @HTTP(method = "GET", path = "https://gitee.com/lingh1996/TapClick/raw/master/shareContent")
    Observable<String> getShareContent();

    @HTTP(method = "GET", path = "https://gitee.com/lingh1996/TapClick/raw/master/privacyAgreement")
    Observable<String> getPrivacyAgreement();

    @HTTP(method = "GET", path = "https://gitee.com/lingh1996/TapClick/raw/master/accessibilityStatement")
    Observable<String> getAccessibilityStatement();
}
