package activities.Retrofit;

import models.FCMBody;
import models.FCMResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMApi {
    //Firebase cloud Messaging

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAApHClE1g:APA91bGQclUN0iT-Pi2M0qh3FKRQAm43cc_e7DxWGTEw77twKrcqjNyIT7e3xBELVZ6LKwNBrITduxjjBTTtQW5mx1kez1XtFZnhCLQR0uw3-8RYnO5MbqvbT9CMU36TdD9Zujp4l_16"
    })
    @POST("fcm/send")
    Call<FCMResponse> send(@Body FCMBody body);

}
