<template lang="">
    <div>
        <img ref="receive" style="width:400px;height:330px;">
        <div ref="dm" style="width:400px;height:330px; position:absolute; top:71px; left: 12px; backgroud:rgb(0,0,0,0.3); color:white;">
            {{message}}
        </div>
        <input type="text" ref="msg">
        <input type="button" value="发送消息" v-on:click="sendMsg()">
    </div>
</template>
<script>
export default {
    data() {
        return {
            chatroomSocket: null,
            message: "",
        }
    },
    mounted() {
        let that = this;
        let image = this.$refs.receive;
        let url = window._CONFIG['domianURL'].replace("https://","wss://").replace("http://","ws://")+"/live";
        let websocket = new WebSocket(url);
        websocket.onmessage = function(info) {
            image.src = info.data;
        }

        let chaturl = window._CONFIG['domianURL'].replace("https://","wss://").replace("http://","ws://")+"/chatroom";
        this.chatroomSocket = new WebSocket(chaturl);
        this.chatroomSocket.onmessage = function(info) {
            that.message+=" " + info.data;
        }
    },
    methods: {
        sendMsg(){
            let that = this
            let msg = that.$refs.msg.value;
            that.chatroomSocket.send(msg);
        }
    },
    
}
</script>
<style lang="">
    
</style>