package com.example.fileserver.core;

import com.yanzhenjie.andserver.annotation.Controller;
import com.yanzhenjie.andserver.annotation.GetMapping;

/**
 * Created on 2022/11/17
 *
 * @author binlee
 */
@Controller
public class ForwardController {

  @GetMapping(path = "/")
  public String forward() {
    return "forward:/index.html";
  }
}
