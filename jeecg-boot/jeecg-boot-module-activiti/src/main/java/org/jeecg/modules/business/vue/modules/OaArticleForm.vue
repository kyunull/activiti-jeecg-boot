<template>
  <a-spin :spinning="confirmLoading">
    <j-form-container :disabled="formDisabled">
      <a-form-model ref="form" :model="model" :rules="validatorRules" slot="detail">
        <a-row>
          <a-col :span="24">
            <a-form-model-item label="文章分类：1政府重要通知；2政策法规；3内部刊物；4资料管理；5新闻；6公告；" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="articleCategory">
              <a-input v-model="model.articleCategory" placeholder="请输入文章分类：1政府重要通知；2政策法规；3内部刊物；4资料管理；5新闻；6公告；"  ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="文章标题" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="articleTitle">
              <a-input v-model="model.articleTitle" placeholder="请输入文章标题"  ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="封面图链接" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="coverImgUrl">
              <a-input v-model="model.coverImgUrl" placeholder="请输入封面图链接"  ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="关键字" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="articleKeywords">
              <a-input v-model="model.articleKeywords" placeholder="请输入关键字"  ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="文章内容" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="articleContent">
              <a-textarea v-model="model.articleContent" rows="4" placeholder="请输入文章内容" />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="文章类型：1原创2转载" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="articleType">
              <a-input v-model="model.articleType" placeholder="请输入文章类型：1原创2转载"  ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="转载原链接" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="transferUrl">
              <a-input v-model="model.transferUrl" placeholder="请输入转载原链接"  ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="位置类型：1普通2热门3置顶4滚动" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="positionType">
              <a-input v-model="model.positionType" placeholder="请输入位置类型：1普通2热门3置顶4滚动"  ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="文章浏览量" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="articleViews">
              <a-input-number v-model="model.articleViews" placeholder="请输入文章浏览量" style="width: 100%" />
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="显示类型：0隐藏；1显示；" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="showType">
              <a-input v-model="model.showType" placeholder="请输入显示类型：0隐藏；1显示；"  ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="文章排序" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="articleSort">
              <a-input v-model="model.articleSort" placeholder="请输入文章排序"  ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="备注" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="remarks">
              <a-input v-model="model.remarks" placeholder="请输入备注"  ></a-input>
            </a-form-model-item>
          </a-col>
          <a-col :span="24">
            <a-form-model-item label="删除标记 0-有效  1-删除" :labelCol="labelCol" :wrapperCol="wrapperCol" prop="delFlag">
              <a-input v-model="model.delFlag" placeholder="请输入删除标记 0-有效  1-删除"  ></a-input>
            </a-form-model-item>
          </a-col>
        </a-row>
      </a-form-model>
    </j-form-container>
  </a-spin>
</template>

<script>

  import { httpAction, getAction } from '@/api/manage'
  import { validateDuplicateValue } from '@/utils/util'

  export default {
    name: 'OaArticleForm',
    components: {
    },
    props: {
      //表单禁用
      disabled: {
        type: Boolean,
        default: false,
        required: false
      }
    },
    data () {
      return {
        model:{
         },
        labelCol: {
          xs: { span: 24 },
          sm: { span: 5 },
        },
        wrapperCol: {
          xs: { span: 24 },
          sm: { span: 16 },
        },
        confirmLoading: false,
        validatorRules: {
           articleCategory: [
              { required: true, message: '请输入文章分类：1政府重要通知；2政策法规；3内部刊物；4资料管理；5新闻；6公告；!'},
           ],
           articleTitle: [
              { required: true, message: '请输入文章标题!'},
           ],
           articleContent: [
              { required: true, message: '请输入文章内容!'},
           ],
           articleType: [
              { required: true, message: '请输入文章类型：1原创2转载!'},
           ],
           showType: [
              { required: true, message: '请输入显示类型：0隐藏；1显示；!'},
           ],
           delFlag: [
              { required: true, message: '请输入删除标记 0-有效  1-删除!'},
           ],
        },
        url: {
          add: "/business/oaArticle/add",
          edit: "/business/oaArticle/edit",
          queryById: "/business/oaArticle/queryById"
        }
      }
    },
    computed: {
      formDisabled(){
        return this.disabled
      },
    },
    created () {
       //备份model原始值
      this.modelDefault = JSON.parse(JSON.stringify(this.model));
    },
    methods: {
      add () {
        this.edit(this.modelDefault);
      },
      edit (record) {
        this.model = Object.assign({}, record);
        this.visible = true;
      },
      submitForm () {
        const that = this;
        // 触发表单验证
        this.$refs.form.validate(valid => {
          if (valid) {
            that.confirmLoading = true;
            let httpurl = '';
            let method = '';
            if(!this.model.id){
              httpurl+=this.url.add;
              method = 'post';
            }else{
              httpurl+=this.url.edit;
               method = 'put';
            }
            httpAction(httpurl,this.model,method).then((res)=>{
              if(res.success){
                that.$message.success(res.message);
                that.$emit('ok');
              }else{
                that.$message.warning(res.message);
              }
            }).finally(() => {
              that.confirmLoading = false;
            })
          }
         
        })
      },
    }
  }
</script>