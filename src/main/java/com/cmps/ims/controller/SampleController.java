package com.cmps.ims.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SampleController {

	@GetMapping("/login")
	public String loginIndex() {

		return "login/index";
	}

	/**
	 * TOPページ(MENU)
	 * 
	 * @return
	 */
	@GetMapping("/top")
	public String topIndex() {
		return "top/index";
	}
	
	@PostMapping("/top")
	public String topPost() {
		return "top/index";
	}
}