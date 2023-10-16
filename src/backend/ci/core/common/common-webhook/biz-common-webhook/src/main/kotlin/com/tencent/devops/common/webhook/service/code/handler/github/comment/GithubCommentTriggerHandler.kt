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

package com.tencent.devops.common.webhook.service.code.handler.github.comment

import com.tencent.devops.common.pipeline.pojo.element.trigger.enums.CodeEventType
import com.tencent.devops.common.webhook.pojo.code.BK_REPO_GIT_WEBHOOK_NOTE_AUTHOR_ID
import com.tencent.devops.common.webhook.pojo.code.BK_REPO_GIT_WEBHOOK_NOTE_COMMENT
import com.tencent.devops.common.webhook.pojo.code.BK_REPO_GIT_WEBHOOK_NOTE_CREATED_AT
import com.tencent.devops.common.webhook.pojo.code.BK_REPO_GIT_WEBHOOK_NOTE_ID
import com.tencent.devops.common.webhook.pojo.code.BK_REPO_GIT_WEBHOOK_NOTE_NOTEABLE_TYPE
import com.tencent.devops.common.webhook.pojo.code.BK_REPO_GIT_WEBHOOK_NOTE_PROJECT_ID
import com.tencent.devops.common.webhook.pojo.code.BK_REPO_GIT_WEBHOOK_NOTE_UPDATED_AT
import com.tencent.devops.common.webhook.pojo.code.BK_REPO_GIT_WEBHOOK_NOTE_URL
import com.tencent.devops.common.webhook.pojo.code.PIPELINE_WEBHOOK_NOTE_COMMENT
import com.tencent.devops.common.webhook.pojo.code.PIPELINE_WEBHOOK_NOTE_ID
import com.tencent.devops.common.webhook.pojo.code.WebHookParams
import com.tencent.devops.common.webhook.pojo.code.github.GithubCommentEvent
import com.tencent.devops.common.webhook.service.code.filter.ContainsFilter
import com.tencent.devops.common.webhook.service.code.filter.EventTypeFilter
import com.tencent.devops.common.webhook.service.code.filter.GitUrlFilter
import com.tencent.devops.common.webhook.service.code.filter.RegexContainFilter
import com.tencent.devops.common.webhook.service.code.filter.WebhookFilter
import com.tencent.devops.common.webhook.service.code.handler.CodeWebhookTriggerHandler
import com.tencent.devops.common.webhook.service.code.matcher.ScmWebhookMatcher
import com.tencent.devops.common.webhook.util.WebhookUtils
import com.tencent.devops.repository.pojo.Repository

/**
 * Github 评论事件处理器接口
 */
@Suppress("TooManyFunctions")
interface GithubCommentTriggerHandler<T : GithubCommentEvent> : CodeWebhookTriggerHandler<T> {
    override fun getUrl(event: T): String {
        return event.repository.htmlUrl
    }

    override fun getUsername(event: T): String {
        return event.sender.login
    }

    override fun getRevision(event: T): String {
        return ""
    }

    override fun getRepoName(event: T): String {
        return event.repository.fullName
    }

    override fun getBranchName(event: T): String {
        return ""
    }

    override fun getEventType(): CodeEventType {
        return CodeEventType.NOTE
    }

    override fun getMessage(event: T): String? {
        return event.comment.body
    }

    override fun preMatch(event: T): ScmWebhookMatcher.MatchResult {
        return ScmWebhookMatcher.MatchResult(true)
    }

    override fun getWebhookFilters(
        event: T,
        projectId: String,
        pipelineId: String,
        repository: Repository,
        webHookParams: WebHookParams
    ): List<WebhookFilter> {
        with(webHookParams) {
            val urlFilter = GitUrlFilter(
                pipelineId = pipelineId,
                triggerOnUrl = getUrl(event),
                repositoryUrl = repository.url,
                includeHost = includeHost
            )
            val eventTypeFilter = EventTypeFilter(
                pipelineId = pipelineId,
                triggerOnEventType = getEventType(),
                eventType = eventType
            )
            val typeActionFilter = ContainsFilter(
                pipelineId = pipelineId,
                filterName = "noteTypeAction",
                triggerOn = event.getCommentType(),
                included = WebhookUtils.convert(includeNoteTypes)
            )
            val commentActionFilter = RegexContainFilter(
                pipelineId = pipelineId,
                filterName = "noteCommentAction",
                triggerOn = event.comment.body,
                included = WebhookUtils.convert(includeNoteComment)
            )
            return listOf(urlFilter, eventTypeFilter, typeActionFilter, commentActionFilter)
        }
    }

    override fun retrieveParams(
        event: T,
        projectId: String?,
        repository: Repository?
    ): Map<String, Any> {
        // Github 评论事件公共参数
        val startParams = mutableMapOf<String, Any>()
        with(event.comment) {
            startParams[PIPELINE_WEBHOOK_NOTE_COMMENT] = body
            startParams[PIPELINE_WEBHOOK_NOTE_ID] = id
            startParams[BK_REPO_GIT_WEBHOOK_NOTE_COMMENT] = body
            startParams[BK_REPO_GIT_WEBHOOK_NOTE_ID] = id
            startParams[BK_REPO_GIT_WEBHOOK_NOTE_PROJECT_ID] = projectId.toString()
            startParams[BK_REPO_GIT_WEBHOOK_NOTE_AUTHOR_ID] = user.id
            startParams[BK_REPO_GIT_WEBHOOK_NOTE_CREATED_AT] = createdAt ?: ""
            startParams[BK_REPO_GIT_WEBHOOK_NOTE_UPDATED_AT] = updatedAt ?: ""
            startParams[BK_REPO_GIT_WEBHOOK_NOTE_URL] = url ?: ""
            startParams[BK_REPO_GIT_WEBHOOK_NOTE_NOTEABLE_TYPE] = event.getCommentType()
        }
        // 填充其他参数
        startParams.putAll(getCommentParam(event))
        return startParams
    }

    /**
     * Github 评论事件关联参数，根据具体事件类型进行填充
     */
    fun getCommentParam(event: T): Map<String, Any>
}
