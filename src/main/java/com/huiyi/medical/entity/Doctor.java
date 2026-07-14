package com.huiyi.medical.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data @TableName("doctor")
public class Doctor {
    @TableId(type = IdType.AUTO) private Long id;
    private String name;
    private Integer age;
    private String gender;
    private String hospital;
    private String phone;
    private String department;
    private String title;
    private LocalDateTime createTime;
}
