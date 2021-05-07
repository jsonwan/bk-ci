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

package com.tencent.devops.common.pipeline.utils

import com.tencent.devops.common.pipeline.Model
import com.tencent.devops.common.pipeline.container.Container
import com.tencent.devops.common.pipeline.container.NormalContainer
import com.tencent.devops.common.pipeline.container.TriggerContainer
import com.tencent.devops.common.pipeline.container.VMBuildContainer
import com.tencent.devops.common.pipeline.enums.JobRunCondition
import com.tencent.devops.common.pipeline.option.JobControlOption
import com.tencent.devops.common.pipeline.pojo.element.Element
import com.tencent.devops.common.pipeline.pojo.element.RunCondition
import com.tencent.devops.common.pipeline.pojo.element.trigger.ManualTriggerElement
import com.tencent.devops.common.pipeline.pojo.element.trigger.RemoteTriggerElement

/**
 *
 * @version 1.0
 */
object ModelUtils {

    /**
     * 兼容旧的数据结构
     */
    @Suppress("ALL")
    fun initContainerOldData(c: Container) {
        if (c is NormalContainer) {
            if (c.jobControlOption == null) {

                c.jobControlOption = JobControlOption(
                    enable = true,
                    timeout = c.maxRunningMinutes,
                    runCondition = if (c.enableSkip == true) {
                        if (c.conditions?.isNotEmpty() == true) {
                            JobRunCondition.CUSTOM_VARIABLE_MATCH_NOT_RUN
                        } else {
                            JobRunCondition.STAGE_RUNNING
                        }
                    } else {
                        JobRunCondition.STAGE_RUNNING
                    },
                    customVariables = c.conditions
                )
            }
        } else if (c is VMBuildContainer) {
            if (c.jobControlOption == null) {
                c.jobControlOption = JobControlOption(
                    enable = true,
                    timeout = c.maxRunningMinutes,
                    runCondition = JobRunCondition.STAGE_RUNNING
                )
            }
        }
    }

    fun canManualStartup(triggerContainer: TriggerContainer): Boolean {
        triggerContainer.elements.forEach {
            if (it is ManualTriggerElement && it.isElementEnable()) {
                return true
            }
        }
        return false
    }

    fun canRemoteStartup(triggerContainer: TriggerContainer): Boolean {
        triggerContainer.elements.forEach {
            if (it is RemoteTriggerElement && it.isElementEnable()) {
                return true
            }
        }
        return false
    }

    fun refreshCanRetry(model: Model, canRetry: Boolean) {
        model.stages.forEach { s ->
            s.canRetry = (s.canRetry ?: false) && canRetry
            s.containers.forEach { c ->
                initContainerOldData(c)
                if (c is VMBuildContainer) {
                    c.canRetry = (c.canRetry ?: false) && canRetry
                }

                val failElements = mutableListOf<Element>()
                c.elements.forEach { e ->
                    refreshElement(element = e, canRetry = canRetry, failElements = failElements)
                }
            }
        }
    }

    private fun refreshElement(element: Element, canRetry: Boolean, failElements: MutableList<Element>) {

        element.canRetry = (element.canRetry ?: false) && canRetry

        val additionalOptions = element.additionalOptions
        if (additionalOptions != null && additionalOptions.enable) {
            if (additionalOptions.continueWhenFailed) {
                element.canRetry = false
            } else if (additionalOptions.runCondition == RunCondition.PRE_TASK_FAILED_BUT_CANCEL ||
                additionalOptions.runCondition == RunCondition.PRE_TASK_FAILED_ONLY
            ) {
                // 前面有失败的插件时也要运行的插件，将前面的失败插件置为不可重试
                element.canRetry = false
                failElements.forEach {
                    it.canRetry = false
                }
            }
        }
        if (element.canRetry == true) { // 先记录可重试的执行失败插件
            failElements.add(element)
        }
    }
}
