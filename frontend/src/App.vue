<template>
	<ion-app>
		<ion-router-outlet id="main-content" />
		<Toasts />

		<InstallPrompt />
	</ion-app>
</template>

<script setup>
import { onMounted } from "vue"
import { IonApp, IonRouterOutlet } from "@ionic/vue"

import { Toasts } from "trinity-ui"

import InstallPrompt from "@/components/InstallPrompt.vue"
import { showNotification } from "@/utils/pushNotifications"

onMounted(() => {
	window?.trinityPushNotification?.onMessage((payload) => {
		showNotification(payload)
	})
})
</script>
