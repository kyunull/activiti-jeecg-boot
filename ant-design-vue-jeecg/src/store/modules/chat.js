import Vue from 'vue'
import { getAction } from '@/api/manage'
import store from '../../store';

const now = new Date();
const chat = {
  state: {
    //在线用户列表
    onlineUserList: [],
    //消息列表
    messageList: [],
    //群消息列表
    commonList: [],
    //当前选中的会话
    currentSessionId: 0,
    currentSessionImg: "",
    currentSessionName: "",
    // 过滤出只包含这个key的会话
    filterKey: ''
  },
  mutations: {
    INIT_ONLINE_USER (state, onlineUser) {
        state.onlineUserList = onlineUser;
    },
    // 选择会话
    SET_SESSION_ID (state, id) {
        state.currentSessionId = id;
    } ,
    SET_SESSION_IMG (state, img) {
        state.currentSessionImg = img;
    } ,
    SET_SESSION_NAME (state, name) {
        state.currentSessionName = name;
    } ,
    // 搜索
    SET_FILTER_KEY (state, value) {
        state.filterKey = value;
    },
    //私聊信息
    SET_MESSAGES_LIST (state, messages) {
        state.messageList = messages;
    },
    //群聊信息
    SET_COMMON_LIST (state, messages) {
        state.commonList = messages;
    },
    
  },
  actions: {
    initUser: ({ commit , state}) => {
        return new Promise((resolve, reject) => {
            getAction("sys/online/list").then(response => {
                if(response.success){
                    commit('INIT_ONLINE_USER', response.result.records);
                    resolve(response)
                }else {
                    resolve(response)
                }
            }).catch( error => {
                reject(error);
            })
        })
    },

    initCommonMessage: ({ commit }) => {
        return new Promise((resolve, reject) => {
            getAction("chat/common").then(response => {
                if(response.success){
                    commit('SET_COMMON_LIST', response.result);
                    resolve(response)
                }else {
                    resolve(response)
                }
            }).catch( error => {
                reject(error);
            })
        })
    },

    initSelfMessage: ({ commit, state }) => {
        return new Promise((resolve, reject) => {
            getAction(`/chat/self/${store.getters.userInfo.id}/${state.currentSessionId}`).then(response => {
                if(response.success){
                    commit('SET_MESSAGES_LIST', response.result);
                    resolve(response)
                }else {
                    resolve(response)
                }
            }).catch( error => {
                reject(error);
            })
        })
    },

    sendMessage: ({ dispatch, commit ,state}, content) => {
        return new Promise((resolve, reject) => {
            if (content == null || content.trim() == '') {
                this._message('请输入消息内容', 'warning')
                reject(error);
            }

            let data = {
                fromUserId: store.getters.userInfo.id,
                fromUserImg: store.getters.userInfo.avatar,
                fromUserName: store.getters.userInfo.realname,

                toUserId: state.currentSessionId,
                toUserImg: state.currentSessionImg,
                toUserName: state.currentSessionName,
                message: content
            }
            getAction("/chat/push",data).then(response => {
                if(!state.currentSessionId) {
                    // this.websocket.send(content.replace(/[\r\n]/g,""))
                    return dispatch('initCommonMessage').then(() => {
                    })
                }else {
                    return dispatch('initSelfMessage').then(() => {
                    })
                }
            }).catch( error => {
                reject(error);
            })
        })
    },

    //切换选择窗口
    selectSession: ({ commit }, id) => {
        return new Promise((resolve, reject) => {
            commit('SET_SESSION_ID', id);
        })
    },
    
    search: ({ commit }, value) => commit('SET_FILTER_KEY', value)
  }

}

export default chat