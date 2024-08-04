package com.xebec.rocks_login.user;

import lombok.NonNull;

public record RegisterDTO(@NonNull String username,
                          @NonNull String email,
                          @NonNull String password) {
}
