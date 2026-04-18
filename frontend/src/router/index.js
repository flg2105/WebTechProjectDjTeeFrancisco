import { createRouter, createWebHistory } from 'vue-router'
import InstructorRegistrationView from '../views/InstructorRegistrationView.vue'
import LoginView from '../views/LoginView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/register/instructor-invite-demo'
    },
    {
      path: '/register/:token',
      name: 'register',
      component: InstructorRegistrationView
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView
    }
  ]
})

export default router
