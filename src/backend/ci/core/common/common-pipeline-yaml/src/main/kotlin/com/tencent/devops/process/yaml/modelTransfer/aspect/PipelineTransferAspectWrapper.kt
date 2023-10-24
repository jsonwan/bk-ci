package com.tencent.devops.process.yaml.modelTransfer.aspect

import com.tencent.devops.common.pipeline.container.Container
import com.tencent.devops.common.pipeline.container.Stage
import com.tencent.devops.common.pipeline.pojo.element.Element
import com.tencent.devops.process.yaml.v3.models.on.TriggerOn
import java.util.LinkedList
import kotlin.reflect.jvm.jvmName
import com.tencent.devops.process.yaml.v3.models.job.Job as YamlV3Job
import com.tencent.devops.process.yaml.v3.models.stage.Stage as YamlV3Stage
import com.tencent.devops.process.yaml.v3.models.step.Step as YamlV3Step


class PipelineTransferAspectWrapper {
    private val aspectTrigger = LinkedList<IPipelineTransferAspectTrigger>()
    private val aspectElement = LinkedList<IPipelineTransferAspectElement>()
    private val aspectJob = LinkedList<IPipelineTransferAspectJob>()
    private val aspectStage = LinkedList<IPipelineTransferAspectStage>()

    private val pipelineTransferJoinPoint = PipelineTransferJoinPointImpl()
    private var clzName: String? = null

    constructor(list: LinkedList<IPipelineTransferAspect>) {
        list.forEach {
            when (it) {
                is IPipelineTransferAspectTrigger -> aspectTrigger.add(it)
                is IPipelineTransferAspectElement -> aspectElement.add(it)
                is IPipelineTransferAspectJob -> aspectJob.add(it)
                is IPipelineTransferAspectStage -> aspectStage.add(it)
            }
        }
    }

    enum class AspectType {
        BEFORE,
        AFTER;
    }


    /*
    * AspectType.BEFORE: model -> yaml before stage
    * AspectType.AFTER: yaml -> model after stage
    */
    fun setModelStage4Model(modelStage: Stage?, aspectType: AspectType) {
        pipelineTransferJoinPoint.modelStage = modelStage
        clzName = IPipelineTransferAspectStage::class.jvmName
        when (aspectType) {
            AspectType.BEFORE -> aspectBefore()
            AspectType.AFTER -> aspectAfter()
        }
    }

    /*
   * AspectType.BEFORE: model -> yaml before job
   * AspectType.AFTER: yaml -> model after job
   */
    fun setModelJob4Model(modelJob: Container?, aspectType: AspectType) {
        pipelineTransferJoinPoint.modelJob = modelJob
        clzName = IPipelineTransferAspectJob::class.jvmName
        when (aspectType) {
            AspectType.BEFORE -> aspectBefore()
            AspectType.AFTER -> aspectAfter()
        }
    }

    /*
       * AspectType.BEFORE: model -> yaml before element
       * AspectType.AFTER: yaml -> model after element
       */
    fun setModelElement4Model(modelElement: Element?, aspectType: AspectType) {
        pipelineTransferJoinPoint.modelElement = modelElement
        clzName = IPipelineTransferAspectElement::class.jvmName
        when (aspectType) {
            AspectType.BEFORE -> aspectBefore()
            AspectType.AFTER -> aspectAfter()
        }
    }

    /*
       * AspectType.BEFORE: yaml -> moel before stage
       * AspectType.AFTER: model -> yaml after stage
       */
    fun setYamlStage4Yaml(yamlStage: YamlV3Stage?, aspectType: AspectType) {
        pipelineTransferJoinPoint.yamlStage = yamlStage
        clzName = IPipelineTransferAspectStage::class.jvmName
        when (aspectType) {
            AspectType.BEFORE -> aspectBefore()
            AspectType.AFTER -> aspectAfter()
        }
    }

    /*
       * AspectType.BEFORE: yaml -> moel before job
       * AspectType.AFTER: model -> yaml after job
       */
    fun setYamlJob4Yaml(yamlJob: YamlV3Job?, aspectType: AspectType) {
        pipelineTransferJoinPoint.yamlJob = yamlJob
        clzName = IPipelineTransferAspectJob::class.jvmName
        when (aspectType) {
            AspectType.BEFORE -> aspectBefore()
            AspectType.AFTER -> aspectAfter()
        }
    }

    /*
       * AspectType.BEFORE: yaml -> moel before step
       * AspectType.AFTER: model -> yaml after step
       */
    fun setYamlStep4Yaml(yamlStep: YamlV3Step?, aspectType: AspectType) {
        pipelineTransferJoinPoint.yamlStep = yamlStep
        clzName = IPipelineTransferAspectElement::class.jvmName
        when (aspectType) {
            AspectType.BEFORE -> aspectBefore()
            AspectType.AFTER -> aspectAfter()
        }
    }

    /*
       * AspectType.BEFORE: yaml -> moel before trigger on
       * AspectType.AFTER: model -> yaml after trigger on
       */
    fun setYamlTriggerOn(yamlTriggerOn: TriggerOn?, aspectType: AspectType) {
        pipelineTransferJoinPoint.yamlTriggerOn = yamlTriggerOn
        clzName = IPipelineTransferAspectTrigger::class.jvmName
        when (aspectType) {
            AspectType.BEFORE -> aspectBefore()
            AspectType.AFTER -> aspectAfter()
        }
    }

    private fun aspectBefore(): Any? {
        val wrappers = iPipelineTransferAspects(clzName)
        if (wrappers.isNullOrEmpty()) return null
        var res: Any? = null
        wrappers.forEach {
            res = it.before(pipelineTransferJoinPoint)
        }
        return res
    }


    private fun aspectAfter(): Any? {
        val wrappers = iPipelineTransferAspects(clzName)
        if (wrappers.isNullOrEmpty()) return null
        var res: Any? = null
        wrappers.forEach {
            res = it.after(pipelineTransferJoinPoint)
        }
        return res
    }

    private fun iPipelineTransferAspects(clzName: String?) = when (clzName) {
        IPipelineTransferAspectTrigger::class.jvmName -> aspectTrigger
        IPipelineTransferAspectElement::class.jvmName -> aspectElement
        IPipelineTransferAspectJob::class.jvmName -> aspectJob
        IPipelineTransferAspectStage::class.jvmName -> aspectStage
        else -> null
    }
}
