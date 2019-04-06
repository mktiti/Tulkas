<template>
    <div id="app">
        <div id="header">
            <router-link v-if="isLoggedIn" :to="'/users/' + loggedInUser" class="header-elem">Profile ({{ this.loggedInUser }})</router-link>
            <router-link v-if="isLoggedIn" to="/mybots" class="header-elem">My bots</router-link>
            <router-link v-if="isLoggedIn" to="/mygames" class="header-elem">My games</router-link>
            <router-link to="/games" class="header-elem">All games</router-link>
            <a v-if="isLoggedIn" href="#" class="header-elem" @click="logout">Logout</a>
            <router-link v-if="!isLoggedIn" to="/login" class="header-elem">Login</router-link>
        </div>

        <router-view v-on:login-success="onLogin($event)" />
    </div>
</template>

<script>
    import Login from "./components/Login";
    import {globalState} from "./main";

    export default {
        name: 'app',
        components: {Login},
        computed: {
            isLoggedIn: function () {
                return globalState.loggedInUser !== '';
            },
            loggedInUser: function () {
                return globalState.loggedInUser;
            }
        },
        methods: {
            logout: function () {
                globalState.loggedInUser = '';
                this.$router.push("/login");
            },
            onLogin: function (event) {
                globalState.loggedInUser = event;
                this.$router.push("/profile");
            }
        }
    }
</script>

<style>
    * {
        outline: none;
        margin: 0;
        padding: 0;
        @import url('https://fonts.googleapis.com/css?family=Ubuntu');
        font-family: 'Ubuntu', sans-serif;
    }

    #app {
        -webkit-font-smoothing: antialiased;
        -moz-osx-font-smoothing: grayscale;
        text-align: center;
        color: #2c3e50;
        margin: 0;
    }

    #header {
        background: #4d6bec;
        width: 100%;
        text-align: right;
    }

    .header-elem:last-child {
        padding-right: 25px;
    }

    .header-elem {
        display: inline-block;
        padding: 16px 8px 16px 8px;
        font-size: x-large;
        color: white;
        font-weight: bold;
        text-decoration: none;
    }

    .header-elem:hover,
    .header-elem.router-link-active {
        background-color: #3147a4;
        color: #FAFAFA;
    }
</style>
