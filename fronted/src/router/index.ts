import { createRouter, createWebHistory } from "vue-router";

import Home from "../views/Home.vue";
import Login from "../views/Auth/Login.vue";
import Register from "../views/Auth/Register.vue";
import ComponentsTest from "../views/testpage/ComponentsTest.vue";
import Profile from '../views/Auth/Profile.vue';
import Forum from "../views/forum/Forum.vue";
import Schedule from "../views/Info/Schedule.vue";
import ForumWrite from "../views/forum/ForumWrite.vue";
import MyPosts from "../views/forum/MyPosts.vue";

import Submit from "../views/submitmanu/Submit.vue";

const routes = [
  { path: "/", component: Home },
  { path: "/login", component: Login },
  { path: "/register", component: Register },

  { path: "/schedule", component: Schedule },

  {path : '/profile', component: Profile},  
  {path : "/testcomp", component: ComponentsTest },

  {path : '/forum', component: Forum},

  {path : '/forum/write', component: ForumWrite},
  {path : '/forum/my-posts', component: MyPosts},

  { path: "/submit", component: Submit },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
