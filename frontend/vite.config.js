import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// The dev server proxies /api calls to the Spring Boot backend on :8080.
// host:true binds to 0.0.0.0 so cloud IDEs (Codespaces, Gitpod) can forward port 3000.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    host: true,
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
});
