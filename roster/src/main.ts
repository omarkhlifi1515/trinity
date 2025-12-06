import "./index.css";

import { createApp } from "vue";
import router from "./router";
import App from "./App.vue";

import { Button, setConfig, trinityRequest, resourcesPlugin } from "trinity-ui";

const app = createApp(App);

setConfig("resourceFetcher", trinityRequest);

app.use(router);
app.use(resourcesPlugin);

app.component("Button", Button);
app.mount("#app");
