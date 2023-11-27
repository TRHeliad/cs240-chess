package handler;

import com.google.gson.Gson;
import service.ApplicationService;
import spark.Request;
import spark.Response;

public class ApplicationHandler {

    private static final ApplicationHandler applicationHandler = new ApplicationHandler();

    public static ApplicationHandler getInstance() {
        return applicationHandler;
    }

    public Object handleClear(Request request, Response response) {
        return new Gson().toJson(ApplicationService.getInstance().clearApplication());
    }
}
