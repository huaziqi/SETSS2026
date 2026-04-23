import {createRouter, createWebHistory} from 'vue-router';

import Home from '../views/Home.vue';
import Login from '../views/Login.vue';
import ComponentsTest from '../views/testpage/ComponentsTest.vue';


const routes = [
    {path : '/', component : Home},
    {path : '/login', component : Login },




    {path: '/testcomp', component: ComponentsTest}

]


const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router;