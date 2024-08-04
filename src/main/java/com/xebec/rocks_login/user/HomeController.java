package com.xebec.rocks_login.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String getHome(){
        return "Home Page";
    }

    @GetMapping("/store")
    public String store(){
        return "Store Page";
    }

    @GetMapping("/admin/home")
    public String getAdminHome(){
        return "Admin Home";
    }

    @GetMapping("/client/home")
    public String getClientHome(){
        return "Client Home";
    }
}
