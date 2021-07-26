<template>
  <!--流程业务表单-->
  <div class="form-main">
    <a-card :body-style="{ padding: '24px 32px' }" :bordered="false">
      <a-form @submit="handleSubmit" :form="form">
        <!-- <a-form-item
          label="标题"
          :labelCol="{ lg: { span: 7 }, sm: { span: 7 } }"
          :wrapperCol="{ lg: { span: 10 }, sm: { span: 17 } }"
        >
          <a-input
            :disabled="disabled"
            v-decorator="['name', { rules: [{ required: true, message: '请输入标题' }] }]"
            name="name"
            placeholder="给目标起个名字"
          />
        </a-form-item> -->

        <a-form-item
          label="所属部门"
          :labelCol="{ lg: { span: 7 }, sm: { span: 7 } }"
          :wrapperCol="{ lg: { span: 10 }, sm: { span: 17 } }"
        >
          <j-select-depart
            :disabled="disabled"
            multi
            name="sysOrgCode"
            v-decorator="['sysOrgCode', { rules: [{ required: true, message: '请输入所属部门' }] }]"
          />
        </a-form-item>

        <a-form-item
          label="申请人"
          :labelCol="{ lg: { span: 7 }, sm: { span: 7 } }"
          :wrapperCol="{ lg: { span: 10 }, sm: { span: 17 } }"
        >
          <j-select-user-by-dep
            :disabled="disabled"
            name="applicant"
            v-decorator="['applicant', { rules: [{ required: true, message: '请输入申请人' }] }]"
          />
        </a-form-item>

        <a-form-item
          label="开始时间"
          :labelCol="{ lg: { span: 7 }, sm: { span: 7 } }"
          :wrapperCol="{ lg: { span: 10 }, sm: { span: 17 } }"
        >
          <j-date
            :disabled="disabled"
            placeholder="请选择开始时间"
            date-format="YYYY-MM-DD"
            style="width: 100%"
            name="beginTime"
            v-decorator="['beginTime', { rules: [{ required: true, message: '请选择开始时间' }] }]"
          />
        </a-form-item>

        <a-form-item
          label="结束时间"
          :labelCol="{ lg: { span: 7 }, sm: { span: 7 } }"
          :wrapperCol="{ lg: { span: 10 }, sm: { span: 17 } }"
        >
          <j-date
            :disabled="disabled"
            placeholder="请选择结束时间"
            :show-time="true"
            date-format="YYYY-MM-DD"
            style="width: 100%"
            name="endTime"
            v-decorator="['endTime', { rules: [{ required: true, message: '请选择结束时间' }] }]"
          />
        </a-form-item>

        <a-form-item
          label="请假天数"
          :labelCol="{ lg: { span: 7 }, sm: { span: 7 } }"
          :wrapperCol="{ lg: { span: 10 }, sm: { span: 17 } }"
        >
          <a-input-number
            :disabled="disabled"
            placeholder="请输入请假天数"
            style="width: 100%"
            name="days"
            v-decorator="['days', { rules: [{ required: true, message: '请输入请假天数' }] }]"
          />
        </a-form-item>

        <a-form-item
          label="请假类型"
          :labelCol="{ lg: { span: 7 }, sm: { span: 7 } }"
          :wrapperCol="{ lg: { span: 10 }, sm: { span: 17 } }"
        >
          <j-dict-select-tag
            :disabled="disabled"
            type="list"
            dictCode="leave"
            placeholder="请选择请假类型"
            name="type"
            v-decorator="['type', { rules: [{ required: true, message: '请选择请假类型' }] }]"
          />
        </a-form-item>

        <a-form-item
          label="请假原因"
          :labelCol="{ lg: { span: 7 }, sm: { span: 7 } }"
          :wrapperCol="{ lg: { span: 10 }, sm: { span: 17 } }"
        >
          <a-textarea
            :disabled="disabled"
            rows="4"
            name="reason"
            placeholder="请输入请假原因"
            v-decorator="['reason', { rules: [{ required: true, message: '请假原因' }] }]"
          />
        </a-form-item>

        <a-form-item v-if="!disabled" :wrapperCol="{ span: 24 }" style="text-align: center">
          <a-button type="primary" :disabled="disabled || btndisabled" @click="handleSubmit">保存</a-button>
          <a-button style="margin-left: 8px" :disabled="disabled" @click="close">取消</a-button>
        </a-form-item>
        <a-form-item v-if="task" :wrapperCol="{ span: 24 }" style="text-align: center">
          <a-button type="primary" @click="passTask">通过</a-button>
          <a-button style="margin-left: 8px" @click="backTask">驳回</a-button>
        </a-form-item>
      </a-form>
    </a-card>
  </div>
</template>

<script>
import pick from 'lodash.pick'

export default {
  name: 'demoForm',
  props: {
    /*全局禁用，可表示查看*/
    disabled: {
      type: Boolean,
      default: false,
      required: false
    },
    /*流程数据*/
    processData: {
      type: Object,
      default: () => {
        return {}
      },
      required: false
    },
    /*是否新增*/
    isNew: { type: Boolean, default: false, required: false },
    /*是否处理流程*/
    task: { type: Boolean, default: false, required: false }
  },
  data() {
    return {
      url: {
        getForm: '/actBusiness/getForm',
        addApply: '/actBusiness/add',
        editForm: '/actBusiness/editForm'
      },
      description: '流程表单demo，按例开发表单。需在 activitiMixin.js 中加入写好的表单',
      // form
      form: this.$form.createForm(this),
      /*表单回显数据*/
      data: {},
      btndisabled: false
    }
  },
  created() {
    console.log('流程数据', this.processData)    
    if (!this.isNew) {
      this.init()
    }
  },
  methods: {
    /*回显数据*/
    init() {
      var r = this.processData
      if(!r.tableId) {
        return
      }
      this.btndisabled = true
      this.getAction(this.url.getForm, {
        tableId: r.tableId,
        tableName: r.tableName
      }).then(res => {
        if (res.success) {
          let formData = res.result
          formData.tableName = r.tableName
          this.data = formData
          console.log('表单回显数据', this.data)
          this.$nextTick(() => {
            this.form.setFieldsValue(pick(this.data, "sysOrgCode","applicant","beginTime","endTime","days","type","reason"))
          })
          this.btndisabled = false
        } else {
          this.$message.error(res.message)
        }
      })
    },
    // handler
    handleSubmit(e) {
      e.preventDefault()
      this.form.validateFields((err, values) => {
        if (!err) {
          let formData = Object.assign(this.data || {}, values)
          formData.procDefId = this.processData.id
          formData.procDeTitle = this.processData.name
          if (!formData.tableName) formData.tableName = this.processData.businessTable
          formData.filedNames = _.keys(values).join(',')
          console.log('formData', values)

          var url = this.url.addApply
          if (!this.isNew) {
            url = this.url.editForm
          }
          this.btndisabled = true
          this.postFormAction(url, formData)
            .then(res => {
              if (res.success) {
                this.$message.success('保存成功！')
                //todo 将表单的数据传给父组件
                this.$emit('afterSubmit', formData)
              } else {
                this.$message.error(res.message)
              }
            })
            .finally(() => {
              this.btndisabled = false
            })
        }
      })
    },
    close() {
      //todo 关闭后的回调
      this.$emit('close')
    },
    /*通过审批*/
    passTask() {
      this.$emit('passTask')
    },
    /*驳回审批*/
    backTask() {
      this.$emit('backTask')
    }
  }
}
</script>
<style lang="less" scoped>
.form-main {
}
</style>
