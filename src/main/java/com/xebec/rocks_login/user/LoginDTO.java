package com.xebec.rocks_login.user;

import lombok.NonNull;

public record LoginDTO (@NonNull String username,
                       @NonNull String password){
}
