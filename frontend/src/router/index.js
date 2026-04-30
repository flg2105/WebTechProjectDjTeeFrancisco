import { createRouter, createWebHistory } from 'vue-router'
import { authSession } from '../shared/services/authSession'

import HomeGateView from '../features/home/HomeGateView.vue'
import AdminHomeView from '../features/home/AdminHomeView.vue'
import InstructorHomeView from '../features/home/InstructorHomeView.vue'
import StudentHomeView from '../features/home/StudentHomeView.vue'
import LoginView from '../features/auth/LoginView.vue'
import SectionsView from '../features/sections/SectionsView.vue'
import InstructorsView from '../features/instructors/InstructorsView.vue'
import StudentsView from '../features/students/StudentsView.vue'
import TeamsView from '../features/teams/TeamsView.vue'
import RubricsView from '../features/rubrics/RubricsView.vue'
import WarView from '../features/war/WarView.vue'
import PeerEvalView from '../features/peereval/PeerEvalView.vue'
import ReportsView from '../features/reports/ReportsView.vue'
import ForbiddenView from '../features/system/ForbiddenView.vue'

const routes = [
  { path: '/login', name: 'login', component: LoginView, meta: { public: true } },
  { path: '/', name: 'home', component: HomeGateView },
  { path: '/forbidden', name: 'forbidden', component: ForbiddenView, meta: { public: true } },
  { path: '/home/admin', name: 'adminHome', component: AdminHomeView, meta: { roles: ['ADMIN'] } },
  { path: '/home/instructor', name: 'instructorHome', component: InstructorHomeView, meta: { roles: ['INSTRUCTOR'] } },
  { path: '/home/student', name: 'studentHome', component: StudentHomeView, meta: { roles: ['STUDENT'] } },
  { path: '/sections', name: 'sections', component: SectionsView, meta: { roles: ['ADMIN'] } },
  { path: '/instructors', name: 'instructors', component: InstructorsView, meta: { roles: ['ADMIN'] } },
  { path: '/students', name: 'students', component: StudentsView, meta: { roles: ['ADMIN', 'INSTRUCTOR'] } },
  { path: '/teams', name: 'teams', component: TeamsView, meta: { roles: ['ADMIN', 'INSTRUCTOR'] } },
  { path: '/rubrics', name: 'rubrics', component: RubricsView, meta: { roles: ['ADMIN'] } },
  { path: '/war', name: 'war', component: WarView, meta: { roles: ['STUDENT'] } },
  { path: '/peer-eval', name: 'peerEval', component: PeerEvalView, meta: { roles: ['STUDENT'] } },
  { path: '/reports', name: 'reports', component: ReportsView, meta: { roles: ['INSTRUCTOR'] } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  if (to.meta.public) {
    if (to.name === 'login' && authSession.isAuthenticated) {
      return { path: '/' }
    }
    return true
  }

  if (!authSession.isAuthenticated) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }

  try {
    if (!authSession.currentUser?.role) {
      await authSession.refreshCurrentUser()
    }
    if (to.meta.roles && !to.meta.roles.includes(authSession.currentUser?.role)) {
      return { path: '/forbidden', query: { from: to.fullPath } }
    }
    return true
  } catch {
    authSession.logout()
    return { path: '/login', query: { redirect: to.fullPath } }
  }
})

export default router
