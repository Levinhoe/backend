package com.huiyi.medical.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data @TableName("essential_material")
public class EssentialMaterial {
    @TableId(type = IdType.AUTO) private Long id;
    private String label;
    private String content;
    private LocalDateTime createTime;
}
