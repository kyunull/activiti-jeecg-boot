<script>
import store from '../../../store';
import {mapActions, mapGetters, mapState} from 'vuex';
import {getFileAccessHttpUrl} from '@/api/manage';

export default {
    updated() {
        this.$nextTick(() => {
            var container = this.$refs.box;
            container.scrollTop = container.scrollHeight;
        });
    },
    methods: {
        getAvatarView: function (avatar) {
            return getFileAccessHttpUrl(avatar)
        },
    },
    computed: {
        user() {
            return store.getters.userInfo;
        },
        ...mapState({
            messageList: state => state.chat.messageList,
            commonList: state => state.chat.commonList,
            currentId: state => state.chat.currentSessionId,
        })
    },
    filters: {
        // 将日期过滤为 hour:minutes
        time (date) {
            if (typeof date === 'string') { 
                date = new Date(date);
            }
            return date.getHours() + ':' + date.getMinutes();
        }
    },
};
</script>

<template>
<div class="message" ref="box">
    <ul v-if="messageList">
        <li v-for="(item, index) in currentId? messageList : commonList" :key="index">
            <p class="time">
                <span>{{ item.time | time }}</span>
            </p>
            <div :class="'main ' +  (item.from == user.id ? 'self': '')">
                <img class="avatar" width="30" height="30" :src="item.from == user.id ? getAvatarView(user.avatar) : getAvatarView(item.fromImg)" />
                <span v-if="item.from == user.id" class="main-name">{{user.realname}}</span>
                <span v-else class="main-name">{{item.fromName}}</span>
                <div class="text">{{ item.message }}</div>
            </div>
        </li>
    </ul>
</div>
</template>

<style lang="less" scoped>
.message {
    padding: 10px 15px;
    overflow-y: scroll;
    ul {
        padding-inline-start: 10px;
    }
    li {
        margin-bottom: 15px;
        list-style: none;
    }
    .time {
        margin: 7px 0;
        text-align: center;

        > span {
            display: inline-block;
            padding: 0 18px;
            font-size: 12px;
            color: #fff;
            border-radius: 2px;
            background-color: #dcdcdc;
        }
    }
    .avatar {
        float: left;
        margin: 0 10px 0 0;
        border-radius: 3px;
    }
    .text {
        display: inline-block;
        position: relative;
        padding: 0 10px;
        max-width: ~'calc(100% - 40px)';
        min-height: 30px;
        line-height: 2.5;
        font-size: 12px;
        text-align: left;
        word-break: break-all;
        background-color: #fafafa;
        border-radius: 4px;

        &:before {
            content: " ";
            position: absolute;
            top: 9px;
            right: 100%;
            border: 6px solid transparent;
            border-right-color: #fafafa;
        }
    }
    .self {
        text-align: right;

        .avatar {
            float: right;
            margin: 0 0 0 10px;
        }
        .text {
            background-color: #b2e281;

            &:before {
                right: inherit;
                left: 100%;
                border-right-color: transparent;
                border-left-color: #b2e281;
            }
        }
    }
    .main-name {
        font-size: 11px;
        color: gray;
        display: inherit;
        font-weight: 500;
        margin-bottom: 5px; 
    }
}
</style>