package com.huiyi.medical.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data @TableName("enterprise")
public class Enterprise {
    @TableId(type = IdType.AUTO) private Long id;
    private String name;
    private String type;
    private String contact;
    private String phone;
    private String address;
    private LocalDateTime createTime;
}
