import Vue from 'vue'
import Router from 'vue-router'
import App from './App.vue'
import axios from 'axios'

import Login from './components/Login.vue'
import Profile from './components/Profile.vue'
import Games from './components/Games.vue'
import Game from './components/Game.vue'
import GamePanel from './components/GamePanel.vue'
import BotPanel from './components/BotPanel.vue'
import Bot from './components/Bot.vue'

Vue.use(Router);
Vue.config.productionTip = false;

const routes = [
    { path: '/login', component: Login },
    { path: '/users/:user', component: Profile },
    { path: '/users/:user/bots/:bot', component: Bot },
    { path: '/games', component: Games },
    { path: '/games/:game', component: Game },
    { path: '/mygames', component: GamePanel },
    { path: '/mybots', component: BotPanel },
];

const router = new Router({
    routes: routes
});

export const axiosConfig = {
    baseURL: 'http://localhost:8000/api/',
    timeout: 30000
};

Vue.prototype.$axios = axios.create(axiosConfig);

export const globalState = {
    loggedInUser: ''
};

new Vue({
    render: h => h(App),
    router: router,
    data: {
        globalState
    }
}).$mount('#app');