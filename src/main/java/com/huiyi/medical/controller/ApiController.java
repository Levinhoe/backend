package com.huiyi.medical.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.huiyi.medical.config.TokenStore;
import com.huiyi.medical.common.PageResult;
import com.huiyi.medical.common.Result;
import com.huiyi.medical.entity.*;
import com.huiyi.medical.mapper.*;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ApiController {

    @Resource private SysUserMapper sysUserMapper;
    @Resource private DoctorMapper doctorMapper;
    @Resource private PharmaceuticalCompanyMapper companyMapper;
    @Resource private CompanyPolicyMapper policyMapper;
    @Resource private CityMapper cityMapper;
    @Resource private SalesLocationMapper salesLocationMapper;
    @Resource private DrugMapper drugMapper;
    @Resource private EssentialMaterialMapper materialMapper;
    @Resource private EnterpriseMapper enterpriseMapper;
    @Resource private PartnerMapper partnerMapper;
    @Resource private AnnouncementMapper announcementMapper;
    @Resource private TokenStore tokenStore;

    private String md5(String s) {
        return DigestUtils.md5DigestAsHex(s.getBytes(StandardCharsets.UTF_8));
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getUsername, body.get("username"))
            .eq(SysUser::getPassword, md5(body.get("password"))));
        if (user == null) return Result.error("用户名或密码错误");
        String token = tokenStore.createToken(user.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("realName", user.getRealName());
        data.put("role", user.getRole());
        return Result.success(data);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader(value = "Authorization", required = false) String token) {
        if (StringUtils.hasText(token)) tokenStore.remove(token);
        return Result.success();
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        Map<String, Object> data = new HashMap<>();
        data.put("doctorCount", doctorMapper.selectCount(null));
        data.put("drugCount", drugMapper.selectCount(null));
        data.put("companyCount", companyMapper.selectCount(null));
        data.put("locationCount", salesLocationMapper.selectCount(null));
        data.put("materialCount", materialMapper.selectCount(null));
        data.put("enterpriseCount", enterpriseMapper.selectCount(null));
        data.put("titleChart", doctorMapper.countByTitle());
        data.put("departmentChart", doctorMapper.countByDepartment());
        data.put("latestPolicies", policyMapper.selectList(
            new LambdaQueryWrapper<CompanyPolicy>().orderByDesc(CompanyPolicy::getPublishDate).last("LIMIT 5")));
        data.put("lowStockDrugs", drugMapper.selectList(
            new LambdaQueryWrapper<Drug>().lt(Drug::getStock, 1000).orderByAsc(Drug::getStock).last("LIMIT 5")));
        data.put("announcements", announcementMapper.selectList(
            new LambdaQueryWrapper<Announcement>().eq(Announcement::getStatus, 1)
                .orderByDesc(Announcement::getPublishDate).last("LIMIT 3")));
        return Result.success(data);
    }

    // ===== 用户管理 =====
    @GetMapping("/user/page")
    public Result<PageResult<SysUser>> userPage(@RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "5") int size, @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) w.like(SysUser::getUsername, keyword).or().like(SysUser::getRealName, keyword);
        Page<SysUser> page = sysUserMapper.selectPage(new Page<>(current, size), w.orderByDesc(SysUser::getId));
        page.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(new PageResult<>(page.getRecords(), page.getTotal(), current, size));
    }

    @GetMapping("/user/{id}")
    public Result<SysUser> getUser(@PathVariable Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user != null) user.setPassword(null);
        return Result.success(user);
    }

    @PostMapping("/user")
    public Result<Void> addUser(@RequestBody SysUser user) {
        if (sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, user.getUsername())) != null)
            return Result.error("用户名已存在");
        user.setPassword(md5(StringUtils.hasText(user.getPassword()) ? user.getPassword() : "123456"));
        sysUserMapper.insert(user);
        return Result.success();
    }

    @PutMapping("/user")
    public Result<Void> updateUser(@RequestBody SysUser user) {
        if (StringUtils.hasText(user.getPassword())) user.setPassword(md5(user.getPassword()));
        else { SysUser e = sysUserMapper.selectById(user.getId()); user.setPassword(e.getPassword()); }
        sysUserMapper.updateById(user);
        return Result.success();
    }

    @DeleteMapping("/user/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) { sysUserMapper.deleteById(id); return Result.success(); }

    @PutMapping("/user/{id}/reset-password")
    public Result<Void> resetUserPassword(@PathVariable Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) return Result.error("用户不存在");
        user.setPassword(md5("123456"));
        sysUserMapper.updateById(user);
        return Result.success();
    }

    @PutMapping("/user/change-password")
    public Result<Void> changePassword(@RequestBody Map<String, String> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) return Result.error("用户不存在");
        if (!user.getPassword().equals(md5(body.get("oldPassword")))) return Result.error("原密码错误");
        user.setPassword(md5(body.get("newPassword")));
        sysUserMapper.updateById(user);
        return Result.success();
    }

    // ===== 医师管理 =====
    @GetMapping("/doctor/{id}")
    public Result<Doctor> getDoctor(@PathVariable Long id) { return Result.success(doctorMapper.selectById(id)); }

    @GetMapping("/doctor/page")
    public Result<PageResult<Doctor>> doctorPage(@RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "5") int size, @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String department) {
        LambdaQueryWrapper<Doctor> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            w.and(q -> q.like(Doctor::getName, keyword).or().like(Doctor::getPhone, keyword)
                .or().like(Doctor::getHospital, keyword));
        }
        if (StringUtils.hasText(department)) w.eq(Doctor::getDepartment, department);
        Page<Doctor> page = doctorMapper.selectPage(new Page<>(current, size), w.orderByDesc(Doctor::getId));
        return Result.success(new PageResult<>(page.getRecords(), page.getTotal(), current, size));
    }

    @PostMapping("/doctor") public Result<Void> addDoctor(@RequestBody Doctor d) { doctorMapper.insert(d); return Result.success(); }
    @PutMapping("/doctor") public Result<Void> updateDoctor(@RequestBody Doctor d) { doctorMapper.updateById(d); return Result.success(); }
    @DeleteMapping("/doctor/{id}") public Result<Void> deleteDoctor(@PathVariable Long id) { doctorMapper.deleteById(id); return Result.success(); }

    @PutMapping("/doctor/{id}/reset-password")
    public Result<Void> resetDoctorPassword(@PathVariable Long id) {
        Doctor doctor = doctorMapper.selectById(id);
        if (doctor == null) return Result.error("医师不存在");
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getRealName, doctor.getName()));
        if (user == null) {
            user = new SysUser();
            user.setUsername("doc" + doctor.getId());
            user.setPassword(md5("123456"));
            user.setRealName(doctor.getName());
            user.setRole("doctor");
            sysUserMapper.insert(user);
        } else {
            user.setPassword(md5("123456"));
            sysUserMapper.updateById(user);
        }
        return Result.success();
    }

    @GetMapping("/doctor/departments")
    public Result<List<String>> doctorDepartments() {
        return Result.success(doctorMapper.selectList(null).stream()
            .map(Doctor::getDepartment).distinct().collect(Collectors.toList()));
    }

    // ===== 医药公司 =====
    @GetMapping("/company/list")
    public Result<List<PharmaceuticalCompany>> companyList() {
        return Result.success(companyMapper.selectList(new LambdaQueryWrapper<PharmaceuticalCompany>().orderByDesc(PharmaceuticalCompany::getId)));
    }

    @GetMapping("/company/{id}")
    public Result<PharmaceuticalCompany> getCompany(@PathVariable Long id) { return Result.success(companyMapper.selectById(id)); }

    @GetMapping("/company/page")
    public Result<PageResult<PharmaceuticalCompany>> companyPage(@RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "5") int size, @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<PharmaceuticalCompany> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) w.like(PharmaceuticalCompany::getName, keyword);
        Page<PharmaceuticalCompany> page = companyMapper.selectPage(new Page<>(current, size), w.orderByDesc(PharmaceuticalCompany::getId));
        return Result.success(new PageResult<>(page.getRecords(), page.getTotal(), current, size));
    }
    @PostMapping("/company") public Result<Void> addCompany(@RequestBody PharmaceuticalCompany c) { companyMapper.insert(c); return Result.success(); }
    @PutMapping("/company") public Result<Void> updateCompany(@RequestBody PharmaceuticalCompany c) { companyMapper.updateById(c); return Result.success(); }
    @DeleteMapping("/company/{id}") public Result<Void> deleteCompany(@PathVariable Long id) { companyMapper.deleteById(id); return Result.success(); }

    // ===== 政策 =====
    @GetMapping("/policy/page")
    public Result<PageResult<Map<String, Object>>> policyPage(@RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "5") int size, @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long companyId) {
        LambdaQueryWrapper<CompanyPolicy> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) w.like(CompanyPolicy::getTitle, keyword);
        if (companyId != null) w.eq(CompanyPolicy::getCompanyId, companyId);
        Page<CompanyPolicy> page = policyMapper.selectPage(new Page<>(current, size), w.orderByDesc(CompanyPolicy::getId));
        List<Map<String, Object>> records = page.getRecords().stream().map(p -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", p.getId()); m.put("companyId", p.getCompanyId());
            m.put("title", p.getTitle()); m.put("content", p.getContent());
            m.put("publishDate", p.getPublishDate());
            PharmaceuticalCompany c = companyMapper.selectById(p.getCompanyId());
            m.put("companyName", c != null ? c.getName() : "");
            return m;
        }).collect(Collectors.toList());
        return Result.success(new PageResult<>(records, page.getTotal(), current, size));
    }

    @GetMapping("/policy/{id}")
    public Result<Map<String, Object>> getPolicy(@PathVariable Long id) {
        CompanyPolicy p = policyMapper.selectById(id);
        if (p == null) return Result.success(null);
        Map<String, Object> m = new HashMap<>();
        m.put("id", p.getId()); m.put("companyId", p.getCompanyId());
        m.put("title", p.getTitle()); m.put("content", p.getContent());
        m.put("publishDate", p.getPublishDate());
        PharmaceuticalCompany c = companyMapper.selectById(p.getCompanyId());
        m.put("companyName", c != null ? c.getName() : "");
        return Result.success(m);
    }

    @PostMapping("/policy") public Result<Void> addPolicy(@RequestBody CompanyPolicy p) { policyMapper.insert(p); return Result.success(); }
    @PutMapping("/policy") public Result<Void> updatePolicy(@RequestBody CompanyPolicy p) { policyMapper.updateById(p); return Result.success(); }
    @DeleteMapping("/policy/{id}") public Result<Void> deletePolicy(@PathVariable Long id) { policyMapper.deleteById(id); return Result.success(); }

    // ===== 城市 =====
    @GetMapping("/city/page")
    public Result<PageResult<City>> cityPage(@RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "5") int size, @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<City> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) w.like(City::getName, keyword).or().like(City::getProvince, keyword);
        Page<City> page = cityMapper.selectPage(new Page<>(current, size), w.orderByDesc(City::getId));
        return Result.success(new PageResult<>(page.getRecords(), page.getTotal(), current, size));
    }
    @GetMapping("/city/list") public Result<List<City>> cityList() { return Result.success(cityMapper.selectList(null)); }

    @GetMapping("/city/{id}")
    public Result<City> getCity(@PathVariable Long id) { return Result.success(cityMapper.selectById(id)); }

    @PostMapping("/city") public Result<Void> addCity(@RequestBody City c) { cityMapper.insert(c); return Result.success(); }
    @PutMapping("/city") public Result<Void> updateCity(@RequestBody City c) { cityMapper.updateById(c); return Result.success(); }
    @DeleteMapping("/city/{id}") public Result<Void> deleteCity(@PathVariable Long id) { cityMapper.deleteById(id); return Result.success(); }

    // ===== 销售地点 =====
    @GetMapping("/sales/page")
    public Result<PageResult<Map<String, Object>>> salesPage(@RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "5") int size, @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long cityId) {
        LambdaQueryWrapper<SalesLocation> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) w.like(SalesLocation::getName, keyword).or().like(SalesLocation::getAddress, keyword);
        if (cityId != null) w.eq(SalesLocation::getCityId, cityId);
        Page<SalesLocation> page = salesLocationMapper.selectPage(new Page<>(current, size), w.orderByDesc(SalesLocation::getId));
        List<Map<String, Object>> records = page.getRecords().stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId()); m.put("supplierId", s.getSupplierId());
            m.put("name", s.getName()); m.put("phone", s.getPhone()); m.put("address", s.getAddress());
            m.put("cityId", s.getCityId()); m.put("longitude", s.getLongitude()); m.put("latitude", s.getLatitude());
            City city = cityMapper.selectById(s.getCityId());
            m.put("cityName", city != null ? city.getName() : "");
            return m;
        }).collect(Collectors.toList());
        return Result.success(new PageResult<>(records, page.getTotal(), current, size));
    }

    @GetMapping("/sales/{id}")
    public Result<Map<String, Object>> getSales(@PathVariable Long id) {
        SalesLocation s = salesLocationMapper.selectById(id);
        if (s == null) return Result.success(null);
        Map<String, Object> m = new HashMap<>();
        m.put("id", s.getId()); m.put("supplierId", s.getSupplierId());
        m.put("name", s.getName()); m.put("phone", s.getPhone()); m.put("address", s.getAddress());
        m.put("cityId", s.getCityId()); m.put("longitude", s.getLongitude()); m.put("latitude", s.getLatitude());
        City city = cityMapper.selectById(s.getCityId());
        m.put("cityName", city != null ? city.getName() : "");
        return Result.success(m);
    }

    @GetMapping("/sales/list")
    public Result<List<Map<String, Object>>> salesList() {
        return Result.success(salesLocationMapper.selectList(null).stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId()); m.put("supplierId", s.getSupplierId());
            m.put("name", s.getName()); m.put("phone", s.getPhone()); m.put("address", s.getAddress());
            m.put("cityId", s.getCityId()); m.put("longitude", s.getLongitude()); m.put("latitude", s.getLatitude());
            City city = cityMapper.selectById(s.getCityId());
            m.put("cityName", city != null ? city.getName() : "");
            return m;
        }).collect(Collectors.toList()));
    }
    @PostMapping("/sales") public Result<Void> addSales(@RequestBody SalesLocation s) { salesLocationMapper.insert(s); return Result.success(); }
    @PutMapping("/sales") public Result<Void> updateSales(@RequestBody SalesLocation s) { salesLocationMapper.updateById(s); return Result.success(); }
    @DeleteMapping("/sales/{id}") public Result<Void> deleteSales(@PathVariable Long id) { salesLocationMapper.deleteById(id); return Result.success(); }

    // ===== 药品 =====
    @GetMapping("/drug/categories")
    public Result<List<String>> drugCategories() {
        return Result.success(drugMapper.selectList(null).stream().map(Drug::getCategory).distinct().collect(Collectors.toList()));
    }

    @GetMapping("/drug/page")
    public Result<PageResult<Drug>> drugPage(@RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "5") int size, @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category, @RequestParam(required = false) Boolean lowStock) {
        LambdaQueryWrapper<Drug> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) w.like(Drug::getName, keyword).or().like(Drug::getManufacturer, keyword);
        if (StringUtils.hasText(category)) w.eq(Drug::getCategory, category);
        if (Boolean.TRUE.equals(lowStock)) w.lt(Drug::getStock, 1000);
        Page<Drug> page = drugMapper.selectPage(new Page<>(current, size), w.orderByDesc(Drug::getId));
        return Result.success(new PageResult<>(page.getRecords(), page.getTotal(), current, size));
    }

    @GetMapping("/drug/low-stock")
    public Result<List<Drug>> drugLowStock() {
        return Result.success(drugMapper.selectList(
            new LambdaQueryWrapper<Drug>().lt(Drug::getStock, 1000).orderByAsc(Drug::getStock)));
    }

    @GetMapping("/drug/{id}")
    public Result<Drug> getDrug(@PathVariable Long id) { return Result.success(drugMapper.selectById(id)); }
    @PostMapping("/drug") public Result<Void> addDrug(@RequestBody Drug d) { drugMapper.insert(d); return Result.success(); }
    @PutMapping("/drug") public Result<Void> updateDrug(@RequestBody Drug d) { drugMapper.updateById(d); return Result.success(); }
    @DeleteMapping("/drug/{id}") public Result<Void> deleteDrug(@PathVariable Long id) { drugMapper.deleteById(id); return Result.success(); }

    // ===== 必备材料 =====
    @GetMapping("/material/page")
    public Result<PageResult<EssentialMaterial>> materialPage(@RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "5") int size, @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<EssentialMaterial> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) w.like(EssentialMaterial::getLabel, keyword).or().like(EssentialMaterial::getContent, keyword);
        Page<EssentialMaterial> page = materialMapper.selectPage(new Page<>(current, size), w.orderByDesc(EssentialMaterial::getId));
        return Result.success(new PageResult<>(page.getRecords(), page.getTotal(), current, size));
    }

    @GetMapping("/material/{id}")
    public Result<EssentialMaterial> getMaterial(@PathVariable Long id) { return Result.success(materialMapper.selectById(id)); }

    @PostMapping("/material") public Result<Void> addMaterial(@RequestBody EssentialMaterial m) { materialMapper.insert(m); return Result.success(); }
    @PutMapping("/material") public Result<Void> updateMaterial(@RequestBody EssentialMaterial m) { materialMapper.updateById(m); return Result.success(); }
    @DeleteMapping("/material/{id}") public Result<Void> deleteMaterial(@PathVariable Long id) { materialMapper.deleteById(id); return Result.success(); }

    // ===== 企业 =====
    @GetMapping("/enterprise/page")
    public Result<PageResult<Enterprise>> enterprisePage(@RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "5") int size, @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Enterprise> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) w.like(Enterprise::getName, keyword);
        Page<Enterprise> page = enterpriseMapper.selectPage(new Page<>(current, size), w.orderByDesc(Enterprise::getId));
        return Result.success(new PageResult<>(page.getRecords(), page.getTotal(), current, size));
    }

    @GetMapping("/enterprise/{id}")
    public Result<Enterprise> getEnterprise(@PathVariable Long id) { return Result.success(enterpriseMapper.selectById(id)); }

    @PostMapping("/enterprise") public Result<Void> addEnterprise(@RequestBody Enterprise e) { enterpriseMapper.insert(e); return Result.success(); }
    @PutMapping("/enterprise") public Result<Void> updateEnterprise(@RequestBody Enterprise e) { enterpriseMapper.updateById(e); return Result.success(); }
    @DeleteMapping("/enterprise/{id}") public Result<Void> deleteEnterprise(@PathVariable Long id) { enterpriseMapper.deleteById(id); return Result.success(); }

    // ===== 合作伙伴 =====
    @GetMapping("/partner/page")
    public Result<PageResult<Partner>> partnerPage(@RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "5") int size, @RequestParam(required = false) String keyword) {
        LambdaQueryWrapper<Partner> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) w.like(Partner::getName, keyword);
        Page<Partner> page = partnerMapper.selectPage(new Page<>(current, size), w.orderByDesc(Partner::getId));
        return Result.success(new PageResult<>(page.getRecords(), page.getTotal(), current, size));
    }

    @GetMapping("/partner/{id}")
    public Result<Partner> getPartner(@PathVariable Long id) { return Result.success(partnerMapper.selectById(id)); }

    @PostMapping("/partner") public Result<Void> addPartner(@RequestBody Partner p) { partnerMapper.insert(p); return Result.success(); }
    @PutMapping("/partner") public Result<Void> updatePartner(@RequestBody Partner p) { partnerMapper.updateById(p); return Result.success(); }
    @DeleteMapping("/partner/{id}") public Result<Void> deletePartner(@PathVariable Long id) { partnerMapper.deleteById(id); return Result.success(); }

    // ===== 公告 =====
    @GetMapping("/announcement/page")
    public Result<PageResult<Announcement>> announcementPage(@RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "5") int size, @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<Announcement> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) w.like(Announcement::getTitle, keyword);
        if (status != null) w.eq(Announcement::getStatus, status);
        Page<Announcement> page = announcementMapper.selectPage(new Page<>(current, size), w.orderByDesc(Announcement::getPublishDate));
        return Result.success(new PageResult<>(page.getRecords(), page.getTotal(), current, size));
    }

    @GetMapping("/announcement/{id}")
    public Result<Announcement> getAnnouncement(@PathVariable Long id) { return Result.success(announcementMapper.selectById(id)); }

    @PostMapping("/announcement") public Result<Void> addAnnouncement(@RequestBody Announcement a) { announcementMapper.insert(a); return Result.success(); }
    @PutMapping("/announcement") public Result<Void> updateAnnouncement(@RequestBody Announcement a) { announcementMapper.updateById(a); return Result.success(); }
    @DeleteMapping("/announcement/{id}") public Result<Void> deleteAnnouncement(@PathVariable Long id) { announcementMapper.deleteById(id); return Result.success(); }

    @PutMapping("/announcement/{id}/status")
    public Result<Void> updateAnnouncementStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null) return Result.error("公告不存在");
        a.setStatus(body.get("status"));
        announcementMapper.updateById(a);
        return Result.success();
    }
}
