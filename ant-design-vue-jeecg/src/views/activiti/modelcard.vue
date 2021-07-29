<template>
  <div v-loading="contentLoading" class="main-home" element-loading-background="rgba(0, 0, 0, 0)">
    <a-upload
        name="uploadfile"
        class="card upload"
        :multiple="true"
        action= "http://localhost:8080/jeecg-boot/activiti/models/uploadFile"
        @change="handleChange"
        :showUploadList="false"
      >
        <div class="upload-text">
          <i class="el-icon-upload"/>
          <div>将文件拖到此处，或<em>点击上传</em></div>
        </div>
    </a-upload>
    <div v-for="item in appList" :key="item.id" class="card app">
      <img
          style="cursor:pointer;"
          alt="图标"
          width="180"
          height="180"
          :src="'http://localhost:8080/jeecg-boot/activiti/models/activiti/exportDiagram' + '?modelId=' + item.id"
          @click="exportdiagram(item)">
      <div class="app-name">{{item.name}}</div>
      <table>
        <tr>
          <td>模型key:</td>
          <td>{{item.key}}</td>
        </tr>
        <tr>
          <td>最新版本:</td>
          <td>{{item.revision}}</td>
        </tr>
      </table>
      <div class="app-btn">
        <el-button size="mini" icon="el-icon-edit" round @click="updatelc(item.id)">设计</el-button>
        <el-button size="mini" icon="el-icon-view" round style="margin-left: 10px" @click.stop="deployment(item)">发布</el-button>
        <a-popconfirm
          title="是否确认删除?"
          @confirm="deletelc(1,item)"
          @cancel="deletelc(0)"
          okText="Yes"
          cancelText="No"
        >
          <!-- <a href="javascript:void(0);">删除</a> -->
          <el-button size="mini" icon="el-icon-view" round style="margin-left: 10px">删除</el-button>
        </a-popconfirm>
      </div>
    </div>
    <app-upload-dialog
        ref="appUploadDialog"
        @upload="$refs.upload.submit()"
        @appInfo="onAppInfo"/>

    <a-modal
      title="设计模型"
      :visible="updateObj.visible"
      :footer="null" :maskClosable="false"
      width="90%"
      @cancel="cancelUpdate"
      style="top: 20px;"
    >
      <iframe :src="iframUrl" frameborder="0" width="100%" height="800px" scrolling="auto" style="background-color: #fff;"></iframe>
    </a-modal>
    <!--查看图片-->
    <el-dialog 
      :title="viewTitle" width="90%"
      :visible.sync="viewImage" :footer="null"
    >
      <div style="min-height: 400px">
        <img :src="diagramUrl" :alt="viewTitle">
      </div>
    </el-dialog>
  </div>

</template>

