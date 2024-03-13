package com.ltz.emplInfo.sys.controller;

import com.ltz.emplInfo.common.vo.Result;
import com.ltz.emplInfo.sys.entity.EmplInfo;
import com.ltz.emplInfo.sys.entity.Graduate;
import com.ltz.emplInfo.sys.entity.TotalTable;
import com.ltz.emplInfo.sys.service.IEmplInfoService;
import com.ltz.emplInfo.sys.service.IGraduateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author tianzhi
 * @since 2024-03-09
 */
@RestController
@RequestMapping("/api/emplInfo")
public class EmplInfoController {
    @Autowired
    private IEmplInfoService emplInfoService;

    @Autowired
    private IGraduateService graduateService;

    @GetMapping("/getAllUserByPage")
    public Result<List<EmplInfo>> getAllEmplInfoByPage(@RequestParam("pageNum") int pageNum, @RequestParam("pageSize") int pageSize) {
        List<EmplInfo> allEmplInfos = emplInfoService.list();
        for (EmplInfo info : allEmplInfos) {
            convertEmplInfo(info);
        }
        // 计算当前页的起始索引和结束索引
        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, allEmplInfos.size());
        List<EmplInfo> currentPageEmplInfos = allEmplInfos.subList(startIndex, endIndex);
        Result<List<EmplInfo>> result = new Result<>();

        // result.setData(allEmplInfos);   // 全部
        result.setData1(currentPageEmplInfos);  // 单页
        result.setTotal(allEmplInfos.size());
        result.setMessage("data1为第 " + pageNum + " 页的结果，data为全部结果");

