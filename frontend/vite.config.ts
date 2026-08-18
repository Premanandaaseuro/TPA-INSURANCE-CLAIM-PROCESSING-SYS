import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 7001,
    strictPort: true,
    proxy: {
      '/api': {
        target: 'http://localhost:7002',
        changeOrigin: true,
        secure: false,
      },
    },
  },
});