<script>
  import AppUploadDialog from "@/views/publish/appList/AppUploadDialog";
  import { getAction } from '@/api/manage'
  export default {
    components: {AppUploadDialog},
    data() {
      return {
        //模型流程图
        viewImage:false,
        viewTitle:"",
        diagramUrl:"",
        contentLoading: true,
        /*流程设计器连接*/
        iframUrl:"",
        /*新增流程框参数*/
        createObj:{
          visible: false,
          confirmLoading: false,
        },
        /*设计流程框参数*/
        updateObj:{
          visible: false,
          confirmLoading: false,
        },

        appList: [],
        url: {
          list: "/activiti/models/modelListData",
          delete: "/activiti/models/delete/",
          deployment: "/activiti/models/deployment/",
          create: "/activiti/models/create",
          update: "/activiti/modeler.html?modelId=",
          // upload: "/activiti/models/uploadFile/",
          upload: "http://localhost:8080/jeecg-boot/activiti/models/uploadFile",
        },
        uploadAppInfo: null,
        auth: {
          authorization: "",
        },
      };
    },
    mounted() {
      this.getAppList();
    },
    methods: {
      getAppList() {
        this.contentLoading = true;
        this.getAction(this.url.list,{}).then(res => {
          if (res.success) {
            this.appList = res.result||[];
          }else {
            this.$message.warning(res.message)
          }
        }).finally(()=>this.contentLoading = false);
      },
      uploadChange(file) {
        if (file.status === "ready") {
          this.$refs.appUploadDialog.open(file);
        }
      },
      uploadError() {
        this.$refs.appUploadDialog.close();
        this.$message.error("上传失败，请重试");
      },
      uploadProgress(event) {
        this.$refs.appUploadDialog.onProgress(event);
      },
      uploadSuccess() {
        this.$refs.appUploadDialog.close();
        this.$message.success("上传成功");
        this.getAppList();
      },
      onAppInfo(appInfo) {
        this.uploadAppInfo = appInfo;
      },
      toVersion(id) {
        this.$router.push({
          path: "/publish/appDetail/AppVersions/" + id
        });
      },
      gotoPreview(item) {
        window.open(document.location.protocol + "//" + window.location.host + "/preview/" + item.shortCode, "_blank");
      },
      getUrl(item) {
        return window.location.protocol + "//" + window.location.host + "/preview/" + item.shortCode;
      },

      exportdiagram(record) {
        this.viewTitle = "流程图片预览(" + record.name + ")";
        this.diagramUrl = 'http://localhost:8080/jeecg-boot/activiti/models/activiti/exportDiagram' + '?modelId=' + record.id;
        this.viewImage = true;
      },
      /*修改流程*/
      updatelc(id){
        var _this = this;
        this.$message
          .loading('稍等。。。', 0.8)
          .then(() => {
            _this.createObj.confirmLoading = true;
            _this.iframUrl = `${window._CONFIG['domianURL']}${_this.url.update}${id}`;
            _this.updateObj.visible = true;
            _this.createObj.confirmLoading = false;
          })
      },
      cancelUpdate(){
        var _this = this;
        this.$confirm({
          title: '关闭前请确认已保存修改?',
          content: '关闭后未保存的修改将丢失！',
          okText: '确认关闭',
          okType: 'danger',
          cancelText: '再看看',
          onOk() {
            _this.updateObj.visible = false;
            _this.getAppList();
          },
          onCancel() {
            console.log('Cancel');
          },
        });
      },
      /*发布流程*/
      deployment(row){
        var _this = this;
        var id = row.id;
        var name = row.name;
        this.$confirm({
          title: '确认部署流程?',
          content: `确认部署流程：${name}`,
          onOk() {
            getAction(_this.url.deployment+id).then((res) => {
              if (res.success){
                _this.$message.success(res.message);
              }else {
                _this.$message.error(res.message);
              }
              _this.getAppList();
            });
          },
          onCancel() {},
        });
      },
      /*删除模型*/
      deletelc(y,row) {
        console.log(y,row);
        if (y){
          getAction(this.url.delete+row.id).then((res) => {
            if (res.success){
              this.$message.success(res.message);
            }else {
              this.$message.error(res.message);
            }
            this.getAppList();
          });
        }
      },
      handleChange(info) {
        if (info.file.status !== 'uploading') {
          console.log(info.file, info.fileList);
        }
        if (info.file.status === 'done') {
          this.$message.success(`${info.file.name} 文件上传成功.`);
          this.getAppList();
        } else if (info.file.status === 'error') {
          this.$message.error(`${info.file.name} 文件上传失败.`);
        }
      },
      
    },
  };
</script>

<style lang="scss" scoped>
@import "./style/variables";

.main-home {
  font-size: 20px;
  display: flex;
  flex-wrap: wrap;
  // width: 1020px;
  margin: 0 auto;
  padding: 20px 50px;

  .card {
    margin: 12px;
    width: 22.5%;
    height: 430px;
    background-color: #fff;
    padding: 0;
    transition: all .25s;

    i {
      transition: all .25s;
    }

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 15px 30px rgba(0, 0, 0, .1);

      .app-name {
        color: #4a4a4a;
      }

      i {
        transform: scale(1.2);
      }
    }
  }

  .upload {
    background-color: $color-primary;
    cursor: pointer;

    i {
      color: white;
      font-size: 85px;
      margin-bottom: 25px;
    }

    .upload-text {
      color: white;
      font-size: 17px;
      margin-top: 135px;
    }
  }

  .app {
    position: relative;
    box-sizing: border-box;
    padding: 50px 45px 45px 45px;

    .app-name {
      font-size: 18px;
      margin: 25px 0;
    }

    table {
      width: 100%;
      font-size: 12px;

      td {
        padding: 4px 0;
      }

      td:last-child {
        padding-left: 10px;
        color: #1A1A1A;
        word-break: break-all;
      }
    }

    .app-btn {
      width: 100%;
      position: absolute;
      text-align: center;
      bottom: 30px;
      left: 0;
    }
  }
}
</style>
<style lang="scss">
.main-home {
  .card {
    .el-upload, .el-upload-dragger {
      width: 100%;
      height: 100%;
    }

    .el-upload-dragger {
      background-color: transparent;
      border: none;
    }

    .el-upload-dragger.is-dragover {
      background-color: rgba(0, 0, 0, 0.3);
      border-radius: 0;
    }
  }
}
</style>