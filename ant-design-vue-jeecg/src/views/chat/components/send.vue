<script>
import {mapActions, mapGetters, mapState} from 'vuex';

export default {
    data () {
        return {
            content: ''
        };
    },
    computed: {
        ...mapState({
            // 当前会话index
            currentId: state => state.chat.currentSessionId,
            messageList: state => state.chat.messageList
        })
    },
    methods: {
        ...mapActions(["sendMessage", "initSelfMessage", "initCommonMessage"]),
        onKeyup (e) {
            if (e.ctrlKey && e.keyCode === 13 && this.content.length) {
                this.sendMessage(this.content);
                this.content = '';                
            }
        },
        onBtnSend() {
            if (this.content.length) {
                this.sendMessage(this.content);
                this.content = '';
            }
        },
        //清空消息
        clean() {
            this.content = '';
        },
    }
};
</script>

<template>
<div class="text">
    <textarea placeholder="按 Ctrl + Enter 发送" v-model="content" @keyup="onKeyup"></textarea>
    <div class="btn">
        <el-button @click="clean" size="mini" type="danger">清空</el-button>
        <el-button @click="onBtnSend" size="mini" icon="el-icon-position" type="success">发送</el-button>
    </div>
</div>
</template>

<style lang="less" scoped>
.text {
    height: 160px;
    border-top: solid 1px #ddd;
    background: white;
    textarea {
        padding: 10px;
        height: 66%;
        width: 100%;
        border: none;
        outline: none;
        font-family: "Micrsofot Yahei";
        resize: none;
    }

    .btn {
        float: right;
        margin: 3px 9px; 
    }
}
</style>