package com.cmps.ims.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SampleController {

    /**
     * ログインページ
     */
    @GetMapping("")
    public String loginIndex() {
        return "login/index";
    }

    /**
     * TOPページ(MENU)
     * 
     */
    @GetMapping("/top")
    public String topIndex() {
    	return "top/index";
    }
    
    @PostMapping("/top")
    public String topPost() {
        return "top/index";
    }
    
    /**
     * 発送管理
     */
    @GetMapping("/send")
    public String sendIndex() {
    	return "send/index";
    }
    
    /**
     * 発送登録／変更
     */
    @GetMapping("/send/entry")
    public String sendEntry() {
    	return "send/entry";
    }
    
    /**
     * 勤怠管理
     */
    @GetMapping("/attendance")
    public String attendanceIndex() {
    	return "attendance/index";
    }
    
    /**
     * ユーザマスタ
     */
    @GetMapping("/user")
    public String userIndex() {
    	return "user/index";
    }
    
    /**
     * ユーザ登録／変更
     */
    @GetMapping("/user/entry")
    public String userEntry() {
    	return "user/entry";
    }
    
    /**
     * 発注管理
     */
    @GetMapping("/place")
    public String placeIndex() {
    	return "place/index";
    }
    
    /**
     * 発注登録／変更
     */
    @GetMapping("/place/entry")
    public String placeEntry() {
    	return "place/entry";
    }
}
