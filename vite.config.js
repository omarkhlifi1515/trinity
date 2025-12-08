import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import laravel, { refreshPaths } from "laravel-vite-plugin";
import tailwindcss from "@tailwindcss/vite";

export default defineConfig({
    plugins: [
        vue(),
        laravel({
            input: [
                "resources/css/app.css",
                "resources/js/app.js",
                "resources/js/filament-chat.js",
                "resources/css/filament/admin/theme.css",
            ],
            refresh: [...refreshPaths, "app/Livewire/**"],
        }),
        tailwindcss(),
    ],
});
