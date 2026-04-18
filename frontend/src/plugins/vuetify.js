import { createVuetify } from 'vuetify'

export default createVuetify({
  theme: {
    defaultTheme: 'projectPulse',
    themes: {
      projectPulse: {
        dark: false,
        colors: {
          primary: '#12343b',
          secondary: '#c8963e',
          surface: '#fffaf1',
          background: '#f3ece2',
          success: '#2f6f4f',
          error: '#9f2d20'
        }
      }
    }
  }
})
