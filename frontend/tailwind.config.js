import trinityUIPreset from "trinity-ui/src/tailwind/preset"
export default {
	presets: [trinityUIPreset],
	content: [
		"./index.html",
		"./src/**/*.{vue,js,ts,jsx,tsx}",
		"./node_modules/trinity-ui/src/components/**/*.{vue,js,ts,jsx,tsx}",
		"../node_modules/trinity-ui/src/components/**/*.{vue,js,ts,jsx,tsx}",
	],
	theme: {
		extend: {
			screens: {
				standalone: {
					raw: "(display-mode: standalone)",
				},
			},
			padding: {
				"safe-top": "env(safe-area-inset-top)",
				"safe-right": "env(safe-area-inset-right)",
				"safe-bottom": "env(safe-area-inset-bottom)",
				"safe-left": "env(safe-area-inset-left)",
			},
		},
	},
	plugins: [],
}
