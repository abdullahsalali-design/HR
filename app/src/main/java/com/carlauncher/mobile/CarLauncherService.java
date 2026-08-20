package com.carlauncher.mobile;

import androidx.annotation.NonNull;
import androidx.car.app.CarAppService;
import androidx.car.app.HostValidator;
import androidx.car.app.Session;

public class CarLauncherService extends CarAppService {
    @NonNull
    @Override
    public HostValidator createHostValidator() {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR;
    }

    @NonNull
    @Override
    public Session onCreateSession() {
        return new CarLauncherSession();
    }
}
