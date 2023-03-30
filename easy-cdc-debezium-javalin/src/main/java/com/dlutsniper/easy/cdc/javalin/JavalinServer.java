package com.dlutsniper.easy.cdc.javalin;

import io.javalin.Javalin;
import io.javalin.http.Context;

public class JavalinServer {
    public void start() {
        Javalin app = Javalin.create();
        app = app.start(8082);
        app.get("/getInfo", this::getInfo);
    }

    private void getInfo(Context context) {
        context.json(context.status());
    }
}
