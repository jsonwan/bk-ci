/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *
 * A copy of the MIT License is included in this file.
 *
 *
 * Terms of the MIT License:
 * ---------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tencent.devops.common.log.pojo

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

/**
 *
 * Powered By Tencent
 */
@ApiModel("日志模型")
data class LogLine(
    @ApiModelProperty("日志行号", required = true)
    val lineNo: Long,
    @ApiModelProperty("日志时间戳", required = true)
    val timestamp: Long,
    @ApiModelProperty("日志消息体", required = true)
    val message: String,
    @ApiModelProperty("日志权重级", required = true)
    val priority: Byte = 0,
    @ApiModelProperty("日志tag", required = true)
    val tag: String = "",
    @ApiModelProperty("日志子tag", required = true)
    val subTag: String = "",
    @ApiModelProperty("日志jobId", required = true)
    val jobId: String = "",
    @ApiModelProperty("日志containerHashId", required = true)
    val containerHashId: String?,
    @ApiModelProperty("日志stepId", required = true)
    val stepId: String?,
    @ApiModelProperty("日志执行次数", required = true)
    val executeCount: Int? = 1
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        return lineNo == (other as LogLine).lineNo
    }

    override fun hashCode(): Int {
        return (lineNo xor lineNo.ushr(32)).toInt()
    }
}
