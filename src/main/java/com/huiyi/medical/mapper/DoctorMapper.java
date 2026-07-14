package com.huiyi.medical.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huiyi.medical.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {
    @Select("SELECT title AS name, COUNT(*) AS value FROM doctor GROUP BY title")
    List<Map<String, Object>> countByTitle();

    @Select("SELECT department AS name, COUNT(*) AS value FROM doctor GROUP BY department")
    List<Map<String, Object>> countByDepartment();
}
