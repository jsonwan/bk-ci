<template>
    <div class="env-variable-wrapper">
        <div class="Key-value-nomal">
            <ul>
                <template v-if="renderList.length">
                    <li
                        class="param-item"
                        v-for="(param, index) in renderList"
                        :key="index"
                    >
                        <input
                            class="bk-form-input"
                            :name="`param-${index}-key`"
                            :disabled="!editable"
                            v-validate="'required'"
                            v-model="param.name"
                            :class="{ 'is-danger': errors.has(`param-${index}-key`) }"
                        />
                        <i class="equal-sign">=</i>
                        <input
                            class="bk-form-input"
                            :name="`param-${index}-value`"
                            :disabled="!editable"
                            :type="param.secure ? 'password' : 'text'"
                            v-validate="'required'"
                            v-model="param.value"
                            :class="{ 'editable-input': editable, 'is-danger': errors.has(`param-${index}-value`) }"
                        />
                        <i
                            class="devops-icon text-type-icon"
                            v-if="editable"
                            :class="param.secure ? 'icon-eye' : 'icon-hide'"
                            @click="toggleInputType(index)"
                        ></i>
                        <section v-if="editable">
                            <i
                                class="devops-icon icon-minus-circle"
                                @click="reduceHandle(index, 'reduce')"
                            ></i>
                        </section>
                    </li>
                </template>
            </ul>
            <span
                v-perm="{
                    hasPermission: nodeDetails.canEdit,
                    disablePermissionApi: true,
                    permissionData: {
                        projectId: projectId,
                        resourceType: NODE_RESOURCE_TYPE,
                        resourceCode: nodeHashId,
                        action: NODE_RESOURCE_ACTION.EDIT
                    }
                }"
            >
                <p
                    class="add-variable"
                    :class="{ 'is-disabled': !nodeDetails.canEdit }"
                    v-if="editable || !renderList.length"
                    @click="addHandle"
                >
                    <i class="devops-icon icon-plus-circle"></i>{{ $t('environment.envInfo.createVariable') }}
                </p>
            </span>
            <div class="footer-handle">
                <bk-button
                    theme="primary"
                    :disabled="!nodeDetails.canEdit"
                    v-if="!editable && renderList.length"
                    @click="edithandle"
                >
                    {{ $t('environment.edit') }}
                </bk-button>
                <bk-button
                    theme="primary"
                    v-if="editable"
                    @click="save"
                >
                    {{ $t('environment.save') }}
                </bk-button>
                <bk-button
                    theme="defalut"
                    v-if="editable"
                    @click="editable = false"
                >
                    {{ $t('environment.cancel') }}
                </bk-button>
            </div>
        </div>
    </div>
</template>

<script>
    import { mapState } from 'vuex'
    import { bus } from '@/utils/bus'
    import { NODE_RESOURCE_ACTION, NODE_RESOURCE_TYPE } from '../../../utils/permission'

    export default {
        data () {
            return {
                editable: false,
                paramList: [],
                editableList: [],
                NODE_RESOURCE_ACTION,
                NODE_RESOURCE_TYPE
            }
        },
        computed: {
            ...mapState('environment', [
                'nodeDetails'
            ]),
            projectId () {
                return this.$route.params.projectId
            },
            nodeHashId () {
                return this.$route.params.nodeHashId
            },
            renderList () {
                return this.editable ? this.editableList : this.paramList
            }
        },
        mounted () {
            this.requestEnvs()
        },
        created () {
            bus.$off('refreshEnv')
            bus.$on('refreshEnv', () => {
                this.requestEnvs()
            })
        },
        methods: {
            edithandle () {
                this.editable = true
                this.editableList = JSON.parse(JSON.stringify(this.paramList))
            },
            async requestEnvs () {
                try {
                    const res = await this.$store.dispatch('environment/requestEnvs', {
                        projectId: this.projectId,
                        nodeHashId: this.nodeHashId
                    })
                    this.paramList.splice(0, this.paramList.length, ...res || [])
                    this.editable = false
                } catch (err) {
                    const message = err.message ? err.message : err
                    const theme = 'error'

                    this.$bkMessage({
                        message,
                        theme
                    })
                }
            },
            addHandle () {
                if (!this.nodeDetails.canEdit) {
                    this.handleNoPermission({
                        projectId: this.projectId,
                        resourceType: NODE_RESOURCE_TYPE,
                        resourceCode: this.nodeHashId,
                        action: NODE_RESOURCE_ACTION.EDIT
                    })
                } else {
                    this.editable = true
                    this.editableList.push({ name: '', value: '', secure: false })
                }
            },
            reduceHandle (index, type) {
                this.editableList.splice(index, 1)
            },
            toggleInputType (key) {
                this.editableList = this.editableList.map((item, index) => {
                    return {
                        ...item,
                        secure: index === key ? !item.secure : item.secure
                    }
                })
            },
            async save () {
                const valid = await this.$validator.validate()
                if (valid) {
                    let message, theme
                    const params = this.editableList
                    try {
                        await this.$store.dispatch('environment/saveEnvs', {
                            projectId: this.projectId,
                            nodeHashId: this.nodeHashId,
                            params
                        })

                        message = this.$t('environment.successfullySaved')
                        theme = 'success'
                        this.requestEnvs()
                    } catch (err) {
                        message = err.message ? err.message : err
                        theme = 'error'
                    } finally {
                        this.$bkMessage({
                            message,
                            theme
                        })
                    }
                }
            }
        }
    }
</script>

<style lang="scss">
    @import './../../../scss/conf';
    .env-variable-wrapper {
        padding: 20px 20px 0;
        .Key-value-nomal {
            input {
                width: 240px;
            }
            .param-item {
                display: flex;
                position: relative;
                align-items: flex-start;
                margin-bottom: 10px;
            }
            .editable-input {
                padding-right: 40px;
            }
            .equal-sign {
                margin: auto 10px;
            }
            .is-danger {
                border-color: #ff5656;
                background-color: #fff4f4;
                color: #ff5656;
            }
            .icon-minus-circle {
                position: relative;
                top: 8px;
                margin-left: 14px;
                font-size: 18px;
                color: #C3CDD7;
                cursor: pointer;
            }
            .icon-plus-circle {
                margin-left: 4px;
            }
            .text-type-icon {
                position: absolute;
                top: 12px;
                left: 480px;
                cursor: pointer;
            }
        }
        .add-variable {
            color: $primaryColor;
            cursor: pointer;
            .devops-icon {
                margin-right: 4px;
            }
        }
        .is-disabled {
            color: #CCC;
        }
        .footer-handle {
            margin-top: 20px;
            margin-bottom: 20px;
        }
    }
</style>
