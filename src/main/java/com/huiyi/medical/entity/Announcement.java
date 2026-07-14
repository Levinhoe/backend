package com.huiyi.medical.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @TableName("announcement")
public class Announcement {
    @TableId(type = IdType.AUTO) private Long id;
    private String title;
    private String content;
    private LocalDate publishDate;
    private Integer status;
    private LocalDateTime createTime;
}
