package com.huiyi.medical.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data @TableName("city")
public class City {
    @TableId(type = IdType.AUTO) private Long id;
    private String name;
    private String province;
    private LocalDateTime createTime;
}
