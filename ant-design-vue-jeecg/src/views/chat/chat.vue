<script>
import store from '../../store';
import { mapActions, mapGetters,mapState } from 'vuex'

import Card from './components/card';
import List from './components/list';
import Send from './components/send';
import Message from './components/message';

export default {
    components: { Card, List, Send, Message },
    data () {
      return {
          websocket: null
      }
    },
    computed : {
        ...mapState({
            currentId: state => state.chat.currentSessionId,
            messageList: state => state.chat.messageList,
            commonList: state => state.chat.commonList,
        })
    },
    created () {
        //主动链接WebSocket
        this.initWebSocket();
        //加载在线用户信息
        this.initUser();
        //加载公共消息列表 -- 群组
        this.initCommonMessage();
    },
    methods: {
        ...mapActions(['initUser', 'initCommonMessage']),
        initWebSocket() {
            let $this = this;
            let user = store.getters.userInfo;
            var url = window._CONFIG['domianURL'].replace("https://","wss://").replace("http://","ws://")+"/chat/"+user.id;
            this.websocket = new WebSocket(url)
            //链接发送错误时调用
            this.websocket.onerror = function () {
                $this._notify('提醒', user.realname + ': 上线失败', 'error')
            }
            //链接成功时调用
            this.websocket.onopen = function () {
                $this._notify('提醒', user.realname + ': 上线了!', 'success')
            }
            //接收到消息时回调
            this.websocket.onmessage = function (event) {
                $this.clean()
                let entity = JSON.parse(event.data);
                //上线提醒
                if (entity.message == undefined) {
                    $this.initUser()
                    $this._notify('消息', entity.msg, 'info')
                    return;
                }

                //消息接收
                let data = JSON.parse(event.data)
                if (data.to == "0") {
                    //群发，推送到官方群组窗口
                    $this.commonList.push(data)
                } else {
                    //单个窗口发送，仅推送到指定的窗口
                    if (data.from == $this.currentId) {
                        $this.messageList.push(data)
                    }
                }
            }
            //链接关闭时调用
            this.websocket.onclose = function () {
                $this._notify('提醒', user.realname + ': 下线', 'info')
            }
        },
        _notify(title, message, type) {
            this.$notification[type]({
                title: title,
                message: message
            });
        },
        //清空消息
        clean() {
            this.content = '';
        },
    },

}
</script>

<template>
    <div id="chat">
        <div class="sidebar">
            <card></card>
            <list></list>
        </div>
        <div class="main">
            <message></message>
            <send></send>
        </div>
    </div>
</template>

<style lang="less" scoped>
#chat {
    // background: #f5f5f5 url('/public/img/bg.jpg') no-repeat center;
    // background: #f0f5f0 url(~@/assets/bg.jpg) no-repeat center;
    margin: 20px auto;
    width: 800px;
    height: 600px;

    overflow: hidden;
    border-radius: 3px;

    .sidebar, .main {
        height: 100%;
    }
    .sidebar {
        float: left;
        width: 200px;
        color: #f4f4f4;
        background-color: #2e3238;
    }
    .main {
        position: relative;
        overflow: hidden;
        background-color: #eee;
    }
    .text {
        position: absolute;
        width: 100%;
        bottom: 0;
        left: 0;
    }
    .message {
        height: ~'calc(100% - 160px)';
    }
}
</style>
