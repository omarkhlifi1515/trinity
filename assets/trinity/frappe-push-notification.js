import { initializeApp } from "firebase/app"
import {
    getMessaging,
    getToken,
    isSupported,
    deleteToken,
    onMessage as onFCMMessage,
} from "firebase/messaging"

class FrappePushNotification {
    static get relayServerBaseURL() {
        return window.frappe?.boot.push_relay_server_url
    }

    constructor(projectName) {
        this.projectName = projectName
        this.webConfig = null
        this.vapidPublicKey = ""
        this.token = null
        this.initialized = false
        this.messaging = null
        this.serviceWorkerRegistration = null
        this.onMessageHandler = null
    }

    async initialize(serviceWorkerRegistration) {
        if (this.initialized) return
        this.serviceWorkerRegistration = serviceWorkerRegistration
        const config = await this.fetchWebConfig()
        this.messaging = getMessaging(initializeApp(config))
        this.onMessage(this.onMessageHandler)
        this.initialized = true
    }

    async appendConfigToServiceWorkerURL(url, parameter_name = "config") {
        let config = await this.fetchWebConfig()
        const encode_config = encodeURIComponent(JSON.stringify(config))
        return `${url}?${parameter_name}=${encode_config}`
    }

    async fetchWebConfig() {
        if (this.webConfig !== null && this.webConfig !== undefined) return this.webConfig
        try {
            let url = `${FrappePushNotification.relayServerBaseURL}/api/method/notification_relay.api.get_config?project_name=${this.projectName}`
            let response = await fetch(url)
            let response_json = await response.json()
            this.webConfig = response_json.config
            return this.webConfig
        } catch (e) {
            throw new Error("Push Notification Relay is not configured properly on your site.")
        }
    }

    async fetchVapidPublicKey() {
        if (this.vapidPublicKey !== "") return this.vapidPublicKey
        try {
            let url = `${FrappePushNotification.relayServerBaseURL}/api/method/notification_relay.api.get_config?project_name=${this.projectName}`
            let response = await fetch(url)
            let response_json = await response.json()
            this.vapidPublicKey = response_json.vapid_public_key
            return this.vapidPublicKey
        } catch (e) {
            throw new Error("Push Notification Relay is not configured properly on your site.")
        }
    }

    onMessage(callback) {
        if (callback == null) return
        this.onMessageHandler = callback
        if (this.messaging == null) return
        onFCMMessage(this.messaging, this.onMessageHandler)
    }

    isNotificationEnabled() {
        return localStorage.getItem(`firebase_token_${this.projectName}`) !== null
    }

    async enableNotification() {
        if (!(await isSupported())) throw new Error("Push notifications are not supported on your device")
        if (this.token != null) return { permission_granted: true, token: this.token }
        const permission = await Notification.requestPermission()
        if (permission !== "granted") return { permission_granted: false, token: "" }
        let oldToken = localStorage.getItem(`firebase_token_${this.projectName}`)
        const vapidKey = await this.fetchVapidPublicKey()
        let newToken = await getToken(this.messaging, { vapidKey: vapidKey, serviceWorkerRegistration: this.serviceWorkerRegistration })
        if (oldToken !== newToken) {
            if (oldToken) await this.unregisterTokenHandler(oldToken)
            let isSubscriptionSuccessful = await this.registerTokenHandler(newToken)
            if (isSubscriptionSuccessful === false) throw new Error("Failed to subscribe to push notification")
            localStorage.setItem(`firebase_token_${this.projectName}`, newToken)
        }
        this.token = newToken
        return { permission_granted: true, token: newToken }
    }

    async disableNotification() {
        if (this.token == null) {
            this.token = localStorage.getItem(`firebase_token_${this.projectName}`)
            if (this.token == null || this.token === "") return
        }
        try {
            await deleteToken(this.messaging)
        } catch (e) {
            console.error("Failed to delete token from firebase", e)
        }
        try {
            await this.unregisterTokenHandler(this.token)
        } catch (e) {
            console.error("Failed to unsubscribe from push notification", e)
        }
        localStorage.removeItem(`firebase_token_${this.projectName}`)
        this.token = null
    }

    async registerTokenHandler(token) {
        try {
            let response = await fetch(
                "/api/method/frappe.push_notification.subscribe?fcm_token=" + token + "&project_name=" + this.projectName,
                { method: "GET", headers: { "Content-Type": "application/json" } }
            )
            return response.status === 200
        } catch (e) {
            console.error(e)
            return false
        }
    }

    async unregisterTokenHandler(token) {
        try {
            let response = await fetch(
                "/api/method/frappe.push_notification.unsubscribe?fcm_token=" + token + "&project_name=" + this.projectName,
                { method: "GET", headers: { "Content-Type": "application/json" } }
            )
            return response.status === 200
        } catch (e) {
            console.error(e)
            return false
        }
    }
}

export default FrappePushNotification
