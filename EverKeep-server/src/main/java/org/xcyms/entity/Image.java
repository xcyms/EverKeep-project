package org.xcyms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Getter;
import lombok.Setter;
import org.xcyms.common.annotation.ApiDocProperty;
import org.xcyms.common.enums.YesNoEnum;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 图片表
 * </p>
 *
 * @author liu-xu
 * @since 2026-01-11
 */
@Getter
@Setter
@TableName("biz_image")
public class Image implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiDocProperty("图片ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiDocProperty("用户ID")
    @TableField("user_id")
    private Long userId;

    @ApiDocProperty("相册ID")
    @TableField("album_id")
    private Long albumId;

    @ApiDocProperty("图片URL")
    @TableField("url")
    private String url;

    @ApiDocProperty("缩略图URL")
    @TableField("thumbnail_url")
    private String thumbnailUrl;

    @ApiDocProperty("图片名称")
    @TableField("name")
    private String name;

    @ApiDocProperty("文件大小(Byte)")
    @TableField("size")
    private Long size;

    @ApiDocProperty("文件类型")
    @TableField("type")
    private String type;

    @ApiDocProperty("状态 (YES-发布, NO-私有)")
    @TableField("status")
    private YesNoEnum status;

    @ApiDocProperty("创建时间")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @ApiDocProperty("更新时间")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @ApiDocProperty("删除标志 (YES-已删除, NO-未删除)")
    @TableField("deleted")
    @TableLogic
    private YesNoEnum deleted;

    @ApiDocProperty("相机制造厂商")
    @TableField("make")
    private String make;

    @ApiDocProperty("相机型号")
    @TableField("model")
    private String model;

    @ApiDocProperty("曝光时间")
    @TableField("exposure_time")
    private String exposureTime;

    @ApiDocProperty("光圈值")
    @TableField("f_number")
    private String fNumber;

    @ApiDocProperty("ISO感光度")
    @TableField("iso")
    private String iso;

    @ApiDocProperty("焦距")
    @TableField("focal_length")
    private String focalLength;

    @ApiDocProperty("镜头型号")
    @TableField("lens_model")
    private String lensModel;

    @ApiDocProperty("纬度")
    @TableField("lat")
    private String lat;

    @ApiDocProperty("经度")
    @TableField("lng")
    private String lng;

    @ApiDocProperty("拍摄时间")
    @TableField("take_time")
    private LocalDateTime takeTime;
}