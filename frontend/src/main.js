import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { authSession } from './shared/services/authSession'
import './styles.css'

authSession.hydrate()

createApp(App).use(router).mount('#app')
