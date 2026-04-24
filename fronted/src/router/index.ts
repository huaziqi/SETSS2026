import { createRouter, createWebHistory } from "vue-router";

import Home from "../views/Home.vue";
import Login from "../views/Auth/Login.vue";
import Register from "../views/Auth/Register.vue";
import ComponentsTest from "../views/testpage/ComponentsTest.vue";

import Submit from "../views/submitmanu/Submit.vue";

const routes = [
  { path: "/", component: Home },
  { path: "/login", component: Login },
  { path: "/register", component: Register },

  { path: "/testcomp", component: ComponentsTest },

  { path: "/submit", component: Submit },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
