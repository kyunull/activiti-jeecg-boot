<script>
import store from '../../../store';
import { mapActions, mapGetters,mapState } from 'vuex'
import {getFileAccessHttpUrl} from '@/api/manage';
export default {
    computed: {
      ...mapState({
        user: state => state.user.info,        
        filterKey: state => state.chat.filterKey,
      })
    },
    methods: {
        ...mapActions(["search"]),
        onKeyup (e) {
            this.search(e.target.value);
        },
        getAvatarView: function (avatar) {
            return getFileAccessHttpUrl(avatar)
        },
    },
    watch: {

    },
};
</script>

<template>
<div class="card">
    <header>
        <a-avatar shape="square" :src="getAvatarView(user.avatar)" icon="user"/>
        <p class="name">{{user.realname}}</p>
    </header>
    <footer>
        <input class="search" type="text" placeholder="search user..." @keyup="onKeyup">
    </footer>
</div>
</template>

<style scoped lang="less">
.card {
    padding: 12px;
    border-bottom: solid 1px #24272C;

    footer {
        margin-top: 10px;
    }

    .avatar, .name {
        vertical-align: middle;
    }
    .avatar {
        border-radius: 2px;
    }
    .name {
        display: inline-block;
        margin: 0 0 0 15px;
        font-size: 16px;
    }
    .search {
        padding: 0 10px;
        width: 100%;
        font-size: 12px;
        color: #fff;
        height: 30px;
        line-height: 30px;
        border: solid 1px #3a3a3a;
        border-radius: 4px;
        outline: none;
        background-color: #26292E;
    }
}
</style>