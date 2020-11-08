package providers;

import activities.Retrofit.IFCMApi;
import activities.Retrofit.RetrofitResi;
import models.FCMBody;
import models.FCMResponse;
import retrofit2.Call;

public class NotificationProvider {

    private String url = "https://fcm.googleapis.com";

    public NotificationProvider() {
    }

    public Call<FCMResponse> sendNotification(FCMBody body) {
        return RetrofitResi.getResiObject(url).create(IFCMApi.class).send(body);
    }
}