        return result;
    }

    @GetMapping("/getUserBySearch")
    public Result<List<EmplInfo>> getEmplInfoBySearch(@RequestParam("keyword") String keyword) {
        List<EmplInfo> list = emplInfoService.getEmplInfoBySearch(keyword);
        Result<List<EmplInfo>> result = new Result<>();
        for (EmplInfo info : list) {
            convertEmplInfo(info);
        }
        result.setData(list);
        result.setTotal(list.size());
        result.setMessage("找到了");
        return result;
    }

    @PostMapping("/addUser")
    public Result<String> addEmplInfo(@RequestBody EmplInfo emplInfo) {
        convertEmplInfo(emplInfo);
        boolean added = emplInfoService.add(emplInfo);
        if (added) {
            return Result.success("添加就业信息成功");
        } else {
            return Result.fail("添加就业信息失败");
        }
    }

    @DeleteMapping("/deleteUser/{id}")
    public Result<String> deleteEmplInfo(@PathVariable("id") String id) {
        boolean deleted = emplInfoService.deleteById(id);
        if (deleted) {
            return Result.success("删除就业信息成功");
        } else {
            return Result.fail("删除就业信息失败，可能该就业信息不存在");
        }
    }

    @DeleteMapping("/deleteAllUser/{ids}")
    public Result<String> deleteAllEmplInfo(@PathVariable("ids") List<String> ids) {
        boolean deleted = emplInfoService.deleteAllByIds(ids);
        if (deleted) {
            return Result.success("删除成功");
        } else {
            return Result.fail("删除失败，可能就业信息不存在");
        }
    }

    @PutMapping("/editUser")
    public Result<String> editEmplInfo(@RequestBody EmplInfo emplInfo) {
        convertEmplInfo(emplInfo);
        boolean updated = emplInfoService.editById(emplInfo);
        if (updated) {
            return Result.success("更新就业信息成功");
        } else {
            return Result.fail("更新就业信息失败，可能该就业信息不存在");
        }
    }

    @GetMapping("/byDept")
    public List<TotalTable> getByDeptTable(@RequestParam("grade") String grade) {
        List<TotalTable> byDept = new ArrayList<>();

        // 由于数据库中的院系信息存在重复，需要先去重
        List<String> list = new ArrayList<>();
        for (Graduate graduate:graduateService.list()) {
            String department = graduate.getDepartment();
            list.add(department);
        }
        HashSet<String> set = new HashSet<>(list);

        // 保留三位小数
        DecimalFormat df = new DecimalFormat("#.###");
        // 四舍五入
        df.setRoundingMode(RoundingMode.HALF_UP);

        for (String department:new ArrayList<>(set)) {
            TotalTable byDeptTable = new TotalTable();
            // 院系
            byDeptTable.setDepartments(department);
            // 毕业生总数
            int gradeCount = emplInfoService.countOfGraduate(department, "", grade);
            byDeptTable.setCountOfGraduate(String.valueOf(gradeCount));
            // 已就业
            int emplCount = emplInfoService.countOfEmployed(department, "", grade);
            byDeptTable.setCountOfEmployed(String.valueOf(emplCount));
            // 计算就业率
            byDeptTable.setEmploymentRate(df.format((double) emplCount / gradeCount));

            byDept.add(byDeptTable);
        }

        return byDept;
    }

    @GetMapping("/byMajor")
    public List<TotalTable> getByMajorTable(@RequestParam("department") String department, @RequestParam("grade") String grade) {
        List<Graduate> all = graduateService.getDeptGraduateBySearch(department);
        List<TotalTable> byMajor = new ArrayList<>();

        // 由于数据库中的院系信息存在重复，需要先去重
        List<String> list = new ArrayList<>();
        for (Graduate graduate:all) {
            String major = graduate.getMajor();
            list.add(major);
        }
        HashSet<String> set = new HashSet<>(list);

        // 保留三位小数
        DecimalFormat df = new DecimalFormat("#.###");
        // 四舍五入
        df.setRoundingMode(RoundingMode.HALF_UP);

        for (String major: new ArrayList<>(set)) {
            TotalTable byMajorTable = new TotalTable();
            // 专业
            byMajorTable.setMajors(major);
            // 毕业生总数
            int gradeCount = emplInfoService.countOfGraduate(department, major, grade);
            byMajorTable.setCountOfGraduate(String.valueOf(gradeCount));
            // 已就业
            int emplCount = emplInfoService.countOfEmployed(department, major, grade);
            byMajorTable.setCountOfEmployed(String.valueOf(emplCount));
            // 计算就业率
            byMajorTable.setEmploymentRate(df.format((double) emplCount / gradeCount));

            byMajor.add(byMajorTable);
        }

        return byMajor;
    }

    // Dept和Major共用分页
    @GetMapping("/byDeptByPage")
    public Result<List<TotalTable>> getByDeptTableByPage(@RequestParam("department") String department, @RequestParam("grade") String grade, @RequestParam("pageNum") int pageNum, @RequestParam("pageSize") int pageSize) {
        List<TotalTable> all = new ArrayList<>();
        if (department != null && !department.equals("")) {
            all = getByMajorTable(department, grade);
        } else {
            all = getByDeptTable(grade);
        }
        // 计算当前页的起始索引和结束索引
        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, all.size());
        Result<List<TotalTable>> result = new Result<>();

        result.setData1(all.subList(startIndex, endIndex));  // 单页
        result.setTotal(all.size());
        result.setMessage("data1为第 " + pageNum + " 页的结果，data为全部结果");

        return result;
    }

    public static void convertEmplInfo(EmplInfo emplInfo) {
        // if是"1 or 0"?执行转换"1,0"为"是,否"   else执行转换"是,否"为"1,0"
        if (Objects.equals(emplInfo.getPostgraduate(), "1") || Objects.equals(emplInfo.getPostgraduate(), "0")) {
            // postgraduate转换
            if (Objects.equals(emplInfo.getPostgraduate(), "1")) {
                emplInfo.setPostgraduate("是");
            } else if (Objects.equals(emplInfo.getPostgraduate(), "0")) {
                emplInfo.setPostgraduate("否");
            } else {
                emplInfo.setPostgraduate("");
            }
            // emplOntime转换
            if (Objects.equals(emplInfo.getEmplOntime(), "1")) {
                emplInfo.setEmplOntime("是");
            } else if (Objects.equals(emplInfo.getEmplOntime(), "0")) {
                emplInfo.setEmplOntime("否");
            } else {
                emplInfo.setEmplOntime("");
            }
            // emplWithintwo转换
            if (Objects.equals(emplInfo.getEmplWithintwo(), "1")) {
                emplInfo.setEmplWithintwo("是");
            } else if (Objects.equals(emplInfo.getEmplWithintwo(), "0")) {
                emplInfo.setEmplWithintwo("否");
            } else {
                emplInfo.setEmplWithintwo("");
            }
        } else {
            // postgraduate转换
            if (Objects.equals(emplInfo.getPostgraduate(), "是")) {
                emplInfo.setPostgraduate("1");
            } else if (Objects.equals(emplInfo.getPostgraduate(), "否")) {
                emplInfo.setPostgraduate("0");
            } else {
                emplInfo.setPostgraduate("");
            }
            // emplOntime转换
            if (Objects.equals(emplInfo.getEmplOntime(), "是")) {
                emplInfo.setEmplOntime("1");
            } else if (Objects.equals(emplInfo.getEmplOntime(), "否")) {
                emplInfo.setEmplOntime("0");
            } else {
                emplInfo.setEmplOntime("");
            }
            // emplWithintwo转换
            if (Objects.equals(emplInfo.getEmplWithintwo(), "是")) {
                emplInfo.setEmplWithintwo("1");
            } else if (Objects.equals(emplInfo.getEmplWithintwo(), "否")) {
                emplInfo.setEmplWithintwo("0");
            } else {
                emplInfo.setEmplWithintwo("");
            }
        }
    }

}