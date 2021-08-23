<script>
import { mapActions, mapGetters, mapState ,mapMutations} from 'vuex'
import { getFileAccessHttpUrl } from '@/api/manage'
import store from '../../../store'

export default {
  data() {
    return {
      sysGroupimg: 'temp/group.png'
    }
  },
  methods: {
    ...mapMutations(['SET_SESSION_IMG','SET_SESSION_NAME']),
    ...mapActions(['selectSession','initCommonMessage','initSelfMessage']),
    getAvatarView: function(avatar) {
      return getFileAccessHttpUrl(avatar)
    },

    //切换选择窗口
    selectWindow: function(id, img, name) {
        this.selectSession(id);
        this.SET_SESSION_IMG(img);
        this.SET_SESSION_NAME(name);
        if (!id) {
            this.initCommonMessage();
        } else {
            this.initSelfMessage();
        }
    },

  },
  computed: {
    ...mapState({
      // 过滤后的会话列表
      onlineUserList: state => {
        let tmp = state.chat.onlineUserList.filter(onlineUser => onlineUser.realname.includes(state.chat.filterKey))
        let result = tmp.filter(onlineUser => onlineUser.id != store.getters.userInfo.id)
        return result
      },
      // 当前会话index
      currentId: state => state.chat.currentSessionId
    })
  }
}
</script>

<template>
  <div class="list">
    <ul>
      <li :class="{ active: currentId === 0 }" @click="selectWindow(0, '', '')">
        <a-avatar shape="square" :src="getAvatarView(sysGroupimg)" icon="user" />
        <p class="name">官方群组</p>
      </li>
      <li
        v-for="item in onlineUserList"
        :class="{ active: item.id === currentId }"
        :key="item.id"
        @click="selectWindow(item.id, item.avatar, item.realname)"
      >
        <a-avatar shape="square" :src="getAvatarView(item.avatar)" icon="user" />
        <p class="name">{{ item.realname }}</p>
      </li>
    </ul>
  </div>
</template>

<style scoped lang="less">
.list {
  ul {
    padding-inline-start: 10px;
  }
  li {
    padding: 12px 15px;
    border-bottom: 1px solid #292c33;
    cursor: pointer;
    transition: background-color 0.1s;
    list-style: none;

    &:hover {
      background-color: rgba(255, 255, 255, 0.03);
    }
    &.active {
      background-color: rgba(255, 255, 255, 0.1);
    }
  }
  .avatar,
  .name {
    vertical-align: middle;
  }
  .avatar {
    border-radius: 2px;
  }
  .name {
    display: inline-block;
    margin: 0 0 0 15px;
  }
}
</style>
