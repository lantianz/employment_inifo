package com.ltz.emplInfo.sys.controller;

import com.ltz.emplInfo.common.vo.Result;
import com.ltz.emplInfo.sys.entity.Admin;
import com.ltz.emplInfo.sys.service.IAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author tianzhi
 * @since 2024-02-04
 */
@RestController
@RequestMapping("/api/user")
public class AdminController {
    @Autowired
    private IAdminService adminService;

    @GetMapping("/all")
    public Result<List<Admin>> getAllAdmin(){
        List<Admin> list = adminService.list();
        return Result.success(list,"success!!!");
    }

    @PostMapping("/login")
    public Result<Map<String,Object>> login(@RequestBody Admin admin){
        Map<String,Object> data = adminService.login(admin);
        if(data != null){
            return Result.success(data);
        }
        return Result.fail(20002,"用户名or密码错误");
    }

    @GetMapping("/info")
    public Result<?> getAdminInfo(@RequestParam("token") String token){
        // 根据token获取用户信息，redis
        Map<String, Object> data = adminService.getAdminInfo(token);
        if(data!=null){
            return Result.success(data);
        }
        return Result.fail(20003,"获取信息失败");
    }

    @PostMapping("/logout")
    public Result<?> logout(@RequestHeader("X-Token") String token){
        adminService.logout(token);
        return Result.success();
    }

}
